;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.common.core
  "RITA main."
  (:require [clojure.tools.cli :as cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [clojure.data.codec.base64 :as base64]
            [clojure.string :as string]
            [clojure.pprint :as pp :refer [pprint]]
            [me.raynes.fs :as fs]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [mbroker.rabbitmq :as rmq]
            [rita.common.config :as config]
            [clojurewerkz.machine-head.client :as mqtt])
  (:gen-class))                                             ;; required for uberjar

(defn application-directory []
  (or (System/getenv "APPDIR")
      (let [dir (fs/expand-home "~/DOLL/ASIST - RITA/git/Code")]
        (log/warn "APPDIR isn't set.  It will be assumed to be " (str dir))
        dir)))

(def cli-options [["-h" "--host rmqhost" "RMQ Host" :default "localhost"]
                  ["-p" "--port rmqport" "RMQ Port" :default 5672 :parse-fn #(Integer/parseInt %)]
                  ["-q" "--mqtthost mqtthost" "Mosquitto Host" :default "localhost"]
                  ["-e" "--exchange name" "RMQ Exchange Name" :default "rita"]
                  ["-m" "--mongo-db host" "Mongo DB Host" :default "localhost"]
                  ["-c" "--simulate-clock" "Will listen for clock messages on rkey `clock` and use the clock for timeouts only!" :default true]
                  ["-t" "--regression-test" "Run components in test mode" :default false]
                  ["-?" "--help" "Print usage"]])

(def repl "True if we're running in a repl." true)

(def demo-delay-mode?
  "True if we insert delays in component execution, for demonstration purposes"
  false)

; Cache RMQ connection info
(defonce rmq-info (atom nil))
(defonce mdb-host (atom "localhost"))
(defonce mqtt-host (atom nil))
(defonce mqtt-connection (atom nil))
(defonce sim-clock (atom true))
(defonce test-mode (atom false))

(def current-exchange
  "The current RMQ exchange"
  (atom nil))

(def current-app-id
  "The current RITA application/module (e.g., ConstraintsIntegrator)"
  (atom "UNDEFINED"))

(def last-msg
  "The last RMQ received.  This is for debugging only."
  (atom {}))

(def msg-queue
  "This is the collection of pending messages for this application/module."
  (atom []))

(defn rabbitmq-keys-and-agents-from-string-map
  "Perform type coercions to convert this to JSON to the form expected by our macros"
  [string-map]
  (into {}
        (map (fn [[k1 v1]]
               (vector (symbol k1)
                       (into {}
                             (map (fn [[k2 v2]]
                                    (let [key (keyword k2)
                                          val (case key
                                                (:publishers :subscribers) (into [] (map symbol v2))
                                                :required-keys (into [] (map keyword v2))
                                                :optional-keys (into [] (map keyword v2)))]
                                      (vector key val)))
                                  v1))))
             string-map)))

(def keys-and-agents
  "Cached copy of the all of the RabbitMQ messages, attributes, and the publishers
  and subscribers"
  (atom {}))

(defn get-mdb-host []
  @mdb-host)

(defn get-mqtt-host []
  @mqtt-host)

(defn make-mqtt-connection [& [host]]
  (let [mqh (or host (get-mqtt-host))]
    (println "Connecting to MQTT:" mqh)
    (if @mqtt-connection
      (println "MQTT Connection is not nil. close-mqtt-connection and try again")
      (when mqh (try (let [con (mqtt/connect (str "tcp://" mqh ":1883"))]
                       (println "MQTT Connected?" (mqtt/connected? con))
                       (reset! mqtt-connection con))
                     (catch Exception e
                       (println "Error connecting to MQTT host:" (get-mqtt-host) "reason:" (.getMessage e))))))))

(defn close-mqtt-connection []
  (if (and @mqtt-connection (mqtt/connected? @mqtt-connection))
    (mqtt/disconnect-and-close @mqtt-connection))
  (reset! mqtt-connection nil))

(defn get-sim-clock []
  @sim-clock)

(defn get-test-mode []
  @test-mode)

(defn rabbitmq-keys-and-agents []
  (if (empty? @keys-and-agents)
    (let [json-filename "rabbitmq-messages.json"
          filename (or (io/resource (str "public/" json-filename))
                       #_(str (application-directory) "/resources/public/" json-filename))
          map-of-keys-and-agents (rabbitmq-keys-and-agents-from-string-map
                                   (json/read-str (slurp filename)))]

      (if (not (empty? map-of-keys-and-agents))
        (reset! keys-and-agents map-of-keys-and-agents))))
  @keys-and-agents)

(defn make-rmq-connection [exchange options]
  (if-not @rmq-info (reset! rmq-info (rmq/make-channel exchange options))
                    (do
                      (println @current-app-id "RMQ Connection exists. Close / Cleanup and call this function again")
                      @rmq-info)))

(defn get-channel []
  (if @rmq-info (get @rmq-info :channel)))

(defn get-exchange []
  (if @rmq-info (get @rmq-info :exchange)))

(defn get-rmq-host []
  (if @rmq-info (get-in @rmq-info [:config :host])))

(defn get-rmq-port []
  (if @rmq-info (get-in @rmq-info [:config :port])))

(defn subscriptions-for-component
  "Return a collection of all of the exchange-keys that are subscribed by this component"
  [component]
  (map first (filter (fn [[k v]]
                       (some #(= component %) (:subscribers v)))
                     (rabbitmq-keys-and-agents))))

(defn verify-rmq-message
  "Verifies that all of the required keys are present in this message.
  If there are any problems, a map is returned with an `:error` key that describes the problem."
  [msg]
  (let [routing-key-string (get msg :routing-key)
        routing-key (and routing-key-string (symbol routing-key-string))
        required-keys (get-in (rabbitmq-keys-and-agents) [routing-key :required-keys])
        missing-keys (if-not required-keys
                       '(:unknown-routing-key)
                       (reduce #(if (find msg %2) %1 (conj %1 %2)) () required-keys))]
    (cond
      (nil? routing-key)
      {:error (str "Found a message without a routing key: " msg)}

      (not (empty? missing-keys))
      {:error (str "The " routing-key " message (Mission-Id=" (or (:mission-id msg) "???")
                   ") is missing the required keys:" missing-keys)}
      :else {})))                                           ; No errors to report

(defn verify-rmq-message-and-report-errors
  "Verifies that all of the required keys are present in this message.
  If there are any problems, the description is printed."
  [msg]
  (let [result (verify-rmq-message msg)
        error (:error result)]
    (when error
      (println "WARN:" error))
    result))

(def defined-conditions
  "Map of the defined conditions for this module.
  The key is the condition name, and the value is the function that checks whether a
  condition is true, and if so, executes the `body` of the condition."
  (atom {}))

;; We assume that every message has a :mission-id and that a specific condition instance
;; is composed of one or more messages, all with the same :mission-id
(defmacro defcondition
  "Define a new condition, which is a set of messages, of interest to this module.
  The `routing-keys` are used *both* for condition matching (i.e., when *all* of those messages
  are received), and as local variables bound to the respective messages to be accessed
  during execution of the `body`"
  [condition-name [& routing-keys] [channel] & body]
  (let [mission (gensym "mission")
        msg (gensym "msg")
        condition-fn (gensym "condition-fn")
        key-checks (map (fn [key-name]
                          `(some (fn [~msg]
                                   (and (= (:mission-id ~msg) ~mission)
                                        #_(= (:routing-key ~msg) ~key-name)
                                        ;;A possibly temporary check
                                        (or (= (:routing-key ~msg) ~key-name)
                                            (= (:received-routing-key ~msg) ~key-name))
                                        ~msg))
                                 @msg-queue))
                        (map str routing-keys))
        keys-and-checks (mapcat list routing-keys key-checks)]
    `(let [~condition-fn (fn ~condition-name [~mission ~channel]
                           (let [~@keys-and-checks]
                             (when (and ~@routing-keys)
                               (doseq [~msg (list ~@routing-keys)]
                                 (verify-rmq-message-and-report-errors ~msg))
                               ;;+++ removed temporarily - to be replaced! (println "True Condition: " '~condition-name)
                               ;;Remove the relevant msgs from the msg-queue
                               ;;This ASSUMES that a msg is used in only one condition
                               (swap! msg-queue #(remove (set (list ~@routing-keys)) %))

                               ~@body)))
           undefined-routing-keys# (filter (fn [key#]
                                            (not (get (rabbitmq-keys-and-agents) key#)))
                                           (quote ~routing-keys))]
       (when (not (empty? undefined-routing-keys#))
         (println "WARN: defcondition" '~condition-name "references the following undefined routing keys:" undefined-routing-keys#))
       (swap! defined-conditions assoc '~condition-name ~condition-fn)
       '~condition-name)))

(defmacro defmain
  "Defines a `-main` function for this namespace, wrapping `body` with all of the
  necessary boilerplate."
  [fn-name config & body]
  (let [args (gensym "args")
        parsed (gensym "parsed")
        opts (gensym "opts")
        help (gensym "help")
        exchange (gensym "exchange")
        conn-info (gensym "conn-info")
        configvar (gensym "config")
        routing-key-symbol (gensym "routing-key-symbol")
        mdbhost (gensym "mdbhost")
        mosquitto-host (gensym "mosquitto-host")
        simclock (gensym "simclock")
        testmode (gensym "testmode")]

    `(defn ~fn-name [& ~args]
       (let [~parsed (cli/parse-opts ~args cli-options)
             ~opts (:options ~parsed)
             ;;_ (pprint ~opts)
             ~help (:help ~opts)
             ~exchange (:exchange ~opts)
             ~conn-info (make-rmq-connection ~exchange ~opts)
             ~configvar ~config
             ~mdbhost (:mongo-db ~opts)
             ~mosquitto-host (:mqtthost ~opts)
             ~simclock (:simulate-clock ~opts)
             ~testmode (:regression-test ~opts)]

         ;;(def repl false)
         (when (:errors ~parsed)
           (print-help (:summary ~parsed))
           (println (string/join \newline (:errors ~parsed)))
           (exit 0))

         (when ~help
           (print-help (:summary ~parsed))
           (exit 0))

         (println ~configvar)
         (reset! current-exchange ~exchange)
         (reset! current-app-id (str ~configvar))
         (println "Setting up subscriptions for" ~configvar "wait 1 secs")
         (doseq [~routing-key-symbol (subscriptions-for-component ~configvar)]
           (println "Subscribing to:" ~routing-key-symbol)
           (rmq/make-subscription (str ~routing-key-symbol) subscription-handler
                                  (:channel ~conn-info) ~exchange))

         (reset! mdb-host ~mdbhost)
         (reset! mqtt-host ~mosquitto-host)
         (reset! sim-clock ~simclock)
         (reset! test-mode ~testmode)
         ~@body

         (Thread/sleep 1000)

         ; Cleanup previous connection
         #_(when ~conn-info
             (rmq/close-connection (:connection ~conn-info)))
         (config/read-config-from-resource)
         (println "App State")
         (println "----------")
         (clojure.pprint/pprint ~conn-info)
         (println "Mongo DB Host" @mdb-host)
         (println "MQTT HOST" @mqtt-host)
         (println "Regression Test Mode:" (get-test-mode))
         (println "----------")))))

(defn publish-message
  "Publish the RITA distributed control message to RMQ.
  `message-attributes` is a map of attributes to be included in the message, going beyond the
  standard ones."
  [channel mission-id routing-key message-attributes]
  (let [msg (merge {:mission-id  mission-id :timestamp (System/currentTimeMillis)
                    :routing-key routing-key :app-id @current-app-id}
                   message-attributes)]
    (verify-rmq-message-and-report-errors msg)
    (rmq/publish-object msg routing-key channel (or @current-exchange (get-exchange)))))

(defn usage [options-summary]
  (->> ["RITA Template"
        ""
        "Usage: java -jar foo.jar [options]"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn print-help [summary-options]
  (println (usage summary-options)))

(defn exit [code]
  (when-not repl
    (System/exit code))
  (if repl
    (println "in repl mode. Not exiting")))

(defn subscription-handler [ch metadata ^bytes data]
  (let [msg (json/read-str (String. data "UTF-8") :key-fn keyword)]
    (reset! last-msg msg)
    (swap! msg-queue #(conj % msg))
    ;;Now that we've added msg to the msg-queue, run all of the defined-condition functions
    (doseq [[_ c-fn] @defined-conditions]
      (c-fn (:mission-id msg) ch))))
