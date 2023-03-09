;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.rita-player.cli
  "RITA RMQ Player"
  (:require [pamela.tools.rmq-logger.log-player :as player]
            [pamela.tools.utils.rabbitmq :as rmq]
            [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [clojure.pprint :refer :all])
  (:import
    (java.time ZoneOffset ZonedDateTime)
    (java.time.format DateTimeFormatter))
  (:gen-class))                                             ;; required for uberjar

(def app-id "RitaLogPlayer")

;(defonce mission-timer-clock-epoch 0)
(defonce mission-timer-clock-epoch-utc (ZonedDateTime/of 1970 1 1 0 0 0 0 (ZoneOffset/UTC)))
(defonce mission-timer-clock-max-value 0)
(defonce mission-timer-clock-active false)
(defonce mission-timer-value nil)                           ;; to keep track of start and stop timer-value. For debug only

(def cli-options [["-h" "--host rmqhost" "RMQ Host" :default "localhost"]
                  ["-p" "--port rmqport" "RMQ Port" :default 5672 :parse-fn #(Integer/parseInt %)]
                  ["-e" "--exchange name" "RMQ Exchange Name" :default "rita"]
                  ["-r" "--routing-key rkey" "Default routing-key is: #, when message itself does not has it" :default "#"]
                  ["-s" "--speedup speed" "Events will be dispatched `speedup` times faster" :default (player/get-speedup) :parse-fn #(Double/parseDouble %)]
                  ["-l" "--num-lines Nlines" "Number of lines to dispatch" :parse-fn #(Integer/parseInt %)]
                  ["-c" "--simulate-clock Frequency" "Will publish clock messages to rkey clock at given frequency" :parse-fn #(Integer/parseInt %) :default 0]
                  ["-?" "--help" "Print this help" :default nil]])

;;;;;;; defns ;;;;;;;

(defn get-mission-id []
  ; TODO Find start message and return mission id. or find firstnon nil trial_id and return it.
  ; TODO How will a file containing multiple missions / trials work?
  (first (first (take 1
                      (remove nil? (map (fn [[_ vec]]
                                          (map (fn [x]
                                                     (:mission-id x)) vec)) (player/get-cached-events)))))))

(defn get-int-or-nil [val]
  (try (Integer/parseInt val)
       (catch Exception _)))

(defn mission-timer-started [timer-val millis-time]
  (println "Mission Timer Started:" timer-val)
  (def mission-timer-clock-active true)
  (def mission-timer-clock-max-value millis-time)
  (def mission-timer-value timer-val))

(defn mission-timer-stopped [timer-val]
  (println "Mission Timer Stopped:" timer-val)
  (def mission-timer-clock-active false)
  ;(def mission-timer-clock-max-value 0)
  (def mission-timer-value timer-val))

(defn parse-mission-timer [val]
  (let [tokens  (str/split val #"\s*:\s*")
        minute  (get-int-or-nil (first tokens))
        seconds (get-int-or-nil (second tokens))]
    (when (and minute seconds)
      (* 1000 (+ seconds (* minute 60))))))


; To make value for tb_clock
; String utctime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
;{:tb_clock "2020-08-19T23:12:11.3527Z",
; :timestamp 1597878731352,
; :received-routing-key "clock",
; :exchange "rita",
; :routing-key "clock",
; :app-id "TestbedBusInterface"}
(defn make-mission-clock-msg [msg]
  (let [timer-val  (get-in msg [:testbed-message :data :mission_timer])
        count-down (parse-mission-timer timer-val)]
    (cond (and (false? mission-timer-clock-active)
               (and (integer? count-down) (pos? count-down)))
          (mission-timer-started timer-val count-down)
          (and (or (= 0 count-down) (nil? count-down))
               (true? mission-timer-clock-active))
          (mission-timer-stopped timer-val))

    (when count-down
      (let [ts  (- mission-timer-clock-max-value count-down)
            tso (.plusNanos mission-timer-clock-epoch-utc (* 1000000 ts))
            tso (.format tso (DateTimeFormatter/ISO_INSTANT))]

        {:timestamp   ts
         :tb_clock    (.toString tso)
         :app-id      app-id
         :routing-key "mission_clock"}))))

(defn mission-timer-clock [msg-list]
  (remove nil? (reduce (fn [res msg]
                         (cond (get-in msg [:testbed-message :data :mission_timer])
                               (into res  [(make-mission-clock-msg msg) msg])

                               :else
                               (conj res msg)))
                       [] msg-list)))


(defn -main [& args]
  (println "Rita RMQ log player")
  (let [parsed (cli/parse-opts args cli-options)
        {help              :help
         host              :host
         port              :port
         exchange          :exchange
         rkey              :routing-key
         speed             :speedup
         nlines            :num-lines
         clock-events      :simulate-clock} (:options parsed)
        [file] (:arguments parsed)]

    ;(println "\ncommand line options:")
    (pprint (:options parsed))
    ;(println "\ncommand line args:")
    (pprint (:arguments parsed))
    (println)

    (player/set-repl true)
    (player/set-speedup speed)
    (player/set-num-lines nlines)
    ; No need to create mission clock messages anymore
    ;(player/set-publish-adapter mission-timer-clock)

    (if (or help (not file))
      (do (println "Usage: rita.rita-player.core options raw-event-data-file\n where options are:")
          (println (:summary parsed))
          (System/exit 1))
      (player/play-and-wait file host port exchange rkey clock-events))
    (rmq/publish-message {:mission-id (get-mission-id)} app-id "rita-player-ended" (System/currentTimeMillis) (player/get-channel) exchange)
    (Thread/sleep 500)
    (println "Rita RMQ Player done")
    (System/exit 0)))


