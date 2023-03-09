;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.temporal-planner.cli
  "RITA TemporalPlanner main."
  (:require [clojure.tools.cli :as cli :refer [parse-opts]]
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
            [pamela.tools.mct-planner.planner :as planner]
            [pamela.tools.utils.tpn-json :as pt-json]
            [pamela.tools.utils.util :as pt-util]
            [rita.common.core :as rc :refer :all]
            [clojure.java.io :as io])
  (:gen-class))                                             ;; required for uberjar

(def debug true)
(def default-solver-iterations 5)

(defn ask-new-plan-from-gp [hy-id hy-rank mission-id reason ch]
  (println "ask-new-plan-from-gp" mission-id reason)
  (publish-message ch mission-id "new-plan-request" {:new-plan-request {:hypothesis-id hy-id
                                                                        :hypothesis-rank hy-rank
                                                                        :fail-reason reason}}))

(defn plan-mission [tpn current-state n-iterations]
  (let [{node-bindings :node-bindings} current-state
        tpn            (pt-json/map-from-json-str (json/write-str tpn))
        planner-output (planner/solve-with-node-bindings tpn node-bindings n-iterations)
        bindings       (:bindings planner-output)]

    (when node-bindings (println "Previous bindings")
                        (pprint node-bindings))
    (println "New bindings")
    (pprint bindings)
    bindings))

(defn replan-mission [hy-id hy-rank mission-id tpn current-state n-iterations ch]
  (let [bindings (plan-mission tpn current-state n-iterations)]
    (cond (not (nil? bindings))
          (publish-message ch mission-id "temporal-plan"
                           {:temporal-plan {:hypothesis-id   hy-id
                                            :hypothesis-rank hy-rank
                                            :tpn             tpn
                                            :bindings        (pt-util/convert-bindings-to-json bindings)}})
          :else
          (ask-new-plan-from-gp hy-id hy-rank mission-id :temporal-planner-failed ch))))

(defn handle-mission-failed [hy-id hy-rank mission-id tpn current-state n-iterations ch]
  ; TPN is pre parsed but values are not in expected form. So write to JSON and read again
  (let [{fail-reason :activity-fail-reason} current-state
        fail-due-to-timeout?          (contains? (into #{} (vals fail-reason)) "timeout")
        choice-all-activities-failed? (contains? (into #{} (vals fail-reason)) "choice-all-activities-failed")]

    (println "handle-mission-failed fail reason")
    (pprint fail-reason)
    (println "fail-due-to-timeout?" fail-due-to-timeout?)
    (println "choice-all-activities-failed?" choice-all-activities-failed?)
    (cond choice-all-activities-failed?                     ; For RITA we always dispatch all choices!
          (ask-new-plan-from-gp hy-id hy-rank mission-id :all-choices-failed ch)

          fail-due-to-timeout?
          (ask-new-plan-from-gp hy-id hy-rank mission-id :time-infeasible ch)
          ; failed due to other reasons. Try to replan and see!!
          :else
          (replan-mission hy-id hy-rank mission-id tpn current-state n-iterations ch))))

;;;;;;; Conditions ;;;;;;;
(defcondition startup-rita-observed [startup-rita] [ch]
              (let [mission-id (:mission-id startup-rita)]
                (println "Startup-RITA message received for mission" mission-id startup-rita)))

; When generative planner sends a new plan.
(defcondition generated-plan-received [generated-plan] [ch]
              (let [{:keys [mission-id current-state]} generated-plan
                    {:keys [hypothesis-id hypothesis-rank tpn]} (:generated-plan generated-plan)]

                ;(pprint generated-plan)
                (println "Generated-Plan message received for mission" mission-id)

                (when debug
                  ;(pprint htn)
                  (println "hypothesis-id" hypothesis-id)
                  (println "hypothesis-rank" hypothesis-rank)
                  ;(pprint tpn)
                  #_(pprint current-state))


                (if tpn
                  (replan-mission hypothesis-id hypothesis-rank mission-id tpn current-state default-solver-iterations  ch)
                  (println "TPN is nil for mission-id" mission-id))))

;; Sent by Mission Dispatcher when the whole mission is failed or completed
(defcondition dispatch-mission-received [dispatch-mission] [ch]
              (let [{:keys [mission-id]} dispatch-mission
                    {:keys [hypothesis-id hypothesis-rank tpn current-state]} (:dispatch-mission dispatch-mission)]
                (println "Dispatch-Mission message received for mission" mission-id "state" (:state current-state))
                ;(pprint current-state)
                (cond (= "failed" (:state current-state))
                      (handle-mission-failed hypothesis-id hypothesis-rank mission-id tpn current-state (* 5 default-solver-iterations) ch)
                      (= "completed" (:state current-state))
                      (ask-new-plan-from-gp hypothesis-id hypothesis-rank mission-id (:state current-state) ch)
                      :else
                      (pt-util/to-std-err (println "TPN Finished" (:network-id tpn) "with unknown state:" (:state current-state))))))


(defmain -main 'TemporalPlanner)
;;Optionally add some initialization code here


; TODO produce predictions for choices.