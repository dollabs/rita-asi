;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.rmq-validator.cli
  "RITA AttackModelGenerator main."
  (:require [rita.rmq-validator.specs :as rita-specs]
            [clojure.tools.cli :as cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [clojure.data.codec.base64 :as base64]
            [clojure.string :as string]
            [clojure.pprint :as pp :refer [pprint]]
            [avenir.utils :refer [assoc-if]]
            [me.raynes.fs :as fs]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [mbroker.rabbitmq :as rmq]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [pamela.parser :as parser]
            [pamela.utils :as putils]
            [rita.common.core :as rc :refer :all]
            [rita.common.surveys :as surveys]
            [pamela.tools.plant.interface :as pi]
            [pamela.tools.plant.connection :as pc]
            ;; [pamela.tools.utils.rabbitmq :as rmq] ;; TODO: deal with this redundancy!
            [pamela.tools.utils.tpn-json :as tpn-json]
            [clojure.java.io :as io]
            [clojure.future :refer :all] ;;Clojure 1.9 functions
            [clojure.spec.alpha :as spec])
  (:gen-class)) ;; required for uberjar

;; Conditions to check for:
;; * Unknown message (not defined in rabbitmq-messages.json)
;; * Message published by a publisher not defined in rabbitmq-messages.json
;; * Unknown keys in a message
;; * Missing required-keys in a message
;; * Value of a key fails the schema check

;; These are messages that we're not (yet) going to validate
(def unvalidated-routing-keys #{"network.new"
                                "network.reset"
                                "tpn.object.update"
                                "tpn.activity.negotiation"
                                "tpn.activity.finished"
                                "dispatcher-manager"
                                "plant"})

(defn validate-message [msg]
  (let [{:keys [metadata-routing-key routing-key received-routing-key app-id
                timestamp mission-id]} msg
        in-message-routing-key (or routing-key received-routing-key)
        _ (assert metadata-routing-key) ;this should NEVER be missing
        unvalidated? (unvalidated-routing-keys metadata-routing-key)
        msg-def (get (rabbitmq-keys-and-agents) (symbol metadata-routing-key))]
    (when (not unvalidated?)
      (if-not msg-def
        {:unknown-message {:routing-key metadata-routing-key
                           :message msg}}
        (let [{:keys [publishers subscribers required-keys optional-keys]} msg-def
              ;; Allow for case-insensitivity when verifying the publisher
              app-id-lowercase (string/lower-case (or app-id "missing-app-id"))
              valid-publisher? (some #(= app-id-lowercase %) (map string/lower-case publishers))
              missing-required-keys (reduce #(if (find msg %2) %1 (conj %1 %2))
                                            () required-keys)
              ;; unknown keys are those not declared as required or optional
              declared-keys (concat required-keys optional-keys)
              unknown-keys (remove #{:metadata-routing-key} ;;We inserted this: remove the evidence
                                   (reduce #(if (>= (.indexOf declared-keys %2) 0) %1 (conj %1 %2))
                                           () (keys msg)))]
          (assoc-if (spec/explain-data :rita.rmq-validator.specs/message msg)
                    :invalid-publisher (and (not valid-publisher?)
                                            {:message msg})
                    ;; This is redundant with the spec-based validation
                    :missing-required-keys (and (not (empty? missing-required-keys))
                                                {:missing-keys missing-required-keys
                                                 :message msg})
                    ;;It's OK for the in-message-routing-key to be missing,
                    ;;but it had better be the same as the metadata routing-key
                    :inconsistent-routing-keys (and in-message-routing-key
                                                    (not (= metadata-routing-key in-message-routing-key))
                                                    {:metadata metadata-routing-key
                                                     :in-message in-message-routing-key})
                    :unknown-keys (and (not (empty? unknown-keys))
                                       {:unknown-keys unknown-keys
                                        :message msg}))
          )))))

(defn validate-the-messages [messages]
  (doseq [msg messages]
    (let [validation-errors (validate-message msg)]
      (if-not (empty? validation-errors)
        (pprint validation-errors)))))

;; Some one-time utilities to create a starting template for our spec definitions
;; These are used to initialize the content of specs.clj
;;
(defn pprint-spec-for-all-keys []
  (let [keys (sort (distinct (mapcat (fn [[_ spec]]
                                 (concat (:required-keys spec) (:optional-keys spec)))
                                     (rabbitmq-keys-and-agents))))
        key-spec-defs (map #(list 'spec/def (keyword "keyword" (name %)) 'any?)
                           keys)]
    (doseq [def key-spec-defs]
      (pprint def)
      (newline))))

(defn pprint-method-for-all-messages []
  (let [message-fn 'message
        keyword-ns "keyword"
        methods (map (fn [[message-symbol message-defn]]
                       (let [required (vec (map #(keyword keyword-ns (name %))
                                                (:required-keys message-defn)))
                             optional (vec (map #(keyword keyword-ns (name %))
                                                (:optional-keys message-defn)))]
                         (list 'defmethod message-fn (name message-symbol) '[_]
                               (concat (list 'spec/keys
                                             :req-un required)
                                       (if (empty? optional)
                                         ()
                                         (list :opt-un optional))))))
                     (rabbitmq-keys-and-agents))
        sorted-methods (sort-by #(nth % 2) methods)]
    (pprint '(defmulti message :routing-key))
    (newline)
    ;;Since pprint doesn't do exactly what I want...
    (doseq [method sorted-methods]
      (print "(")
      (dotimes [i 4]
        (pr (nth method i)) (print " "))
      (newline)
      (print "  ")
      (let [[spec-fn & keys-and-values] (nth method 4)]
        (print "(") (prn spec-fn)
        (loop [[k v & remaining-keys-and-values] keys-and-values]
          (print "   ") (pr k v)
          (when remaining-keys-and-values
            (newline)
            (recur remaining-keys-and-values))))
      ;; (pr (nth method 4))
      (println "))")
      (newline))
    ;; Last, but not least:
    (prn '(spec/def :rita.rmq-validator.specs/message (spec/multi-spec message :routing-key)))
    ))


(def current-connection-info
  "The current RMQ connection information"
  (atom nil))

(defn shutdown []
  (rmq/close-connection (:connection @current-connection-info))
  (reset! current-connection-info {}))

(def ^:dynamic validation-failure-action
  "Should be one of :error, :warn, or :silent"
  :warn)

(defn validator-subscription-handler [ch metadata ^bytes data]
  (let [metadata-routing-key (:routing-key metadata)
        msg (assoc (json/read-str (String. data "UTF-8") :key-fn keyword)
                   :metadata-routing-key metadata-routing-key)]
    (reset! last-msg msg)
    ;;(swap! msg-queue #(conj % msg)) ;;Debugging only
    (let [validation-errors (validate-message msg)]
      (if-not (empty? validation-errors)
        (case validation-failure-action
          :silent "Do nothing"
          :warn (do
                  (newline)
                  (println "WARN: Validation Failure in RMQ message:")
                  (pprint msg)
                  (pprint validation-errors))
          :error (throw (Exception. (with-out-str
                                      (println "ERROR: Validation Failure in RMQ message:")
                                      (pprint msg)
                                      (pprint validation-errors)))))))))


(defn -main [& args]
  (let [parsed (cli/parse-opts args cli-options)
        opts (:options parsed)
        _ (pprint opts)
        help (:help opts)
        exchange (:exchange opts)
        opts-for-rmq (dissoc opts :model :dp) ;cleanup irrelevant opts
        conn-info (rmq/make-channel exchange opts-for-rmq)
        config 'RMQValidator
        ]

    (when (:errors parsed)
      (print-help (:summary parsed))
      (println (string/join \newline (:errors parsed)))
      (exit 0))

    (when help
      (print-help (:summary parsed))
      (exit 0))

    (reset! current-connection-info conn-info)
    (reset! current-exchange exchange)
    (reset! current-app-id (str config))
    (reset! msg-queue [])
    (println "Setting up subscriptions")
    (rmq/make-subscription "#" validator-subscription-handler
                           (:channel conn-info) exchange)
      (Thread/sleep 2000)

      ;; Cleanup previous connection
      #_(when conn-info
          (rmq/close-connection (:connection conn-info)))

      (println "App State")
      (println "----------")
      (clojure.pprint/pprint conn-info)
      (println "----------")))


(def msg1 {:mission-id "mission114",
           :timestamp 1596123617014,
           :routing-key "startup-rita",
           :app-id "RITAControlPanel"})

(def msg2 {:mission-id "mission114",
           :timestamp 1596123619050,
           :routing-key "observations",
           :app-id "RITAControlPanel",
           :plant-id "plant",
           :state "observations",
           :observations
           [{:field "spatial-ability-question-1", :value 4}
            {:field "spatial-ability-question-2", :value 2}
            {:field "spatial-ability-question-3", :value 2}
            {:field "spatial-ability-question-4", :value 3}
            {:field "spatial-ability-question-5", :value 3}
            {:field "spatial-ability-question-6", :value 3}
            {:field "spatial-ability-question-7", :value 1}
            {:field "spatial-ability-question-8", :value 5}
            {:field "spatial-ability-question-9", :value 2}
            {:field "spatial-ability-question-10", :value 3}
            {:field "spatial-ability-question-11", :value 4}
            {:field "spatial-ability-question-12", :value 5}
            {:field "spatial-ability-question-13", :value 6}
            {:field "spatial-ability-question-14", :value 5}
            {:field "spatial-ability-question-15", :value 4}
            {:field "spatial-ability-aggregate-score", :value 52}
            {:field "satisficing-question-1", :value 2}
            {:field "satisficing-question-2", :value 1}
            {:field "satisficing-question-3", :value 2}
            {:field "satisficing-question-4", :value 2}
            {:field "satisficing-question-5", :value 2}
            {:field "satisficing-question-6", :value 2}
            {:field "satisficing-question-7", :value 1}
            {:field "satisficing-question-8", :value 3}
            {:field "satisficing-question-9", :value "-99"}
            {:field "satisficing-question-10", :value 2}
            {:field "satisficing-aggregate-score", :value 20}
            {:field "subject-id", :value "subject_id_000005"}]})

(def msg3 {:mission-id "mission114",
           :timestamp 1596123621801,
           :routing-key "mission-solver",
           :app-id "RITAControlPanel",
           :mission-solver {}})

(def msg4 {:mission-id "mission114",
           :timestamp 1596123619050,
           :routing-key "observations",
           :app-id "RITAControlPanel",
           :plant-id "plant",
           :state "observations",
           :observations
           [{:field "spatial-ability-question-1", :value 4}
            {:field "spatial-ability-question-2", :value 2}
            {:field "spatial-ability-question-3", :value 2}
            {:field "spatial-ability-question-4", :value 3}
            {:field "spatial-ability-question-5", :value 3}
            {:field "spatial-ability-question-6", :value 3}
            {:field "spatial-ability-question-7", :value 1}
            {:field "spatial-ability-question-8", :value 5}
            {:field "spatial-ability-question-9", :value 2}
            {:field "spatial-ability-question-10", :value 3}
            {:field "spatial-ability-question-11", :value 4}
            {:field "spatial-ability-question-12", :value 5}
            {:field "spatial-ability-question-13", :value 6}
            {:field "spatial-ability-question-14", :value 5}
            {:field "spatial-ability-question-15", :value 4}
            {:field "spatial-ability-aggregate-score", :value 52}
            {:field "satisficing-question-1", :value 2}
            {:field "satisficing-question-2", :value 1}
            {:field "satisficing-question-3", :value 2}
            {:field "satisficing-question-4", :value 2}
            {:field "satisficing-question-5", :value 2}
            {:field "satisficing-question-6", :value 2}
            {:field "satisficing-question-7", :value 1}
            {:field "satisficing-question-8", :value 3}
            {:field "satisficing-question-9", :value "-99"}
            {:field "satisficing-question-10", :value 2}
            {:field "satisficing-aggregate-score", :value 20}
            {:field "subject-id", :value "subject_id_000005"}],
           :foo 7})

(def msg5 {:mission-id "mission114",
           :routing-key "observations",
           :app-id "RITAControlPanel",
           :plant-id "plant",
           :state "observations",
           :observations
           [{:field "spatial-ability-question-1", :value 4}
            {:field "spatial-ability-question-2", :value 2}
            {:field "spatial-ability-question-3", :value 2}
            {:field "spatial-ability-question-4", :value 3}
            {:field "spatial-ability-question-5", :value 3}
            {:field "spatial-ability-question-6", :value 3}
            {:field "spatial-ability-question-7", :value 1}
            {:field "spatial-ability-question-8", :value 5}
            {:field "spatial-ability-question-9", :value 2}
            {:field "spatial-ability-question-10", :value 3}
            {:field "spatial-ability-question-11", :value 4}
            {:field "spatial-ability-question-12", :value 5}
            {:field "spatial-ability-question-13", :value 6}
            {:field "spatial-ability-question-14", :value 5}
            {:field "spatial-ability-question-15", :value 4}
            {:field "spatial-ability-aggregate-score", :value 52}
            {:field "satisficing-question-1", :value 2}
            {:field "satisficing-question-2", :value 1}
            {:field "satisficing-question-3", :value 2}
            {:field "satisficing-question-4", :value 2}
            {:field "satisficing-question-5", :value 2}
            {:field "satisficing-question-6", :value 2}
            {:field "satisficing-question-7", :value 1}
            {:field "satisficing-question-8", :value 3}
            {:field "satisficing-question-9", :value "-99"}
            {:field "satisficing-question-10", :value 2}
            {:field "satisficing-aggregate-score", :value 20}
            {:field "subject-id", :value "subject_id_000005"}]})
