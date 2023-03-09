;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.multhyp
  "RITA Multiple Hypothesis Tracking."
  (:import java.util.Date
           (java.util.concurrent LinkedBlockingQueue TimeUnit))
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
            [clojure.instant :as instant]
            [clojure.java.io :as io]
            [random-seed.core :refer :all]
            [rita.common.core :as rc :refer :all]
            ;[rita.common.surveys :as surveys]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [pamela.tools.utils.util]
            [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
            [pamela.tools.belief-state-planner.montecarloplanner :as bs]
            [pamela.tools.belief-state-planner.ir-extraction :as irx]
            [pamela.tools.belief-state-planner.coredata :as global]
            [pamela.tools.belief-state-planner.evaluation :as eval]
            [pamela.tools.belief-state-planner.lvarimpl :as lvar]
            [pamela.tools.belief-state-planner.prop :as prop]
            [pamela.tools.belief-state-planner.imagine :as imag]
            [pamela.tools.belief-state-planner.vprops :as vp]
            [pamela.tools.belief-state-planner.dmcgpcore :as core]
            [pamela.tools.belief-state-planner.planexporter :as pexp])
  (:refer-clojure :exclude [rand rand-int rand-nth])
  (:gen-class)) ;; required for uberjar

#_(in-ns 'rita.state-estimation.multhyp)

(def ^:dynamic *debug-level* #{:demo :all})
(defn dplev
  [& x]
  (some *debug-level* x))

(def ^:dynamic *multiple-hypothesis-plans* [])

(defrecord PlanHypo [rank
                     id
                     score
                     apsp-model])

;;; Accessors

(defn get-plan-hypotheses [] *multiple-hypothesis-plans*)
(defn get-plan-hypo-rank [hypo] @(.rank hypo))
(defn get-plan-hypo-id [hypo] @(.id hypo))
(defn get-plan-hypo-score [hypo] @(.score hypo))
(defn get-plan-hypo-apsp-model [hypo] @(.apsp-model hypo))

;;; setters

(defn set-plan-hypo-rank! [hypo nuval] (reset! (.rank hypo) nuval))
(defn set-plan-hypo-id! [hypo nuval] (reset! (.id hypo) nuval))
(defn set-plan-hypo-score! [hypo nuval] (reset! (.score hypo) nuval))
(defn set-plan-hypo-apsp-model! [hypo nuval] (reset! (.apsp-model hypo) nuval))
(defn set-plan-hypotheses! [nuhypos] (def ^:dynamic *multiple-hypothesis-plans* nuhypos))

(defn num-plan-hypos [] (count *multiple-hypothesis-plans*))

(defn get-plan-hypothesis-by-rank
  [n]
  (if (< n (num-plan-hypos))
    (nth (get-plan-hypotheses) n)
    (when (dplev :all) (println "ERROR: tried to acces plan hypothesis ranked " n "which doesn't exist"))))

(defn hypothesis-ids
  []
  (map get-plan-hypo-id (get-plan-hypotheses)))

(defn get-plan-hypothesis-by-id
  [id]
  (let [phyp (first (filter #(= (get-plan-hypo-id %) id) (get-plan-hypotheses)))]
    (or phyp (when (dplev :all) (println "ERROR: tried to access plan hypothesis with id" (global/prs id) "which doesn't exist"
                      "valid hypothesis ids are" (global/prs (hypothesis-ids)))))))

(defn add-plan-hypothesis
  [id]
  (let [existing (get-plan-hypothesis-by-id id)]
    (if (empty? existing)
      (let [numhypos (+ (num-plan-hypos) 1)
            new-hypo (PlanHypo.
                      (atom numhypos)
                      (atom id)
                      (atom 0.0)
                      (atom {}))]
        (set-plan-hypotheses! (conj (get-plan-hypotheses) new-hypo))
        new-hypo)
      (do
        (when (dplev :all) (println "Attempt to make a plan hypothesis that already exists:" id))
        existing))))

;;; Fin
