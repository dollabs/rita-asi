;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.generative-planner.cli
  "RITA AttackModelGenerator main."
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
            [rita.common.core :as rc :refer :all]
            [pamela.tools.utils.tpn-json :as pt-json]
            ;; [rita.generative-planner.generative-planner :as amg :refer :all]
            [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
            [pamela.tools.belief-state-planner.montecarloplanner :as bs]
            [pamela.tools.belief-state-planner.ir-extraction :as irx]
            [rita.generative-planner.planning-support-algorithms :as psa]
            [clojure.java.io :as io])
  (:gen-class)) ;; required for uberjar

;; This is an example handler used only to test RITA's inter-component plumbling
;; It should be replaced with somethig real
(defcondition startup-rita-observed [startup-rita] [ch]
  (let [mission-id (:mission-id startup-rita)]
    (println "Startup-RITA message received for mission" mission-id startup-rita)))

(defcondition new-plan-request-received [new-plan-request] [ch]
  (println "Generative Planner got new plan request for mission" (:mission-id new-plan-request))
  (let [{:keys [mission-id reason]} new-plan-request]
    (pprint reason)))

;; This is an example handler used only to test RITA's inter-component plumbling
;; It should be replaced with somethig real
(defcondition mission-pamela-and-solver [mission-pamela mission-solver] [ch]
  (let [{:keys [mission-id mission-pamela mission-ir-json]} mission-pamela
        {solver :mission-solver} mission-solver]
    (println "Mission-Pamela and Mission-Solver message received for mission" mission-id)
    (pprint mission-pamela)
    (pprint mission-ir-json)
    (pprint solver)

    (let [htn {:name "test-htn"}
          tpn {:name "test-tpn"}
          current-state {:name "test-current-state"}]
      ;;This is now obsolete, so let's not publish it and mislead ourselves.
      ;; (publish-message ch mission-id "generated-plan"
      ;;                  {:htn htn
      ;;                   :tpn tpn
      ;;                   :current-state current-state})
      )))

(defn test-send-generated-plan [& [fname]]
  (let [;_htn (slurp "resources/public/test-environment.htn.json")
        fname (if fname fname
                        "resources/public/test-environment.tpn.json"
                        ;"test/example-plan/example-plan.tpn.json"
                        )
        tpn (pt-json/from-file fname)]
    (println "test-send-generated-plan Publishing " fname)
    (publish-message (get-channel) "test-mission-id" "generated-plan" {:htn           {}
                                                                       :tpn           tpn
                                                                       :current-state {}})))


(defmain -main 'GenerativePlanner
         ;;Optionally add some initialization code here
         ;(test-send-generated-plan)
         (exit 0))
