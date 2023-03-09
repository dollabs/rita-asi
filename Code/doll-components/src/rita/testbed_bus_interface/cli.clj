;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.testbed-bus-interface.cli
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
            ;; [rita.generative-planner.generative-planner :as amg :refer :all]
            ;; [rita.generative-planner.desirable-properties :as dp :refer :all]
            ;; [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
            ;; [pamela.tools.belief-state-planner.montecarloplanner :as bs]
            ;; [pamela.tools.belief-state-planner.ir-extraction :as irx]

            [clojure.java.io :as io])
  (:gen-class)) ;; required for uberjar

;; This is an example handler used only to test RITA's inter-component plumbling
;; It should be replaced with somethig real
(defcondition startup-rita-observed [startup-rita] [ch]
  (let [mission-id (:mission-id startup-rita)]
    (println "Startup-RITA message received for mission" mission-id startup-rita)
    (Thread/sleep 2000)
    (publish-message ch mission-id "testbed-message"
                       {:testbed-message {:name "test-testbed-message"}})))

(defmain -main 'TestbedBusInterface
  ;;Optionally add some initialization code here
  )
