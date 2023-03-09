;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.cognitiveload
  "cognitive load tracking"
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
            [mbroker.asist-msg :as asist-msg]
            [clojure.java.shell :as shell]
            [clojure.data.xml :as xml]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.instant :as instant]
            [random-seed.core :refer :all]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [rita.common.core :as rc :refer :all]
            ;[rita.common.surveys :as surveys]
            ;[rita.state-estimation.volumes :as vol :refer :all]
            ;[rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            [rita.state-estimation.ritamessages :as ritamsg]
            ;;[rita.state-estimation.rlbotif :as rlbotif]
            ;[rita.state-estimation.statlearn :as slearn]
            ;[rita.state-estimation.multhyp :as mphyp]
            ;[rita.state-estimation.rita-se-core :as rsc :refer :all]
            ;; [rita.generative-planner.generative-planner :as amg :refer :all]
            ;; [rita.generative-planner.desirable-properties :as dp :refer :all]
            [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
            [pamela.tools.belief-state-planner.montecarloplanner :as bs]
            [pamela.tools.belief-state-planner.ir-extraction :as irx]
            [pamela.tools.utils.util]
            [pamela.tools.belief-state-planner.coredata :as global]
            [pamela.tools.belief-state-planner.evaluation :as eval]
            [pamela.tools.belief-state-planner.lvarimpl :as lvar]
            [pamela.tools.belief-state-planner.prop :as prop]
            [pamela.tools.belief-state-planner.imagine :as imag]
            [pamela.tools.belief-state-planner.vprops :as vp]
            [pamela.tools.belief-state-planner.dmcgpcore :as core]
            [pamela.tools.belief-state-planner.planexporter :as pexp]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [rand rand-int rand-nth])
  (:gen-class)) ;; required for uberjar

(defn vote-cogload
  [source pid data em]
  (seglob/set-cogload-votes
   (merge (seglob/get-cogload-votes)
          {[source pid] [data em]})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Reporting cognitive load changes

;;; For Medic RED
;;; Rooms passed but not visited
;;; Critical victims seen but not cared for
;;; Normal victims seen but not cared for
;;; Critical Markers seen but not processed
;;; Normal Markers seen but not processed

;;; For Engineer
;;; Rooms passed but not visited
;;; Critical victims seen but not cared for
;;; Normal victims seen but not cared for
;;; Critical Markers seen but not processed
;;; Normal Markers seen but not processed

;;; For Evacuator
;;; Rooms passed but not visited
;;; Saved critical victims seen but not evacuated
;;; Saved normal victims seen but not evacuated
;;; Critical Markers seen but not processed
;;; Normal Markers seen but not processed

;;;+++ maybe make a map os maps indexed by role, then we wouldn't have to change the code when roles are renamed +++

(def ^:dynamic *cognitive-load-engineer* {})
(def ^:dynamic *cognitive-load-medical {})
(def ^:dynamic *cognitive-load-transport* {})

(defn reset-cognitive-load-counters
  []
  (def ^:dynamic *cognitive-load-engineer*  {:total-cognitive-load     0
                                             :skipped-rooms            #{} ; Set of rooms seen but not entered
                                             :skipped-critical-victims #{} ; Set of critical victims seen but not processed
                                             :skipped-normal-victims   #{} ; Set of normal victims seen but not processed
                                             :skipped-critical-markers #{} ; Set of critical markers seen but not handled
                                             :skipped-normal-markers   #{}}) ; Set of non-critical markers seen but not handled
  (def ^:dynamic *cognitive-load-medical*   {:total-cognitive-load     0
                                             :skipped-rooms            #{}
                                             :skipped-critical-victims #{}
                                             :skipped-normal-victims   #{}
                                             :skipped-critical-markers #{}
                                             :skipped-normal-markers   #{}})
  (def ^:dynamic *cognitive-load-transport* {:total-cognitive-load     0
                                             :skipped-rooms            #{}
                                             :skipped-critical-victims #{}
                                             :skipped-normal-victims   #{}
                                             :skipped-critical-markers #{}
                                             :skipped-normal-markers   #{}}))

(defn finagle
  [thresholdlow thresholdhigh]
  (let [rn (rand)]
    (cond (> rn thresholdhigh) 1
          (< rn thresholdlow) -1
          :otherwise 0)))

(defn computeDL
  [val thresholdlow thresholdhigh]
  (let [adjustment (finagle thresholdlow thresholdhigh)
        nuval (+ val adjustment)
        nuval (if (< nuval 0) 0 nuval)]
    (* 9 nuval)))

(defn convert-cl-to-bits
  [cogload]
  (let [{tcl :total-cognitive-load
         sr  :skipped-rooms
         scv :skipped-critical-victims
         snv :skipped-normal-victims
         scm :skipped-critical-markers
         snm :skipped-normal-markers} cogload
        nsr  (computeDL (count sr) 0.05 0.8)
        nscv (computeDL (count scv) 0.0 0.999)
        nsnv (computeDL (count snv) 0.0 0.999)
        nscm (computeDL (count scm) 0.05 0.6)
        nsnm (computeDL (count snm) 0.05 0.7)
        ntcl (+ nsr nscv nsnv nscm nsnm)]
    {:total-cognitive-load ntcl
     :skipped-rooms nsr
     :skipped-critical-victims nscv
     :skipped-normal-victims nsnv
     :skipped-critical-markers nscm
     :skipped-normal-markers nsnm}))

(defn maybe-report-cognitive-load
  [pub id participant old-cog-load new-cog-load]
  (or
   (if (not (= old-cog-load new-cog-load))
     (do ;
         (when (dplev :io :all) (println "Cognitive load for " participant "changed to:" (:total-cognitive-load new-cog-load)))
         (ritamsg/add-bs-change-message pub {:subject participant
                                     :changed :cognitive-load
                                     :values (convert-cl-to-bits new-cog-load)
                                     :agent-belief 0.9})))
   pub))

(defn get-cl-data-from-role
  [role]
  (case role
    "Engineering_Specialist"        *cognitive-load-engineer*
    "Hazardous_Material_Specialist" *cognitive-load-engineer*
    "Transport_Specialist"          *cognitive-load-transport*
    "Search_Specialist"             *cognitive-load-transport*
    "Medical_Specialist"            *cognitive-load-medical*
    "None"                          #{} ; People without a role have no load
    nil                             #{} ; People without a role have no load
    (when (and role (dplev :warn :all))
      (println "get-cl-data-from-role called with unknown role: " role))))

(defn cog-load-role
  [role]
  (let [cogload (get-cl-data-from-role role)
        bits (:total-cognitive-load cogload)]
    bits))

(defn transporter-cog-load-high-and-highest
  []
  (and (> (cog-load-role "Transport_Specialist") 50) ;+++ constant, at least pick a good value
       (> (cog-load-role "Transport_Specialist") (cog-load-role "Medical_Specialist") )
       (> (cog-load-role "Transport_Specialist") (cog-load-role "Engineering_Specialist") )))

(defn set-cl-data-from-role!
  [role data]
  (case role
    "Engineering_Specialist"        (def ^:dynamic *cognitive-load-engineer*  data)
    "Hazardous_Material_Specialist" (def ^:dynamic *cognitive-load-engineer*  data)
    "Transport_Specialist"          (def ^:dynamic *cognitive-load-transport* data)
    "Search_Specialist"             (def ^:dynamic *cognitive-load-transport* data)
    "Medical_Specialist"            (def ^:dynamic *cognitive-load-medical*   data)
    "None"                          #{} ; People without a role have no load
    nil                             #{} ; People without a role have no load
    (when (and role (dplev :warn :all)) (println "set-cl-data-from-role called with unknown role: " role)))
  data)

(defn compute-cognitive-load-in-bits
  [tnsr tscv tsnv tscm tsnm]
  (+ (* 9 tnsr) (* 9 tscv) (* 9 tsnv) (* 9 tscm) (* 9 tsnm))) ; These numbers should be calculated +++

(defn change-cognitive-load
  [role changes]
  (let [cl-data (get-cl-data-from-role role)]
    ; (when (dplev :all) (println "change-cognitive-load: cl-data=" cl-data))
    (if (not (empty? cl-data))
      (let [{psr  :skipped-rooms
             pscv :skipped-critical-victims
             psnv :skipped-normal-victims
             pscm :skipped-critical-markers
             psnm :skipped-normal-markers
             } cl-data
            nsr  (:skipped-rooms changes [])
            nscv (:skipped-critical-victims changes [])
            nsnv (:skipped-normal-victims changes [])
            nscm (:skipped-critical-markers changes [])
            nsnm (:skipped-normal-markers changes [])
            tsr  (clojure.set/union psr nsr)
            tscv (clojure.set/union pscv nscv)
            tsnv (clojure.set/union psnv nsnv)
            tscm (clojure.set/union pscm nscm)
            tsnm (clojure.set/union psnm nsnm)
            cload (compute-cognitive-load-in-bits (count tsr) (count tscv) (count tsnv) (count tscm) (count tsnm))
            newdata {:total-cognitive-load cload
                     :skipped-rooms tsr
                     :skipped-critical-victims tscv
                     :skipped-normal-victims tsnv
                     :skipped-critical-markers tscm
                     :skipped-normal-markers tsnm}]
        ; (when (dplev :all) (println "Setting cl-data for role:" role "data=" newdata))
        (set-cl-data-from-role! role newdata)))))

;;; Fin
