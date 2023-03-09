(ns mbroker.asist-msg
  (:require [rita.common.config :as config]
            [clojurewerkz.machine-head.client :as mqtt]
            [clojure.pprint :refer [pprint]]
            [clojure.data.json :as json])
  (:import (java.time Instant)))

(defonce header-version "0.1")
(defonce msg-version "0.1")

;; Helper fns to support create messages sent to MQTT

(defn- make-tb-timestamp [millis]
  (str (Instant/ofEpochMilli millis)))

(defn- make-header [ts msg-type]
  {:timestamp    ts
   :message_type msg-type
   :version      header-version})

(defn- make-msg-object [ts source sub-type version trial-id exp-id]
  {:timestamp     ts
   :source        source
   :sub_type      sub-type
   :version       msg-version
   :rita-version  version
   :trial_id      trial-id
   :experiment_id exp-id})

(defn make-header-and-msg [ts-millis msg-type sub-type trial-id exp-id]
  (let [tb-time (make-tb-timestamp ts-millis)]
    {:header (make-header tb-time msg-type)
     :msg    (make-msg-object tb-time (config/get-agent-name) sub-type
                              (config/get-rita-version) trial-id exp-id)}))

(defn publish-asist-message-to-mqtt [msg topic mqtt-connection]
  ; publish with mq-qos 0 => fire and forget. Don't cache. If no listeners, drop it.
  (if mqtt-connection
    (mqtt/publish mqtt-connection
                  topic
                  (json/write-str msg)
                  0                                         ;mq-qos
                  false)                                    ;retain

    (println "MQTT Connection is nil")))

(defn publish-intervention-chat-msg-mqtt [msg mqtt-connection]
  (publish-asist-message-to-mqtt msg
                                 (str "agent/intervention/" (config/get-agent-name) "/chat")
                                 mqtt-connection))

(defn make-intervention-chat-message [addressed-to-v chat-text duration explanation elapsed_milliseconds hdr-time-millis trial-id exp-id]
  (let [hdr-and-msg (make-header-and-msg hdr-time-millis "agent" "Intervention:Chat" trial-id exp-id)]
    (merge hdr-and-msg {:data {:id                   (str (java.util.UUID/randomUUID))
                               :agent                (config/get-agent-name)
                               :created              (get-in hdr-and-msg [:header :timestamp])
                               :start                -1
                               :duration             (or duration 20000) ;assumed for now
                               :explanation          (or explanation {})
                               :receivers            addressed-to-v
                               :content              chat-text
                               :type                 "string"
                               :renderers            ["Minecraft_Chat" "Client_Map"]
                               :elapsed_milliseconds elapsed_milliseconds}})))

(defn test-make-chat-message []
  (make-intervention-chat-message ["Player420"] "I am in 114 Waltham" 100000 {} 1000 (System/currentTimeMillis) "t1" "e1"))

(defn make-version-message [hdr-time-millis trial-id exp-id]
  (let [hdr-and-msg (make-header-and-msg hdr-time-millis "agent" "versioninfo" trial-id exp-id)]
    (merge hdr-and-msg {:data {:agent_name   (config/get-agent-name)
                               :version      (config/get-rita-version)
                               :owner        "Dollabs"
                               :config       []
                               :source       ["private git repo for now"]
                               :dependencies []
                               :publishes    []
                               :subscribes   []}})))

(defn make-roll-call-request-message [roll-call-id hdr-time-millis trial-id exp-id]
  (let [hdr-and-msg (make-header-and-msg hdr-time-millis "agent" "rollcall:request" trial-id exp-id)]
    (merge hdr-and-msg {:data {:rollcall_id roll-call-id}})))

(defn publish-roll-call-request [roll-call-id hdr-time-millis trial-id exp-id mqtt-connection]
  (publish-asist-message-to-mqtt (make-roll-call-request-message roll-call-id hdr-time-millis trial-id exp-id)
                                 "agent/control/rollcall/request"
                                 mqtt-connection))

; status can be One of ["initializing" | "up" | "unknown" | "down"]
(defn make-roll-call-response-message [roll-call-id status uptime hdr-time-millis trial-id exp-id]
  (let [hdr-and-msg (make-header-and-msg hdr-time-millis "agent" "rollcall:response" trial-id exp-id)]
    (merge hdr-and-msg {:data {:rollcall_id roll-call-id
                               :version     (config/get-rita-version)
                               :status      status
                               :uptime      uptime}})))

(defn publish-roll-call-response [roll-call-id status uptime hdr-time-millis trial-id exp-id mqtt-connection]
  (publish-asist-message-to-mqtt (make-roll-call-response-message roll-call-id status uptime hdr-time-millis trial-id exp-id)
                                 "agent/control/rollcall/response"
                                 mqtt-connection))

;See MessageSpecs/Status/status.md
; msg.state	| string	| The basic state of the component. One of ["ok", “info”, “warn”, “error”, “fail”]
(defn make-status-message [state sub_type details active? hdr-time-millis trial-id exp-id]
  (let [hdr-and-msg (make-header-and-msg hdr-time-millis "status" sub_type trial-id exp-id)]
    #_{:msg (conj (:msg hdr-and-msg) {:state state}
                  (if details {:status details} {})
                  (if (nil? active?) {} {:active active?}))}
    (merge hdr-and-msg {:data (conj {:state state}
                                    (if (nil? active?) {} {:active active?})
                                    (if details {:status details} {}))})))

(defn make-heartbeat-message [hdr-time-millis trial-id exp-id]
  (make-status-message :ok "heartbeat" "I am processing messages" true hdr-time-millis trial-id exp-id))

(defn publish-heartbeat-message [hdr-time-millis trial-id exp-id mqtt-connection]
  (publish-asist-message-to-mqtt (make-heartbeat-message hdr-time-millis trial-id exp-id)
                                 (str "status/" (config/get-agent-name) "/heartbeats")
                                 mqtt-connection))

; https://gitlab.asist.aptima.com/asist/testbed/-/blob/develop/MessageSpecs/Agent/Prediction/agent_prediction._message.md
; asist action prediction in asist format
(defn- make-action-prediction [unique-id start_elapsed_time duration predicted-property
                               action using subject object probability-type probability
                               confidence-type confidence explanation]
  {:unique_id          unique-id
   :start_elapsed_time start_elapsed_time
   :duration           duration
   :predicted_property predicted-property
   :action             action
   :using              using
   :subject            subject
   :object             object
   :probability_type   probability-type
   :probability        probability
   :confidence_type    (or confidence-type "none")
   :confidence         (or confidence 0)
   :explanation        (or explanation {})})

; asist state prediction in asist format
(defn- make-state-prediction [unique-id start_elapsed_time duration subject_type subject predicted-property prediction
                              probability-type probability confidence-type confidence explanation]
  {:unique_id          unique-id
   :start_elapsed_time start_elapsed_time
   :duration           duration
   :subject_type       subject_type
   :subject            subject
   :predicted_property predicted-property
   :prediction         prediction
   :probability_type   probability-type
   :probability        probability
   :confidence_type    (or confidence-type "none")
   :confidence         (or confidence 0)
   :explanation        (or explanation {})})

; https://coveooss.github.io/json-schema-for-humans/#/
;   * generate-schema-doc generates good documentation for schema.

; There are multiple timestamps that need to be added at appropriate places.
; data.created_elapsed_time, number, The time the prediction was created by the agent based on the trial elapsed time

; data.group.start_elapsed_time, , The time the prediction group is effective in trial elapsed time.
;  if this field is null the prediction will be effective as soon as it is published.
; -- We will set this value to nil. This field is required.

; data.predictions[n].start_elapsed_time, number , the time the prediction is made. This should use trial elapsed time.
; Ex trial elapsed_milliseconds time that the prediction was made.  For M1,
; Should be at 4 (240000ms), 9 (540000ms) and 14 (840000ms) minutes

; Agent prediction message can be either `action` or `state`
; msg is of type RMQ prediction message
(defn- make-data-msg-action [msg start-elapsed-time predicted-property]
  (let [pred (:predictions msg)

        {unique_id            :uid
         created_elapsed_time :elapsed_milliseconds
         action               :action
         using                :using
         subject              :subject
         object               :object
         probability          :agent-belief
         explanation          :reason} pred

        duration (- (second (:bounds pred)) (first (:bounds pred)))
        asist-pred (make-action-prediction unique_id start-elapsed-time duration predicted-property
                                           action using subject object "float" probability nil nil explanation)]
    {:created_elapsed_time created_elapsed_time
     :group                {:start_elapsed_time nil
                            :duration           duration
                            :explanation        explanation}
     :predictions          [asist-pred]}))


(defn- make-data-msg-state [msg start-elapsed-time subject-type team-or-individual predicted-property prediction]
  (let [pred (:predictions msg)

        {unique_id            :uid
         created_elapsed_time :elapsed_milliseconds
         probability          :agent-belief
         explanation          :reason} pred
        subject team-or-individual
        duration (- (second (:bounds pred)) (first (:bounds pred)))
        asist-pred (make-state-prediction unique_id start-elapsed-time duration subject-type subject predicted-property prediction
                                          "float" probability nil nil explanation)]
    {:created_elapsed_time created_elapsed_time
     :group                {:start_elapsed_time nil         ; The time the prediction group is effective in trial elapsed time. if this field is null the prediction will be effective as soon as it is published.
                            :duration           duration
                            :explanation        explanation}
     :predictions          [asist-pred]}))

(defn make-prediction-action-msg [msg hdr-time exp-id pred-start-elapsed-time predicted-property]
  (let [tb-time (make-tb-timestamp hdr-time)
        version (config/get-rita-version)
        hdr (make-header tb-time "agent")
        asist-msg (make-msg-object tb-time (config/get-agent-name) "Prediction:Action"
                                   version (:mission-id msg) exp-id)
        data (make-data-msg-action msg pred-start-elapsed-time predicted-property)]
    {:header hdr :msg asist-msg :data data}))

(defn make-prediction-state-msg [msg hdr-time exp-id team-or-individual pred-start-elapsed-time subject-type predicted-property prediction]
  (let [tb-time (make-tb-timestamp hdr-time)
        version (config/get-rita-version)
        hdr (make-header tb-time "agent")
        asist-msg (make-msg-object tb-time (config/get-agent-name) "Prediction:State"
                                   version (:mission-id msg) exp-id)
        data (make-data-msg-state msg pred-start-elapsed-time subject-type team-or-individual predicted-property prediction)]
    {:header hdr :msg asist-msg :data data}))

(def rita-pred-state-template-study-2 {:timestamp   -1
                                       :mission-id  "12345abcd"
                                       :predictions {:uid                  "uid-1001"
                                                     :action               "some action"
                                                     :using                "using what"
                                                     :subject              "subject goes here" ; required in some cases only.
                                                     :object               "object value"
                                                     :bounds               [0 50] ; Optional for study 2
                                                     :elapsed_milliseconds 805293.0
                                                     :reason               {:f1 "reason/explanation"}
                                                     :agent-belief         1.0}}) ;"agent-belief optional field for study 2"}})


(defn test-make-study-2-m1-prediction-state []
  ; "TM-000188" ;string-quoted team id such as "TM-000188"
  ; "team_performance"
  ; "750"
  (make-prediction-state-msg rita-pred-state-template-study-2 (System/currentTimeMillis) "test-exp-id" "TM-000188"
                             10000 "team" "M1:team_performance" 800))


(defn final-score-to-m1 [prediction-msg hdr-time exp-id team-id pred-start-elapsed-time]
  (let [pred (:predictions prediction-msg)]
    (make-prediction-state-msg prediction-msg hdr-time exp-id team-id
                               pred-start-elapsed-time "team" "M1:team_performance" (:object pred))))

(defn test-make-study-2-m3-prediction-state []
  (make-prediction-state-msg rita-pred-state-template-study-2 (System/currentTimeMillis) "test-exp-id"
                             "E000101" 10000 "individual" "M3:participant_map" "SaturnA_24"))

(defn rita-m3-to-asist-m3 [prediction-msg hdr-time exp-id pred-start-elapsed-time]
  (let [participants (get-in prediction-msg [:predictions :object])]
    (map (fn [participant]
           (make-prediction-state-msg prediction-msg hdr-time exp-id (:participant_id participant)
                                      pred-start-elapsed-time "individual" "M3:participant_map" (:map participant)))
         participants)))

(defn rita-m6-to-asist-m6 [prediction-msg hdr-time exp-id pred-start-elapsed-time]
  (let [participants (get-in prediction-msg [:predictions :object])]
    (map (fn [participant]
           (make-prediction-state-msg prediction-msg hdr-time exp-id (:participant_id participant)
                                      pred-start-elapsed-time "individual" "M6:participant_block_legend" (:markerblocks participant)))
         participants)))

(defn test-make-study-2-m6-prediction-state []
  (make-prediction-state-msg rita-pred-state-template-study-2 (System/currentTimeMillis) "test-exp-id" "E000101"
                             10000 "individual" "M6:participant_block_legend" "B_Sally"))

(defn test-make-study-2-m7-prediction-action []
  (make-prediction-action-msg rita-pred-state-template-study-2 (System/currentTimeMillis) "test-exp-id"
                              10000 "M7:participant_room_enter"))

(defn rita-m7-to-asist-m7 [prediction-msg hdr-time exp-id]
  (make-prediction-action-msg prediction-msg hdr-time exp-id
                              (get-in prediction-msg [:predictions :elapsed_milliseconds])
                              "M7:participant_room_enter"))

(def sample-study-2 {:m1 (test-make-study-2-m1-prediction-state)
                     :m3 (test-make-study-2-m3-prediction-state)
                     :m6 (test-make-study-2-m6-prediction-state)
                     :m7 (test-make-study-2-m7-prediction-action)})
; Study-2 prediction message details.
; https://docs.google.com/document/d/1tKOmGW6Xf42VmKi2WUYHFm8DQKz4W_3OSARDsVactIQ/edit#heading=h.cnm8qrshztlu

; Our prediction message format
{:predictions {:hypothesis-rank 0,
               :uid             "se17096",
               :agent-belief    0.8124999992433004,
               :state           true,
               :bounds          [0 500],
               :reason          ["and" {:agent-belief 1.0, :wants-to-visit "all-rooms", :subject "ASIST5"}],
               :hypothesis-id   "hyp0001-pending",
               :action          "next-room-to-visit",
               :subject         "ASIST5",
               :object          "/Falcon.Room107"},
 :app-id      "StateEstimation",
 :routing-key "predictions",
 :timestamp   1615235041999,
 :mission-id  "4a8bcb7f-39e1-4124-94a6-51f962fcb242",
 :_id         "4a8bcb7f-39e1-4124-94a6-51f962fcb242-se17096"}

;;; Example message. See TESTBED/MessageSpecs/Agent for details and updates
{:header {:timestamp "2019-12-26T14:05:02.3412Z", :message_type "agent", :version "0.1"},
 :msg    {:trial_id       "123e4567-e89b-12d3-a456-426655440000",
          :timestamp      "2019-12-26T14:05:02.1412Z",
          :replay_id      "a44120f7-b5ba-47a3-8a21-0c76ede62f75",
          :replay_root_id "123e4567-e89b-12d3-a456-426655440000",
          :source         "tom_generator:1.0",
          :sub_type       "prediction:action",
          :version        "0.1"},
 :data   {:created    "2019-12-26T14:05:02.3412Z",
          :group      {:start "2019-12-26T14:05:02.3412Z", :timeout "50", :explanation {:agent-custom-json-object> ""}},
          :prediction [{:action           "<action type>",
                        :using            "<item to be used>",
                        :subject          "<who is taking the action>",
                        :object           "<who/what is being acted upon>",
                        :probability_type "percent",
                        :probability      0.9,
                        :explanation      {:agent-custom-json-object> ""}}
                       {:action           "<action type>",
                        :using            "<item to be used>",
                        :subject          "<who is taking the action>",
                        :object           "<who/what is being acted upon>",
                        :probability_type "string",
                        :probability      "low",
                        :explanation      {:agent-custom-json-object> ""}}]}}

; (def sv (v/validator slurp "/Users/prakash/projects/asist-rita/git/testbed/MessageSpecs/Agent/Prediction/agent_prediction_message.json"))

;; from Gdrive and stale.
; data.created -- string -- The time the prediction was created by the agent based on the trial time
; data.group.start -- string -- The time the prediction is effective
; data.group.duration -- number -- The length of time in seconds the prediction remains valid
; data.group.explanation -- json object -- an agent custom json object to describe why this prediction was generated

; Action message
; data.predictions[n].unique_id
; data.predictions[n].start -- the time the prediction becomes valid. If this field is left as null, the prediction will be effective immediately. This is useful if the prediction is made before the mission effectively starts
; data.predictions[n].duration -- The duration is seconds that the prediction remains valid. If this field is left null, the prediction will be valid for the full trial run
; data.predictions[n].action -- the type of action that is being predicted
; data.predictions[n].using -- the tool that is used in the action
; data.predictions[n].subject -- the entity taking the action
; data.predictions[n].object -- who or what is being acted upon
; data.predictions[n].probability_type -- string or float for the data type of probability
; data.predictions[n].probability --
; data.predictions[n].confidence_type -- string or float for the data type of probability
; data.predictions[n].confidence --
; data.predictions[n].explanation -- an explanation of the the prediction

; State message
; data.predictions[n].unique_id
; data.predictions[n].start -- the time the prediction becomes valid. If this field is left as null, the prediction will be effective immediately. This is useful if the prediction is made before the mission effectively starts
; data.predictions[n].duration -- The duration is seconds that the prediction remains valid. If this field is left null, the prediction will be valid for the full trial run
; data.predictions[n].subject -- the entity taking the action
; data.predictions[n].predicted_property -- the discrete property for which the prediction is made -> for a player this could be position or score, for a victim this could be poisition or triage state, for a team this could be the team score
; data.predictions[n].prediction -- the actual predicted value for the subject property in question -> ie 770 for a team score, successful triage for a victim, Location -2100 60 59 for player location
; data.predictions[n].probability_type -- string or float for the data type of probability
; data.predictions[n].probability --
; data.predictions[n].confidence_type -- string or float for the data type of probability
; data.predictions[n].confidence --
; data.predictions[n].explanation -- an explanation of the the prediction


; Player Strength message
; Corresponding RMQ fn to be called from cli or wherever core is accessible
; (publish-message (get-channel) "mission-id or trial-id" "player-strength" (asist-msg/make-player-strength :participant-123 456))
(defn make-player-strength [participant_id strength]
  {:player-strength strength
   :participant_id  participant_id})

(defn make-player-strength-asist [participant_id strength ts-millis trial-id exp-id]
  (let [hdr-msg (make-header-and-msg ts-millis "agent" "player_strength" trial-id exp-id)]
    (merge hdr-msg {:data (make-player-strength participant_id strength)})))

(defn publish-player-strength [participant_id strength ts-millis trial-id exp-id mqtt-connection]
  (publish-asist-message-to-mqtt (make-player-strength-asist participant_id strength
                                                             ts-millis trial-id exp-id)
                                 (str "agent/" (config/get-agent-name) "/player_strength")
                                 mqtt-connection))

(defn make-rmq-to-mqtt-msg [ts-millis sub_type trial-id exp-id data]
  ; msg-type sub-type
  {:topic   sub_type                                        ;rita_anomaly
   :message (merge (make-header-and-msg ts-millis "agent" sub_type trial-id exp-id)
                   {:data data})})

(defn test-make-rmq-to-mqtt-msg []
  (make-rmq-to-mqtt-msg (System/currentTimeMillis)
                        "rita_anomaly" "t1234" "e1234" {:anomaly  "Missing Participant ID in client_info",
                                                        :callsign "Red",
                                                        :role     "Medical_Specialist"}))