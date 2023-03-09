(ns rita.rmq-validator.specs
  "Clojure specs for validation of RMQ messages"
  (:require [clojure.future :refer :all] ;;Clojure 1.9 functions
            [clojure.spec.alpha :as spec])
  (:gen-class)) ;; required for uberjar

;;;
;;; The following define each and every keyword that can appear in an RMQ message
;;;
;;; Any specs defined as 'any?' are probably incorrect and still need to be made more specific
;;;
;;; Note: it is required that the same key has the same specification across
;;;   all of the messages in which it is used.
;;;
;;; Initially, these were generated by rita.rmq-validator.cli/pprint-spec-for-all-keys
;;; If you add a message key (to rabbitmq-messages.json), you probably want to run this function
;;; and then cut-and-paste the appropriate def(s) below
;;;

;; The following are patterned after the plan-schema definitions
;; See  https://github.com/dollabs/plan-schema/blob/master/src/plan_schema/core.clj
(spec/def ::tpn-type #(= % "activity"))
(spec/def ::uid string?)
(spec/def ::constraints (spec/coll-of string? :kind vector?))
(spec/def ::end-node string?)
(spec/def ::name string?)
(spec/def ::label string?)
(spec/def ::display-name string?)
(spec/def ::sequence-label string?)
(spec/def ::sequence-end string?)
(spec/def ::cost number?)
(spec/def ::reward number?)
(spec/def ::controllable boolean?)
(spec/def ::htn-node string?)
(spec/def ::plant string?)
(spec/def ::plantid string?)
(spec/def ::command string?)
(spec/def ::arg any?)
(spec/def ::args (spec/coll-of ::arg :kind vector?)) ;;TODO
(spec/def ::argsmap (spec/map-of any? ::arg)) ;;TODO
(spec/def ::non-primitive string?)
(spec/def ::order number?)
(spec/def ::number number?)
(spec/def :pschema/activity (spec/keys :req-un [::tpn-type ::uid ::constraints ::end-node]
                                       :opt-un [::name ::label ::display-name ::sequence-label
                                                ::sequence-end ::cost ::reward ::controllable
                                                ::htn-node ::plant ::plantid ::command
                                                ::args ::argsmap ::non-primitive ::order ::number]))

(spec/def ::belief (spec/and float?
                             #(<= 0.0 % 1.0)))

(spec/def :keyword/action-probably any?)

(spec/def :keyword/affective-indicators any?)

(spec/def :keyword/app-id string?)

(spec/def :bsc/subject string?)
(spec/def :bsc/changed string?)
(spec/def :bsc/values (spec/or :string string?
                               :vector vector?
                               :map map?)) ;;string, map, or a vector?
(spec/def :bsc/agent-belief ::belief)
(spec/def :keyword/belief-state-changes (spec/keys
                                         :req-un [:bsc/subject :bsc/changed :bsc/values
                                                  :bsc/agent-belief]))

(spec/def :bindings/numeric-bound int?)
(spec/def :bindings/infinity #(= "Infinity" %))
(spec/def :bindings/bound (spec/or :number :bindings/numeric-bound
                                   :infinity :bindings/infinity))
(spec/def :bindings/temporal-value (spec/or :just-lower :bindings/numeric-bound
                                            :lower-and-upper (spec/tuple :bindings/numeric-bound
                                                                         :bindings/bound)))
(spec/def :bindings/binding (spec/keys :req-un [:bindings/temporal-value]))
;;The keys in the bindings map are the IDs of the TPN node
(spec/def :keyword/bindings (spec/map-of any? :bindings/binding))

(spec/def :keyword/chat-intervention map?)

(spec/def :keyword/current-actions (spec/map-of any? :pschema/activity))

(spec/def ::node-bindings (spec/map-of any? number?)) ;;long value representing time in millis since unix epoch
(spec/def ::state #(#{"failed" "completed"} %))
(spec/def ::activity-fail-reason (spec/map-of any?
                                              #(#{"timeout" "choice-all-activities-failed"} %)))
;; TODO: Should ::state be a required key in current-state?
(spec/def :keyword/current-state (spec/keys :req-un []
                                            :opt-un [::state ::node-bindings ::activity-fail-reason]))

(spec/def :keyword/exchange string?)

(spec/def :keyword/generated-plan
  (spec/keys
   :req-up [:keyword/tpn :keyword/current-state
            :keyword/hypothesis-id :keyword/hypothesis-rank]
   :opt-un [:keyword/htn :keyword/pamela]))

(spec/def :keyword/htn any?)

(spec/def :keyword/hypothesis-rank (spec/and int?
                                             #(>= % 0)))
(spec/def :keyword/hypothesis-id string?)

(spec/def :keyword/learned-participant-model any?)

(spec/def :keyword/lpm-file string?)

(spec/def :keyword/mission_clock int?)

(spec/def :keyword/mission-ended any?) ;;TBD - likely boolean?, but need to verify

(spec/def :keyword/mission-id string?)

(spec/def :keyword/mission-ir-json any?)

(spec/def :keyword/mission-pamela any?)

(spec/def :keyword/mission-solver map?) ;;TBD (since we don't use this yet)

(spec/def :mqheader/version string?)
(spec/def :mqheader/timestamp string?)
(spec/def :mqheader/message_type string?)
(spec/def :mqtt/header (spec/keys :req-un [:mqheader/version
                                           :mqheader/timestamp
                                           :mqheader/message_type]))
(spec/def :mqmsg/experiment_id string?)
(spec/def :mqmsg/trial_id string?)
(spec/def :mqmsg/timestamp string?)
(spec/def :mqmsg/source string?)
(spec/def :mqmsg/sub_type string?)
(spec/def :mqmsg/version string?)
(spec/def :mqmsg/replay_parent_type string?)
(spec/def :mqmsg/replay_parent_id string?)
(spec/def :mqmsg/replay_id string?)
(spec/def :mqtt/msg (spec/keys :req-un [:mqmsg/experiment_id
                                        :mqmsg/trial_id
                                        :mqmsg/timestamp
                                        :mqmsg/source
                                        :mqmsg/sub_type
                                        :mqmsg/version]
                               :opt-un [:mqmsg/replay_parent_type
                                        :mqmsg/replay_parent_id
                                        :mqmsg/replay_id]))
(spec/def :mqtt/data map?)
(spec/def :mqtt/topic string?)
(spec/def :mqtt/message (spec/keys :req-un [:mqtt/header :mqtt/msg :mqtt/data]))
(spec/def :keyword/mqtt-message
  (spec/keys :req-un [:mqtt/topic :mqtt/message]))

(spec/def :keyword/next-actions (spec/map-of any? :pschema/activity))

(spec/def :observation/field string?)
(spec/def :observation/value any?) ;;this any? is probably correct
(spec/def ::observation (spec/keys :req-un [:observation/field :observation/value]))
(spec/def :keyword/observations (spec/coll-of ::observation :kind vector?))

(spec/def :keyword/other-possibilities (spec/map-of any? :pschema/activity))

(spec/def :keyword/pamela string?)

(spec/def :keyword/plant-id string?)

(spec/def :prediction/uid string?)
(spec/def :prediction/agent-belief ::belief)
(spec/def :prediction/state (spec/or :unknown #(= % "unknown")
                                     :boolean boolean?))  ;;unknown, true, or false
(spec/def :prediction/subject string?)
(spec/def :prediction/action string?)
(spec/def :prediction/object any?) ;;Currently use both string? and vector of maps TODO

(spec/def :prediction/using string?)
(spec/def :prediction/bound number?)
(spec/def :prediction/bounds (spec/coll-of :prediction/bound :kind vector?))
(spec/def :prediction/reason (spec/or :string string?
                                      :vector vector?
                                      :map map?))
(spec/def :keyword/predictions
  (spec/keys :req-un [:prediction/uid :prediction/agent-belief :prediction/state
                      :prediction/subject :prediction/action :prediction/object] ;;Is subject required? TODO
             :opt-un [:keyword/hypothesis-rank :keyword/hypothesis-id :prediction/using
                      :prediction/bounds :prediction/reason]))

(spec/def :pstat/unhandled-timeouts int?)
(spec/def :pstat/mission-stats map?)
(spec/def :pstat/mission-stats-details map?)
(spec/def :pstat/aggregate-stats map?)
(spec/def :pstat/aggregate-stats-details map?)

(spec/def :keyword/prediction-stats
  (spec/keys :req-un [:pstat/unhandled-timeouts
                      :pstat/mission-stats
                      :pstat/mission-stat-details
                      :pstat/aggregate-stats
                      :pstat/aggregate-stats-details]))

(spec/def :keyword/raycasting any?)

(spec/def :keyword/reason (spec/and string?
                                    #(#{"temporal-planner-failed"
                                        "all-choices-failed"
                                        "time-infeasible"
                                        "completed"} %)))

(spec/def :keyword/received-routing-key string?)

(spec/def :keyword/routing-key string?)

(spec/def :keyword/rtd-filename string?)

(spec/def :keyword/state string?)

(spec/def :keyword/task-completed any?)

(spec/def :keyword/task-started any?)

(spec/def :keyword/testbed-message any?)

(spec/def :keyword/timestamp int?)

(spec/def :tpn/network-id string?)
(spec/def :keyword/tpn (spec/keys
                        :req-un [:tpn/network-id]))

(spec/def :keyword/training-rtd-files (spec/coll-of string? :kind vector?))

(spec/def :keyword/unknown-action any?)

; TODO Write a function that actually parses the clock string and validates it.
(spec/def :keyword/tb_clock string?)

(spec/def :keyword/temporal-plan map?)

(spec/def :keyword/dispatch-mission map?)

(spec/def :keyword/new-plan-request map?)

(spec/def :keyword/data map?)

;;;
;;; There should be a single `defmethod message` definition for each RMQ message
;;;
;;; Initially, these were generated by rita.rmq-validator.cli/pprint-method-for-all-messages
;;; If you add a message (to rabbitmq-messages.json), you probably want to run this function
;;; and then cut-and-paste the appropriate defmethod(s) below
;;;

(defmulti message :routing-key)

(defmethod message "ac-controller" [_]
  (spec/keys
   :req-un [:keyword/routing-key :keyword/app-id :keyword/data]
   :opt-un [:keyword/timestamp :keyword/exchange :keyword/received-routing-key]))

(defmethod message "action-probably" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/action-probably]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "affective-indicators" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/affective-indicators]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "belief-state-changes" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/belief-state-changes]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "chat-intervention" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/chat-intervention]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "clock" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id]
   :opt-un [:keyword/exchange :keyword/received-routing-key :keyword/tb_clock]))

(defmethod message "create-learned-participant-model" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id
            :keyword/training-rtd-files :keyword/lpm-file]
   :opt-un [:keyword/exchange :keyword/received-routing-key :keyword/mission-id]))

(defmethod message "dispatch-mission" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id :keyword/dispatch-mission]
   :opt-un [:keyword/exchange :keyword/received-routing-key :keyword/htn]))

(defmethod message "dispatch-mission-updates" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/tpn :keyword/current-actions :keyword/next-actions :keyword/other-possibilities]
   :opt-un [:keyword/exchange :keyword/received-routing-key :keyword/htn]))

(defmethod message "generated-plan" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/generated-plan]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "ground_truth_prompt" [_]
  (spec/keys
   :req-un [:keyword/routing-key :keyword/app-id]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "mission_clock" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/tb_clock]
   :opt-un [:keyword/exchange :keyword/received-routing-key :keyword/mission_clock]))

(defmethod message "mission-ended" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id :keyword/mission-ended]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "mission-pamela" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/mission-pamela :keyword/mission-ir-json]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "mission-solver" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/mission-solver]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "mqtt-message" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/mqtt-message]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "new-plan-request" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id :keyword/new-plan-request]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "next-actions" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/tpn :keyword/next-actions]
   :opt-un [:keyword/exchange :keyword/received-routing-key :keyword/htn]))

(defmethod message "observations" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/plant-id :keyword/observations :keyword/state]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "prediction-stats" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/prediction-stats :keyword/mission-id]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "predictions" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/predictions]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "raycasting" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/raycasting]))

(defmethod message "rita-player-ended" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "shutdown-rita" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "start-timing-data-extraction" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/rtd-filename]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "startup-rita" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id]
   :opt-un [:keyword/exchange :keyword/received-routing-key :keyword/learned-participant-model]))

(defmethod message "task-completed" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/task-completed]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "task-started" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/task-started]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(defmethod message "temporal-plan" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id :keyword/temporal-plan]
   :opt-un [:keyword/exchange :keyword/received-routing-key :keyword/htn]))

(defmethod message "testbed-message" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id
            :keyword/testbed-message]
   :opt-un [:keyword/exchange :keyword/received-routing-key :keyword/mission-id]))

(defmethod message "unknown-action" [_]
  (spec/keys
   :req-un [:keyword/timestamp :keyword/routing-key :keyword/app-id :keyword/mission-id
            :keyword/unknown-action]
   :opt-un [:keyword/exchange :keyword/received-routing-key]))

(spec/def ::message (spec/multi-spec message :routing-key))
