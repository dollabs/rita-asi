;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.prediction-generator.cli
  "RITA PredictionGenerator main."
  (:require [rita.prediction-generator.mdb :as db]
            [rita.prediction-generator.probability :as pb]
            [rita.common.core :refer :all]
            [mbroker.asist-msg :as asist-msg]
            [pamela.tools.utils.timer :as pt-timer]

            [taoensso.timbre :as timbre]

            [environ.core :refer [env]]

            [clojure.pprint :refer [pprint]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [rita.common.config :as config])
  (:gen-class))                                             ;; required for uberjar

;;;;;;; Forward declarations
(declare update-prediction-state-change)

(declare forward-to-mqtt)

(declare init-prediction-probability-from-resources)

(declare handle-testbed-message)

(declare handle-m7)

(declare handle-mqtt-message)
;;;;;;; Class init
(timbre/merge-config! {:level :warn})                       ; Silence mongodb driver

;;;;;;; defs ;;;;;;;
; To be loaded once at startup time and cache.
(defonce prediction-probability-db {})
(defonce prediction-probability {})
(defonce default-timeout 5)
; agent/prediction/<prediction_type>/<unique_agent_name>
; TODO Review action and state predictions and ensure we send our predictions to appropriate topics
(defonce mqtt-predictions-topic (str "agent/prediction/action/" (config/get-agent-name)))
(defonce mqtt-versioninfo-topic (str "agent/" (config/get-agent-name) "/versioninfo"))
(defonce mission-start-msg nil)
(defonce mission-stop-msg nil)
(defonce study-2-predictions (atom {}))
; start time needs to be in seconds in support of roll call message
; It needs to be real time and not based on header-clock-time as we won't know when we will receive header clock messages.
(defonce agent-start-time (System/currentTimeMillis))

;;;;;;; defns ;;;;;;;

(defn get-experiment-id []
  (if mission-start-msg
    (get-in mission-start-msg [:testbed-message :msg :experiment_id])
    "No-experiment-id"))

(defn get-trial-id []
  (if mission-start-msg
    (get-in mission-start-msg [:testbed-message :msg :trial_id])
    "No-trial-id"))

(defn get-uptime []
  (- (System/currentTimeMillis) agent-start-time))

(defn reset-study-2-predictions []
  (reset! study-2-predictions {}))

(defn cache-final-score [msg]
  (let [score-val (get-in msg [:predictions :object])]
    (when score-val (swap! study-2-predictions assoc :final-score msg))
    (when-not score-val (println "Warning object of final score is nil"))))

(defn cache-m3-msg [msg]
  (swap! study-2-predictions assoc :m3 msg))

(defn cache-m6-msg [msg]
  (swap! study-2-predictions assoc :m6 msg))


(defn init-prediction-probability [data]
  (def prediction-probability (reduce (fn [res [id {prob :probability}]]
                                        (conj res {id (pb/make-probability-vector-from (select-keys prob [:true :false]))}))
                                      {} data)))

(defn init-prediction-probability-from-db []
  (def prediction-probability-db (reduce (fn [res x]
                                           (conj res {(keyword (:_id x)) x}))
                                         {} (db/get-probability)))
  (init-prediction-probability prediction-probability-db))

(defn print-probabilities []
  (doseq [[id {prob :probability}] prediction-probability-db]
    (println id "\n" prob)
    #_(println "counts" (pb/count-elements (get prediction-probability id)))))

(defn get-prediction-state [uid]
  (let [x (db/find-by-id uid)]
    ;(println "prediction in db")
    ;(pprint x)
    (if x (get-in x [:predictions :state]))))

(defn insert-prediction [uid msg]
  (let [given-bounds (get-in msg [:predictions :bounds])
        def-bounds [0 default-timeout]
        bnds (or given-bounds def-bounds)
        msg (assoc-in msg [:predictions :bounds] bnds)
        at-time (pt-timer/get-unix-time)]

    (pt-timer/schedule-task (fn []
                              (println "Prediction timed out " uid #_"at" #_(pt-timer/get-unix-time) "after" (- (pt-timer/get-unix-time) at-time))
                              (update-prediction-state-change uid msg (get-prediction-state uid) false))
                            ; timeout value is in seconds
                            (* 1000 (second bnds)))

    (println "----- inserting prediction" uid)
    (if-not given-bounds (println "Insert prediction. given bounds are nil. using defaults" def-bounds))
    (db/insert-one (merge msg {:_id uid}))))

(defn update-prediction-state-db [uid state]
  (let [db-obj (db/find-by-id uid)
        up-obj (assoc-in db-obj [:predictions :state] state)]
    ;(pprint up-obj)
    (db/update-one up-obj)
    up-obj))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Prediction state changes as follows:
; nil (not in DB) -> "unknown" (Sent by StateEstimation or any other component)
; "unknown" (in DB) -> True (Sent by SE or ..)
; "unknown" (in DB) -> False (Timeout) Only this is published.
(defn update-prediction-state-change [uid msg prev-state new-state]
  ;(println "update-prediction")
  ;(pprint msg)
  (println "----- update prediction." uid ":" prev-state "->" new-state #_(pt-timer/get-unix-time))
  (cond (and (nil? prev-state) (= "unknown" new-state))
        (do (insert-prediction uid msg)
            (forward-to-mqtt msg))

        (and (= "unknown" prev-state) (= true new-state))
        (update-prediction-state-db uid new-state)

        ; if the component is delayed in producing prediction to be true,
        ; we update it.
        (and (= false prev-state) (= true new-state))
        (do
          (println "updating delayed prediction" uid prev-state "->" new-state)
          (update-prediction-state-db uid new-state))

        (and (= "unknown" prev-state) (= false new-state))
        (let [upd-msg (update-prediction-state-db uid new-state)]
          (println "publishing:" uid new-state)
          (publish-message (get-channel) (:mission-id msg) "predictions" {:predictions (:predictions upd-msg)}))

        ; when we publish failed message, we get the same message back. ignore
        ; when component says prediction succeeded and timer went of. ignore
        (or (and (= false prev-state) (= false new-state))  ;
            (and (= true prev-state) (= false new-state)))
        (println "ignore state change:" uid ":" prev-state "->" new-state)

        :else
        (println "prediction state change not allowed." uid ":" prev-state "->" new-state)))

(defn make-db-uid [mission-id pred-id]
  (str mission-id "-" pred-id))

(defn make-prediction-id [app-id action]
  (str app-id "--" action))

(defn handle-study-2-data [msg]
  (let [action (get-in msg [:predictions :action])
        app-id (get msg :app-id)]
    (when (= "StateEstimation" app-id)
      (when (= action "final-score")
        (println "Got final score" (get-in msg [:predictions :object]) (pt-timer/as-str))
        (pprint msg)
        (cache-final-score msg))

      (when (= action "m3")
        (println "Got M3 message" (pt-timer/as-str))
        (pprint msg)
        (cache-m3-msg msg))

      (when (= action "m6")
        (println "Got M6 message" (pt-timer/as-str))
        (pprint msg)
        (cache-m6-msg msg))

      (when (or (= action "m7_will_enter_room") (= action "m7_will_not_enter_room"))
        (println "Got M7 message" (pt-timer/as-str))
        (pprint msg)
        (handle-m7 msg)))))

(defn handle-prediction [msg]
  (let [{:keys [predictions mission-id]} msg
        uid (make-db-uid mission-id (:uid predictions))
        our-p-state (get-prediction-state uid)
        new-p-state (get-in msg [:predictions :state])]
    (handle-study-2-data msg)
    (update-prediction-state-change uid msg our-p-state new-p-state)))


(defn analyse-predictions [preds]
  (let [
        ; group predictions by app-id and action
        grouped (group-by (fn [input]
                            (make-prediction-id (:app-id input) (get-in input [:predictions :action]))) preds)
        ; for each group, count success and failure
        tfcounts (map (fn [[k v]]
                        (let [grouptf (group-by (fn [vv]
                                                  (str (get-in vv [:predictions :state])))
                                                v)]
                          [k (reduce (fn [res [k v]]
                                       (conj res {k (count v)})) {} grouptf)]))
                      grouped)
        tfcounts (reduce (fn [res [k v]]
                           (let [totals (apply + (vals v))]
                             (conj res {k {:counts      v
                                           :totals      totals
                                           :probability (reduce (fn [res [k v]]
                                                                  (conj res {k (double (/ v totals))}))
                                                                {} v)}})))
                         {} tfcounts)]
    tfcounts))

(defn analyse-predictions-aggregate [preds]
  (let [grouped (group-by (fn [x]
                            (get-in x [:predictions :state])) preds)
        total (count preds)
        stats (reduce (fn [result [k v]]
                        (conj result {k (count v)})) {} grouped)]
    {:total     total
     :breakdown stats}))

(defn update-probabilities-in-db []
  (db/update-probability (analyse-predictions (db/get-predictions))))

(defn handle-mission-ended [mission-id]
  (let [all-pred (db/get-predictions)
        aggr-stats-detail (analyse-predictions all-pred)
        mission-pred (db/get-predictions-by-mission-id mission-id)
        mission-stats-detail (analyse-predictions mission-pred)
        aggr-stats (analyse-predictions-aggregate all-pred)
        mission-stats (analyse-predictions-aggregate mission-pred)
        data {:unhandled-timeouts     (count @pt-timer/call-backs)
              :mission-stats          mission-stats
              :mission-stats-detail   mission-stats-detail
              :aggregate-stats        aggr-stats
              :aggregate-stats-detail aggr-stats-detail}]
    (publish-message (get-channel) mission-id "prediction-stats" {:prediction-stats data})
    (update-probabilities-in-db)))



;;;;;;; Conditions ;;;;;;;
(defcondition startup-rita-observed [startup-rita] [ch]
              (let [mission-id (:mission-id startup-rita)]
                (println "Startup-RITA message received for mission" mission-id startup-rita)))

(defcondition predictions-received [predictions] [ch]
              ;(println "got prediction")
              ;(pprint predictions)
              (handle-prediction predictions))

(defcondition clock-message [clock] [ch]
              ; (println "clock message received: " clock)
              ;(pprint clock)
              (pt-timer/update-clock (:timestamp clock)))

(defcondition rita-player-ended [rita-player-ended] [ch]
              (println "Rita Player finished")
              (pprint rita-player-ended)
              (handle-mission-ended (:mission-id rita-player-ended)))

(defcondition testbed-message [testbed-message] [ch]
              (handle-testbed-message testbed-message))

(defcondition mqtt-message [mqtt-message] [ch]
              (handle-mqtt-message mqtt-message))

(defn make-topic [topic]
  (str "agent/" (config/get-agent-name) "/" topic))

(defn handle-mqtt-message [mqtt-message]
  ;(print "mqtt-message")
  ;(pprint mqtt-message)
  (let [envelope (:mqtt-message mqtt-message)
        {the-message :message
         topic       :topic} envelope
        topic (or topic "unknown_topic")
        final-topic (make-topic topic)]
    (println "got mqtt-message for topic" topic "->" final-topic)
    (pprint the-message)
    (asist-msg/publish-asist-message-to-mqtt the-message final-topic @mqtt-connection)
    ))

(defn init []
  ;(println "mdb host" (get-mdb-host))
  (pt-timer/set-use-sim-time (get-sim-clock))
  (db/make-connection :host (get-mdb-host))
  (make-mqtt-connection)
  #_(init-prediction-probability-from-db)
  (init-prediction-probability-from-resources))

(defmain -main 'PredictionGenerator
         ;;Optionally add some initialization code here
         (init))

;; Other useful functions to be used later.


(defn forward-to-mqtt [msg]
  (let [subject (get-in msg [:predictions :subject])
        action (get-in msg [:predictions :action])
        object (get-in msg [:predictions :object])
        using (get-in msg [:predictions :using])
        pred-start-elapsed-time (get-in msg [:predictions :elapsed_milliseconds])
        pred-id (keyword (make-prediction-id (:app-id msg) action))
        forward? (= :true (pb/yay-or-nay (get prediction-probability pred-id)))]
    ; TODO when the forward is true, we should publish to mqtt
    (println "Got action" pred-id
             "\nprob of success" (get-in prediction-probability-db [pred-id :probability])
             "\nTo mqtt forward? " forward? subject "->" action object using)
    (if (and action forward?)
      ; DEBUG ME FIXME
      (let [msg (asist-msg/make-prediction-action-msg msg
                                                      (pt-timer/get-unix-time)
                                                      (get-experiment-id)
                                                      pred-start-elapsed-time
                                                      action)]
        (asist-msg/publish-asist-message-to-mqtt msg mqtt-predictions-topic @mqtt-connection)))))

(defn write-probability-file [json_f]
  (with-open [x (clojure.java.io/writer json_f)]
    (binding [*out* x]
      (json/pprint prediction-probability-db))))

(defn read-probability-file [json_f]
  (with-open [rdr (clojure.java.io/reader json_f)]
    (json/read rdr
               :key-fn #(keyword %))))

(defn init-prediction-probability-from-resources []
  (let [fname "prediciton-probability.json"
        fname (io/resource (str "public/" fname))]
    (def prediction-probability-db (read-probability-file fname))
    (init-prediction-probability prediction-probability-db)))

(defn minutes-to-millis [avec]
  (into [] (map #(* 60 1000 %1) avec)))

; intervals are in millis
(def study-2-intervals {:m1 (minutes-to-millis [4 9 14])
                        :m3 (minutes-to-millis [2 7 12])
                        :m6 (minutes-to-millis [3 8 13])
                        ; FIXME m7 is temporary and for testing.
                        #_:m7 #_[5000 15000]})

(defn handle-m1 [timeout-val]
  (println "publish study-2 m1" timeout-val (pt-timer/as-str))
  (if-not (:final-score @study-2-predictions)
    (println "rita prediction final-score is nil for timer" (/ timeout-val (* 1000 60)) "minutes"))
  (let [final-score-msg (asist-msg/final-score-to-m1 (:final-score @study-2-predictions)
                                                     (pt-timer/get-unix-time)
                                                     (get-experiment-id)
                                                     (get-in mission-start-msg [:testbed-message :data :experiment_name])
                                                     timeout-val)]
    (println "asist-prediction-final-score," (json/write-str final-score-msg))))


(defn handle-m3 [timeout-val]
  (println "publish study-2 m3" timeout-val (pt-timer/as-str))
  (if-not (:m3 @study-2-predictions)
    (println "rita prediction m3 is nil for timer" (/ timeout-val (* 1000 60)) "minutes"))
  (pprint @study-2-predictions)
  (let [m3-msgs (asist-msg/rita-m3-to-asist-m3 (:m3 @study-2-predictions)
                                               (pt-timer/get-unix-time)
                                               (get-experiment-id)
                                               timeout-val)]
    (doseq [m3-msg m3-msgs]
      (println "asist-prediction-m3-message," (json/write-str m3-msg)))))

(defn handle-m6 [timeout-val]
  (println "publish study-2 m6" timeout-val (pt-timer/as-str))
  (if-not (:m6 @study-2-predictions)
    (println "rita prediction m6 is nil for timer" (/ timeout-val (* 1000 60)) "minutes"))
  (let [m6-msgs (asist-msg/rita-m6-to-asist-m6 (:m6 @study-2-predictions)
                                               (pt-timer/get-unix-time)
                                               (get-experiment-id)
                                               timeout-val)]
    (doseq [m6-msg m6-msgs]
      (println "asist-prediction-m6-message," (json/write-str m6-msg)))))

(defn handle-m7 [msg]
  (println "publish study-2 m7" (pt-timer/get-unix-time))
  (let [action (get-in msg [:predictions :action])
        action (cond (= action "m7_will_enter_room")
                     "will_enter_room"
                     (= action "m7_will_not_enter_room")
                     "will_not_enter_room")
        msg (assoc-in msg [:predictions :action] action)
        m7-msg (asist-msg/rita-m7-to-asist-m7 msg
                                              (pt-timer/get-unix-time)
                                              (get-experiment-id))]
    (println "asist-prediction-m7-message," (json/write-str m7-msg))))

(defn schedule-study-2-timers []
  (println "scheduling study-2 timers" (pt-timer/as-str))
  (doseq [tme (:m1 study-2-intervals)]
    (pt-timer/schedule-task (fn []
                              (handle-m1 tme)) tme))
  (doseq [tme (:m3 study-2-intervals)]
    (pt-timer/schedule-task (fn []
                              (handle-m3 tme)) tme))
  (doseq [tme (:m6 study-2-intervals)]
    (pt-timer/schedule-task (fn []
                              (handle-m6 tme)) tme)))

(defn handle-testbed-message [tbm]
  (let [{{hdr :header msg :msg} :testbed-message} tbm
        message_type (:message_type hdr)
        sub_type (:sub_type msg)]
    (when (and (= message_type "trial") (= sub_type "start"))
      (println "Got trial start" (pt-timer/as-str))
      (def mission-start-msg tbm)
      (asist-msg/publish-asist-message-to-mqtt (asist-msg/make-version-message (pt-timer/get-unix-time) (get-trial-id) (get-experiment-id))
                                               mqtt-versioninfo-topic @mqtt-connection)
      (schedule-study-2-timers)
      #_(pprint tbm))
    (when (and (= message_type "agent") (= sub_type "rollcall:request"))
      (asist-msg/publish-roll-call-response (get-in tbm [:testbed-message :msg :data :rollcall_id])
                                            "up"
                                            (get-uptime)
                                            (pt-timer/get-unix-time)
                                            (get-trial-id)
                                            (get-experiment-id)
                                            @mqtt-connection))
    (when (and (= message_type "trial") (= sub_type "stop"))
      (def mission-stop-msg tbm)
      (println "Got trial stop via testbed-message." (pt-timer/as-str)))))




