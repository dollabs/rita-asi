;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.study2
  "RITA State Estimation."
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
            [rita.common.surveys :as surveys]
            [rita.state-estimation.volumes :as vol :refer :all]
            ;[rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            [rita.state-estimation.ras :as ras]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.interventions :as intervene]
            [rita.state-estimation.predictions :as predict]
            [rita.state-estimation.victims :as victims]
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Study 2 specific


(defn choose-not
  [possibilities & avoid]
  (let [possibilities (clojure.set/difference possibilities (into #{} avoid))]
    (first possibilities)))

(defn select-map-bindings
  [mapbeliefs]
  (let [part-entropy (into [] (map (fn [[pid pdf]] [(ras/distribution-entropy pdf) [pid pdf]]) mapbeliefs))
        sortedbeliefs (sort (fn [x y] (< (first x) (first y))) part-entropy)
        maxresults (into [] (map (fn [[ent [pid pdf]]] [pid (key (apply max-key val pdf))]) sortedbeliefs))
        constrained (cond
                      (< (count maxresults) 3) nil
                      ;; If the best two are different, the unused one
                      (not (= (second (first maxresults)) (second (second maxresults))))
                      [(nth maxresults 0)
                       (nth maxresults 1)
                       [(first (nth maxresults 2)) (choose-not #{"_24" "_34" "_64"} (second (nth maxresults 0)) (second (nth maxresults 1)))]]
                      :otherwise
                      (let [choice2 (choose-not #{"_24" "_34" "_64"} (second (nth maxresults 0)))
                            choice3 (choose-not #{"_24" "_34" "_64"} (second (nth maxresults 0)) choice2)]

                        [(first maxresults)
                         [(first (nth maxresults 1)) choice2]
                         [(first (nth maxresults 2)) choice3]]))]
    constrained))


(defn get-map-assignment-beliefs
  []
  (into [] (map (fn [[pid assignment]] {:participant_id pid,
                                        :map (str (seglob/get-estimated-map-name) assignment)})
                (select-map-bindings (seglob/get-belief-state "map-assignments")))))



(defn predict-m3-asist
  [pubs id elapsed_milliseconds]
  (let []
    (if (dplev :prediction :all) (println "New M3 prediction"))
    (predict/add-prediction-message
     pubs
     id
     {:action :m3
      :subject "participant_map"
      :object (get-map-assignment-beliefs)
      :hypothesis-id   "hyp0001-pending"
      :elapsed_milliseconds elapsed_milliseconds
      :hypothesis-rank 0
      :bounds [0 50]
      :agent-belief 0.4})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Marker semantics (M6)

;;; Semantics "A" participant was tols about semantics "A"
;;; Semantics "B" participant was told about semantics "B"
;;; If no marker is placed, we learn nothing about what the participant learned.
;;; All but one of the participants are told about semantics "A", or "B" and  precisely one about the other

;;; Get maximum belief assignment:
;;; There are 6 possible assignments= 1=AAB 2=ABA 3=BAA 4=BBA 5=BAB 6=ABB (only the first three appear in the training set)
;;; Initially all are equally probable
;;; Initially where all propbabilities are the same (no information) the probability of each possibility is the same: 0.166667
;;; But actually, the first three have a probability of 0.33333
;;; if p1 and p2 have the same assignment p3 will have the other semantics, for any assignment of p1, p2, and p3
;;; if p1 and p2 are different, p3 could be either one of the other, but in fact it must be "A"

;;; Each participant is given either semantics "A" 1=none, 2=normal, 3=critical or "B" 1=normal, 2=none, 3=critical
;;; Two participants are given the same semantics while the third has the other semantics.
;;; Initially the assignment to all participants is unknown and has equal probability of being semantics "A" or semantics "B".

(defn select-marker-bindings
  [markerbeliefs]
  (let [part-entropy (into [] (map (fn [[pid pdf]] [(ras/distribution-entropy pdf) [pid pdf]]) markerbeliefs))
        sortedbeliefs (sort (fn [x y] (< (first x) (first y))) part-entropy)
        maxresults (into [] (map (fn [[ent [pid pdf]]] [pid (key (apply max-key val pdf))]) sortedbeliefs))
        constrained (case (count markerbeliefs)
                      0
                      []

                      1
                      [(first maxresults)]

                      2
                      [(nth maxresults 0) (nth maxresults 1)]

                      3
                      (cond
                        ;; If the best two are different, the third must be "A" so that we have two "A"s
                        (not (= (second (first maxresults)) (second (second maxresults))))
                        [(nth maxresults 0) (nth maxresults 1) [(first (nth maxresults 2)) "A"]]
                        ;; If the first two are the same, and "A", the third must be "B"
                        (= (second (first maxresults)) "A")
                        [(first maxresults) (second maxresults) [(first (nth maxresults 2)) "B"]]
                        ;; Otherwise the first one is "B" so that last two must be "A"
                        :otherwise
                        [(first maxresults) [(first (nth maxresults 1)) "A"] [(first (nth maxresults 2)) "A"]])

                      maxresults)]
    constrained))

;;; (establish-prior-beliefs-about-marker-semantics ["p1" "p2" "p3"])
;;; (select-marker-bindings (establish-prior-beliefs-about-marker-semantics ["p1" "p2" "p"]))

(defn establish-prior-beliefs-about-marker-semantics
  [subjects]
  (let [prior-beliefs-about-marker-semantics
        (into {} (map
                  (fn [participant] {participant
                                     (cond  (== (count subjects) 1) {"A" 0.0, "B" 1.0}
                                            :otherwise {"A" (/ (- (count subjects) 1.0) (count subjects))
                                                        "B" (/ 1.0 (count subjects))})})
                  subjects))]
    (seglob/set-belief-state! "marker-semantics" prior-beliefs-about-marker-semantics)
    prior-beliefs-about-marker-semantics))

;;; (select-marker-bindings {"P1" {"A" 0.6, "B" 0.4} "P2" {"A" 0.3, "B" 0.7} "P3" {"A" 0.5, "B" 0.5}})
;;; (select-marker-bindings (seglob/get-belief-state "marker-semantics"))

(defn get-markerblock-assignment-beliefs
  []
  (into [] (map (fn [[pid assignment]] {:participant_id pid,
                                        :markerblocks assignment})
                (select-marker-bindings (seglob/get-belief-state "marker-semantics")))))


(defn interpret-marker-placement
  [pid type marker_x marker_y marker_z em]
  ;; belief state update for marker placement
  (let [previous-room (seglob/get-last-room pid)
        victims (if previous-room (victims/green-victims-in-room previous-room))
        role (seglob/get-role pid)]
    ;;(if previous-room (if (dplev :marker :all) (println "Marker type=" type "From room=" previous-room "victims=" (prop/prop-readable-form victims))))
    ;; now update belief state
    (let [prior-beliefs (seglob/get-belief-state "marker-semantics")
          pid-prior-belief (get prior-beliefs pid)
          best-belief (if pid-prior-belief (apply max-key val pid-prior-belief))]
      (if (not pid-prior-belief) (if (dplev :warn :all) (println "prior belief not found for particiant " pid "in" prior-beliefs)))

      (cond
        (not best-belief)
        nil

        ;; A medical specialist who find a normal victim will give care.  Marking indicates that there is no longer a
        ;; victim to save. Only marker for critical victims makes sense for a medical specialist.
        (and (= role "Medical_Specialist") (= type "Marker Block 1") (< (second best-belief) 0.8) (< em 300000))
        (do
          (seglob/update-belief! "marker-semantics" pid {"A" 0.9, "B" 0.1})
          (if (dplev :all) (println pid "used marker block 1 to indicate no victim => A marker semantics!")))

        (and (= role "Medical_Specialist") (= type "Marker Block 2") (< (second best-belief) 0.8))
        (do
          (seglob/update-belief! "marker-semantics" pid {"A" 0.1, "B" 0.9})
          (if (dplev :all) (println pid "used marker block 2 to indicate no victim => B marker semantics!")))

        (and (not (empty? victims)) (= type "Marker Block 1"))
        (if (< (second best-belief) 0.8)
          (do
            (seglob/update-belief! "marker-semantics" pid {"A" 0.1, "B" 0.9})
            (if (dplev :all) (println pid "used marker block 1 when a normal victim was present => B marker semantics!"))))

        (and (not (empty? victims)) (= type "Marker Block 2")  (< em 300000))  ; after 5 minutes, enough time to learn what others are doing
        (if (< (second best-belief) 0.8)
          (do
            (seglob/update-belief! "marker-semantics" pid {"A" 0.9, "B" 0.1})
            (if (dplev :all) (println pid "used marker block 2 when a normal victim was present => A marker semantics!"))))

        (and (empty? victims) (= type "Marker Block 1") (< em 300000))
        (if (< (second best-belief) 0.8)
          (do
            (seglob/update-belief! "marker-semantics" pid {"A" 0.9, "B" 0.1})
            (if (dplev :all) (println pid "used marker block 1 when a normal victim was not present => A marker semantics!"))))

        (and (not (empty? victims)) (= type "Marker Block 2"))
        (if (< (second best-belief) 0.8)
          (do
            (seglob/update-belief! "marker-semantics" pid {"A" 0.1, "B" 0.9})
            (if (dplev :all) (println pid "used marker block 2 when a normal victim was not present => B marker semantics!")))))))
  nil)

(defn predict-m6-asist
  [pubs id reason elapsed_milliseconds]
  (let [prior-beliefs (seglob/get-belief-state "marker-semantics")
        best-belief (if prior-beliefs (apply max (map (fn [[pid pdf]] (second (apply max-key val pdf))) prior-beliefs)))]
    (if (and best-belief (dplev :prediction :all)) (println "New M6 prediction"))
    (predict/add-prediction-message
     pubs
     id
     {:action :m6
      :subject "participant_blocks"
      :object (get-markerblock-assignment-beliefs)
      :hypothesis-id   "hyp0001-pending"
      :elapsed_milliseconds elapsed_milliseconds
      :reason reason
      :hypothesis-rank 0
      :bounds [0 50]
      :agent-belief best-belief})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Strategy

;;; Study-2 permits role chqngeing

(def ^:dynamic *current-strategy* :unknown)

;;; New strategy names :sss :hhh :mmm :mmh :mms :ssh :ssm :hhs :hhm :hsm

(defn role-strategy
  [roles]
  (case roles
    [1 1 1] :hsm ; :one-of-each
    [1 0 2] :ssh ; :h1s2
    [1 2 0] :mmh ; :h1m2
    [2 0 1] :hhs ; :h2s1
    [2 1 0] :hhm ; :h2m1
    [0 1 2] :ssm ; :m1s2
    [0 2 1] :mms ; :m2s1
    [3 0 0] :hhh ; :h3
    [0 3 0] :mmm ; :m3
    [0 0 3] :sss ; :s3
    :hsm))       ; :allroles

(defn establish-strategy
  [roles]
  (seglob/set-role-strategy (role-strategy roles))
  (if (dplev :strategy :all) (println "Strategy established=" (seglob/get-role-strategy))))

;;; Fin
