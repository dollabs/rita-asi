;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.planning
  ""
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
            [clojure.data.xml :as xml]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [random-seed.core :refer :all]
            [rita.common.core :as rc :refer :all]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            [rita.state-estimation.ras :as ras]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
            [pamela.tools.belief-state-planner.montecarloplanner :as bs]
            [pamela.tools.belief-state-planner.ir-extraction :as irx]
            [pamela.tools.belief-state-planner.coredata :as global]
            [pamela.tools.belief-state-planner.evaluation :as eval]
            [pamela.tools.belief-state-planner.lvarimpl :as lvar]
            [pamela.tools.belief-state-planner.prop :as prop]
            [pamela.tools.belief-state-planner.imagine :as imag]
            [pamela.tools.belief-state-planner.vprops :as vp]
            [pamela.tools.belief-state-planner.dmcgpcore :as core])
  (:refer-clojure :exclude [rand rand-int rand-nth shuffle]) ; because of random-seed.core
  (:gen-class)) ;; required for uberjar

#_(in-ns 'rita.state-estimation.planning)


(def newplanlock (Object.))

(defn enqueue-new-plan-request-reason
  [reason]
  (locking newplanlock
    (seglob/set-new-plan-request (conj (seglob/get-new-plan-request) reason))))

(defn dequeue-new-plan-request-reason
  []
  (locking newplanlock
    (if (not (empty? (seglob/get-new-plan-request)))
      (let [npr (first (seglob/get-new-plan-request))]
        (seglob/set-new-plan-request (into [] (drop 1 (seglob/get-new-plan-request))))
        npr))))
