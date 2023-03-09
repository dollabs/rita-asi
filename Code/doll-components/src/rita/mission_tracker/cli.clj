;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.mission-tracker.cli
  "RITA AttackModelGenerator main."
  (:require [pamela.tools.plant.connection :as plant]
            [pamela.tools.plant.interface :as plant_i]
            [clojure.tools.cli :as cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [clojure.data.codec.base64 :as base64]
            [clojure.string :as string]
            [clojure.pprint :as pp :refer [pprint]]
            [me.raynes.fs :as fs]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [mbroker.rabbitmq :as rmq]
            [clojure.java.shell :as shell]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [rita.common.core :as rc :refer :all]
    ;; [rita.generative-planner.generative-planner :as amg :refer :all]
    ;; [rita.generative-planner.desirable-properties :as dp :refer :all]
    ;; [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
    ;; [pamela.tools.belief-state-planner.montecarloplanner :as bs]
    ;; [pamela.tools.belief-state-planner.ir-extraction :as irx]

            [clojure.java.io :as io])
  (:gen-class))                                             ;; required for uberjar

;;;;;;; defs
(defonce act-state (atom {}))
(defonce belief-state (atom []))
(defonce predictions (atom []))
(defonce activities (atom []))
(defonce plant-object (atom nil))
(defonce cache-belief-state (atom []))

;;;;;;; defns ;;;;;;;
(defn reset-state []
  (reset! act-state {})
  (reset! activities [])
  (reset! belief-state [])
  (reset! predictions [])
  (reset! cache-belief-state []))


(defn update-act-state [k v]
  (swap! act-state merge {k v})
  (swap! activities conj {k v}))

(defn get-current-actions []
  (:current-actions @act-state))
; Example prediction
#_{:hypothesis-rank 0,
   :uid             "se16974",
   :agent-belief    0.6,
   :state           "unknown",
   :bounds          [0 5],
   :reason          ["and"
                     {:subject "ASIST5", :is-moving-towards "Lobby entrance to North Corridor", :agent-belief 0.9}
                     {:subject "ASIST5", :sees-no-untriaged-victims-of-type "unknown-type", :agent-belief 0.7}
                     {:subject "ASIST5", :wants-to-find "victim", :agent-belief 0.9}
                     {:subject "ASIST5", :wants-to-triage "victim", :agent-belief 1.0}],
   :hypothesis-id   "hyp0001-pending",
   :action          "exit-room",
   :using           "Lobby entrance to North Corridor",
   :subject         "ASIST5",
   :object          "North Corridor"}
(defn publish-prediction [act state]
  ; FIXME mission-id
  ; FIXME activity bounds
  (let [msg {:hypothesis-id   "FIXME"
             :hypothesis-rank 0
             :uid             (:plant-invocation-id act)
             ; Assume 1.0 for now
             :agent-belief    1.0
             :state           state
             ; FIXME bounds
             :bounds          [0 5]
             :action          (:command act)
             :subject         (:plant act)
             :object          (first (:args act))}]
    (println "publish prediction" (:subject msg) (:action msg) (:object msg) ":" state)
    ;(pprint msg)
    (publish-message (get-channel) nil "predictions" {:predictions msg})))

(defn update-current-actions [val]
  (doseq [[_ act] val]
    (publish-prediction act "unknown"))
  (update-act-state :current-actions val))

(defn update-next-actions [val]
  (update-act-state :next-actions val))

(defn update-other-actions [val]
  (update-act-state :other-actions val))

(defn print-act-state []
  (let [print-a (fn [which-action]
                  (doseq [act (vals (which-action @act-state))]
                    (pprint act)
                    (println "command:" (:command act))
                    (println "name:" (:name act))))]
    (print-a :current-actions)))

(defn add-belief-state [val]
  (swap! belief-state conj val))

(defn add-cached-belief-state [val]
  (swap! cache-belief-state conj val))

(defn add-prediction [val]
  (swap! predictions conj val))

; ??perhaps we need to associate prediction id with plant-message-id instead of action name and plant function name
; Not using predictions to determine acitivity state.
#_(defn handle-prediction-update [msg]
    (add-prediction msg)
    ;(pprint msg)
    ; if prediction state is
    ;  * unknown, consider activity as started
    ;  * true (from state estimation) consider activity as finished.
    ;  * false (from state estimation or prediction generator timed out), consider activity as failed.
    (let [action (:action msg)
          state  (:state msg)]
      (println "pred " action state)
      (doseq [[aid act] (:current-actions @act-state)]
        ;(pprint act)
        (when (= (:command act) action)
          (cond (= state "unknown")
                (plant_i/started @plant-object (:plant act) (:plant-invocation-id act) nil)
                (true? state)
                (plant_i/finished @plant-object (:plant act) (:plant-invocation-id act) {} nil)
                (false? state)
                (plant_i/failed @plant-object (:plant act) (:plant-invocation-id act) {} nil)
                :else
                (println "unknown prediction state" state))))))

(defn handle-belief-state-update [subject]
  (println "visited" subject)
  (println "Current actions / activities" (count (:current-actions @act-state)))
  (doseq [[_ act] (:current-actions @act-state)]
    (println (:plant act) (:command act) (:args act)))
  (println)
  (doseq [[_ act] (:current-actions @act-state)]
    ;(pprint act)
    (when (= (:command act) "goto-room")
      (cond (= subject (first (:args act)))
            (do
              (println "Finished Act: " (:command act) (first (:args act)))
              (plant_i/finished @plant-object (:plant act) (:plant-invocation-id act) {} nil)
              (publish-prediction act true))
            :else
            (do
              (println "Failed Act: " (:command act) (first (:args act)))
              (plant_i/failed @plant-object (:plant act) (:plant-invocation-id act) {:command (:command act)
                                                                                     :subject subject} nil)
              (publish-prediction act false)))
      (println)
      (reset! act-state {}))))

; The case when planner is lagging behind belief state
;  -- If current-actions are found in belief state
;     They are marked as completed
;
;     If no more current actions, then look at next-actions and mark them as completed
;     If anymore current-actions left, then wait for future belief state update to deal with them
;  -- clear cached-belief-state as TPN actions are considered to be caught with belif state


(defn handle-cached-belief-state []
  (println "handle cached belief state"))



(defn init-plant [exchange host port]
  (if (nil? @plant-object)
    (reset! plant-object (plant/make-plant-connection exchange {:host host :port port}))
    (println "plant object already initialized. ")))

;;;;;;; Conditions ;;;;;;;

;; This is an example handler used only to test RITA's inter-component plumbling
;; It should be replaced with somethig real
(defcondition startup-rita-observed [startup-rita] [ch]
              (let [mission-id (:mission-id startup-rita)]
                (println "Startup-RITA message received for mission" mission-id startup-rita)))

(defcondition dispatch-mission-updates-received [dispatch-mission-updates] [ch]
              (let [{:keys [mission-id htn tpn current-actions next-actions other-possibilities]}
                    dispatch-mission-updates]
                (println "Dispatch-Mission-Updates message received for mission" mission-id)
                #_(pprint dispatch-mission-updates)
                ;(pprint current-actions)
                ;(pprint next-actions)
                ;(pprint other-possibilities)
                (println "received activities")
                (update-current-actions current-actions)
                (update-next-actions next-actions)
                (update-other-actions other-possibilities)
                (if (> (count @cache-belief-state) 0)
                  (handle-cached-belief-state))))


(defcondition belief-state-changes-received [belief-state-changes] [ch]
              (let [{:keys [mission-id belief-state-changes]} belief-state-changes]
                ;(println "---- Belieft state changes")
                ;(pprint belief-state-changes)
                ;{:subject "/Falcon.CloakR", :changed "state", :values "visited", :agent-belief 1.0}
                (add-belief-state belief-state-changes)
                (if (= 0 (count (get-current-actions)))
                  (add-cached-belief-state belief-state-changes)
                  (let [{:keys [subject changed values]} belief-state-changes]
                    (if (and (= "state" changed) (= "visited") values)
                      (handle-belief-state-update subject))))))

#_(defcondition predictions-received [predictions] [ch]
                ;(pprint predictions)
                (let [{:keys [mission-id predictions]} predictions]
                  (handle-prediction-update predictions)))

(defmain -main 'MissionTracker
         (init-plant (get-exchange) (get-rmq-host) (get-rmq-port)))
