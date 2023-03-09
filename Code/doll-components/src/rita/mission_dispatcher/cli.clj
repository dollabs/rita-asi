;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.mission-dispatcher.cli
  "RITA Mission Dispatcher main."
  (:require [clojure.data.json :as json]
            [clojure.pprint :refer [pprint]]
            [environ.core :refer [env]]
            [pamela.tools.dispatcher_manager.core :as dapp]
            [pamela.tools.utils.util :as pt-utils]
            [pamela.tools.plant.util :as ptp-utils]
            [pamela.tools.utils.tpn-json :as pt-json]
            [pamela.tools.utils.timer :as pt-timer]
            [pamela.tools.dispatcher.tpn-import :as pt-tpn-import]
            [rita.common.core :refer :all])

  (:gen-class))                                             ;; required for uberjar

(defonce dapp-setup nil)
(defonce startup-msg (atom {}))
;(defonce plant-subscriptions (atom #{}))
(defonce state (atom {}))
(defonce counter-prefix (str "mission-dispatcher-" (ptp-utils/make-time-as-str)))
(def use-network-id-as-plant-method-id true)

;;;;;;; defns ;;;;;;;
(defn get-mission-id []
  (get @startup-msg :mission-id))

#_(defn get-htn []
    (get @startup-msg :htn))

#_(defn get-tpn []
    (get @startup-msg :tpn))

(defn make-plant-method-id [tpn]
  (if use-network-id-as-plant-method-id
    (:network-id tpn)
    (ptp-utils/make-method-id counter-prefix)))

(defn add-new-tpn [mission-id hy-id hy-rank netid tpn bindings]
  (when (contains? @state (:network-id tpn))
    (let [netid (:network-id tpn)
          st    @state
          {prev-hy-id :hypothesis-id prev-hy-rank :hypothesis-rank} (netid st)]
      (println "TPN exists in state. Will override" netid prev-hy-id prev-hy-rank)))
  (swap! state merge {netid {:mission-id mission-id :hypothesis-id hy-id :hypothesis-rank hy-rank :tpn tpn :bindings bindings}}))

(defn get-tpn-info [netid]
  (println "get-tpn-info looking for" netid)
  (if (netid @state)
    (let [{:keys [mission-id hypothesis-id hypothesis-rank ]} (netid @state)]
      {:mission-id mission-id :hypothesis-id hypothesis-id :hypothesis-rank hypothesis-rank})
    {netid          :no-tpn-found
     :hypothesis-id :not-tpn-found}))

(defn handle-tpn-finished [tpn]
  (let [info (get-tpn-info (:network-id tpn))]
    (println "MissionDispatcher TPN Completed:" (:network-id tpn) info)
    (publish-message (get-channel) (or (:mission-id info) (get-mission-id)) "dispatch-mission" {:dispatch-mission (merge info
                                                                                                                         {:tpn           tpn
                                                                                                                          :current-state {:state :completed}})})))
(defn handle-tpn-failed [tpn node-state fail-reason]
  (println "MissionDispatcher TPN Failed:" (:network-id tpn))
  ; Notify temporal planner that mission has failed.
  (println "node state")
  ;(pprint node-state)
  (println "activity fail reason")
  (pprint fail-reason)
  (let [info (get-tpn-info (:network-id tpn))]
    (publish-message (get-channel) (or (:mission-id info) (get-mission-id)) "dispatch-mission" {:dispatch-mission (merge info {:tpn           tpn
                                                                                                                               :current-state {:node-bindings        node-state
                                                                                                                                               :state                :failed
                                                                                                                                               :activity-fail-reason fail-reason}})})))
(defn handle-new-tpn [mission-id hy-id hy-rank tpn bindings]
  (let [plant-id (make-plant-method-id tpn)
        netid (keyword (str (name plant-id) "-" (name (:network-id tpn))))]
   (add-new-tpn mission-id hy-id hy-rank netid tpn bindings)
   (dapp/send-start-msg plant-id mission-id tpn (pt-utils/convert-bindings-to-json bindings) true))
  #_(doseq [pl-id (pt-utils/get-plant-ids tpn)]
      (add-plant-subscription pl-id))
  #_(dapp/setup-and-dispatch-tpn-with-bindings tpn bindings 0))

(defn init []
  (pt-timer/set-use-sim-time (get-sim-clock)))

(defn init-dapp []
  (when-not dapp-setup
    (println "Setting up Dispatcher Manager App")
    (dapp/set-dispatch-all-choices true)
    (dapp/set-tpn-failed-handler (fn [tpn node-state fail-reason]
                                   (handle-tpn-failed tpn node-state fail-reason)))
    (dapp/set-tpn-finished-handler (fn [tpn]
                                     (handle-tpn-finished tpn)))
    (dapp/init dapp/default-plant-id (get-exchange) (get-rmq-host) (get-rmq-port))
    (def dapp-setup true)))

;;;;;;;;;;;;;;;;; Conditions ;;;;;;;;;;;;;;;;

(defcondition startup-rita-observed [startup-rita] [ch]
              (let [mission-id (:mission-id startup-rita)]
                (reset! startup-msg startup-rita)
                (println "Startup-RITA message received for mission" mission-id startup-rita)))

; Handle a mission
(defcondition temporal-plan-received [temporal-plan] [ch]
              (let [{:keys [mission-id]} temporal-plan
                    {:keys [hypothesis-id hypothesis-rank tpn bindings]} (:temporal-plan temporal-plan)
                    ;_ (pprint tpn)
                    tpn      (pt-json/map-from-json-str (json/write-str tpn))
                    tpn      (pt-tpn-import/from-map tpn)
                    bindings (pt-utils/convert-json-bindings-to-clj bindings)]
                (println "New mission" mission-id "TPN" (:network-id tpn))
                (reset! startup-msg {:mission-id mission-id :tpn tpn :bindings bindings})
                (handle-new-tpn mission-id hypothesis-id hypothesis-rank tpn bindings)))

(defcondition clock-message [clock] [ch]
              ;; (println "clock message received: " clock)
              ;(pprint clock)
              (pt-timer/update-clock (:timestamp clock)))

(defmain -main 'MissionDispatcher
         (init)
         (init-dapp))

; "dispatch-mission-updates": {"publishers": ["MissionDispatcher"],
;                              "subscribers": ["ActionPlanning", "Genesis", "MissionTracker"],
;                              "required-keys": ["timestamp", "routing-key",  "app-id", "mission-id",
;                                                "htn", "tpn", "current-actions", "next-actions",
;                                                "other-possibilities"]},
; Dispatcher will extract plant ids from tpn and subscribe to plant start commands
; For each plant command it will produce current-actions, next-actions and other-possibilities.
; each started command goes in to current-actions bucket
; for each started activity, next-actions will be next activity that could start if this activity is finished
;   impl a function in dispatch.clj to give this data.
; All other activities beyond the started activity will be other-possibilities
; Finished, failed, cancelled activities should not be in any of current-actions,next-actions and other-possibilities
;

;; This is an example handler used only to test RITA's inter-component plumbling
;; It should be replaced with somethig real
#_(defcondition task-completed-received [task-completed] [ch]
                (let [{:keys [mission-id task-completed]} task-completed]
                  (println "Task-Completed message received for mission" mission-id)
                  (pprint task-completed)))

;; This is an example handler used only to test RITA's inter-component plumbling
;; It should be replaced with somethig real
#_(defcondition task-started-received [task-started] [ch]
                (let [{:keys [mission-id task-started]} task-started]
                  (println "Task-Started message received for mission" mission-id)
                  (pprint task-started)))

;; This is an example handler used only to test RITA's inter-component plumbling
;; It should be replaced with somethig real
#_(defcondition action-probably-received [action-probably] [ch]
                (let [{:keys [mission-id action-probably]} action-probably]
                  (println "Action-Probably message received for mission" mission-id)
                  (pprint action-probably)))

;; This is an example handler used only to test RITA's inter-component plumbling
;; It should be replaced with somethig real
#_(defcondition unknown-action-received [unknown-action] [ch]
                (let [{:keys [mission-id unknown-action]} unknown-action]
                  (println "Unknown-Action message received for mission" mission-id)
                  (pprint unknown-action)))




#_(defn publish-mission-updates [current-next-other-actions]
    ;(pprint current-next-other-actions)
    (publish-message (get-channel) (get-mission-id) "dispatch-mission-updates" {;:tpn                 (get-tpn)
                                                                                ;:htn                 (get-htn)
                                                                                :current-actions     (:current-actions current-next-other-actions)
                                                                                :next-actions        (:next-actions current-next-other-actions)
                                                                                :other-possibilities (:other-actions current-next-other-actions)}))


#_(defn plant-msg-handler [data]
    (let [pl-msg (pt-utils/rmq-data-to-clj data)]
      (println "Got plant msg")
      (pprint pl-msg))
    #_(pprint (dapp/make-current-next-other-activities))
    (publish-mission-updates (dapp/make-current-next-other-activities)))

#_(defn add-plant-subscription [plant-id]
    (when-not (contains? @plant-subscriptions plant-id)
      (println "Creating plant subscription:" plant-id)
      (swap! plant-subscriptions conj plant-id)
      (println "Plant subscriptions" @plant-subscriptions)
      (rmq/make-subscription plant-id (fn [_ _ data]
                                        (plant-msg-handler data)) (get-channel) (get-exchange))))