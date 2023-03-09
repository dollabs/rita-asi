;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.markers
  "Observations about changes to marker states."
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
            ;;
            [rita.common.core :as rc :refer :all]
            [rita.common.surveys :as surveys]
            [rita.state-estimation.volumes :as vol :refer :all]
            ;[rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            [rita.state-estimation.teamstrength :as ts]
            [rita.state-estimation.ras :as ras]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.rita-se-core :as rsc]
            [rita.state-estimation.cognitiveload :as cogload]
            [rita.state-estimation.interventions :as intervene]
            [rita.state-estimation.anomalies :as anomaly]
            [rita.state-estimation.study2 :as study2]
            ;;
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

#_(in-ns 'rita.state-estimation.markers)

(declare select-marker-bindings)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Marker knowledge management


(defn marker-same-place-same-type?
  [m1 m2]
  (and (== (:marker_x m1) (:marker_x m2))
       (== (:marker_y m1) (:marker_y m2))
       (== (:marker_z m1) (:marker_z m2))
       (= (:type m1) (:type m2))))

(defn already-considered?
  [amarker]
  (some #(= amarker %) (seglob/get-considered-markers)))

;;; Is there already a marker at that location?
(defn same-location-marker?
  [amarker alistofmarkers]
  (let [{marker_x :marker_x
         marker_y :marker_y
         marker_z :marker_z} amarker]
    (some (fn [mn] (and (== marker_x (:marker_x mn))
                        (== marker_y (:marker_y mn))
                        (== marker_z (:marker_z mn))))
          alistofmarkers)))

(defn register-marker-placement
  [pid type marker_x marker_y marker_z em]
  (let [prior-added-markers (seglob/get-placed-markers)
        ;; exists-already (find-nearby-marker-of-type-1-or-2 marker_x marker_z 60 2 pid em)
        updated-placed-markers (conj prior-added-markers
                                     {:type type
                                      :marker_x marker_x
                                      :marker_y marker_y
                                      :marker_z marker_z
                                      :elapsed_milliseconds em
                                      :pid pid
                                      :role (seglob/get-role pid)})]
    ;;; +++anomaly we could check for a marker already there ...
    (when (dplev :marker :all) (println "marker registry:")
          (pprint updated-placed-markers))
    (seglob/set-placed-markers! updated-placed-markers)))

(defn register-marker-removal
  [pid type marker_x marker_y marker_z em]
  (let [prior-placed-markers (seglob/get-placed-markers)
        prior-removed-markers (seglob/get-removed-markers)
        removed-marker  {:type type
                         :marker_x marker_x
                         :marker_y marker_y
                         :marker_z marker_z
                         :elapsed_milliseconds em
                         :pid pid
                         :role (seglob/get-role pid)}
        updated-removed-markers (conj prior-removed-markers removed-marker)]
    (if (not (some #(marker-same-place-same-type? % removed-marker) prior-placed-markers))
      (anomaly/removal-of-nonexistant-marker pid marker_x marker_y marker_z type em))
    (if (and (some #(marker-same-place-same-type? % removed-marker) prior-placed-markers)
             (some #(marker-same-place-same-type? % removed-marker) prior-removed-markers))
      (anomaly/removal-of-already-removed-marker pid marker_x marker_y marker_z type em))

    (when (dplev :marker :all) (println "marker registry:")
          (pprint updated-removed-markers))
    (seglob/set-removed-markers! updated-removed-markers)))


;;; There are often multipls markers near a door.  To understand a players action, look at all marker types, not only one of them.
(defn find-nearby-markers
  [my_x my_y my_z maxdistance types]
  (let [nearloc (filter (fn [amarker]
                        (let [{marker_x :marker_x
                               marker_y :marker_y
                               marker_z :marker_z} amarker]
                          (and (<= (ras/straight-line-distance my_x my_y my_z marker_x marker_y marker_z) maxdistance)
                               amarker)))
                        (seglob/get-placed-markers))
        ;; _ (when (dplev :all) (println "markers near " my_x my_z "=" nearloc))
        ;; Now remove all but the most recent marker in a given location
        noburiedlist (loop [nearbymarkers nearloc
                            results []]
                       (if (empty? nearbymarkers)
                         results
                         (let [this-marker (first nearbymarkers)
                               remaining-markers (rest nearbymarkers)]
                           (if (same-location-marker? this-marker results)
                             (recur remaining-markers results) ; elide the old markers
                             (recur remaining-markers (conj results this-marker))))))
        ;; _ (when (dplev :all) (println "noburiedlist=" noburiedlist))
        markers-of-desired-type (into [] (filter (fn [amarker]
                                                   (and (some (partial = (:type amarker)) types) amarker))
                                                 noburiedlist))]
    markers-of-desired-type))

(defn get-marker-types
  [list-of-markers]
  (into #{} (map #(:type %) list-of-markers)))
<
(defn marker-type?
  [n mtypeset]
  (case n
    1 (mtypeset "Marker Block 1")
    2 (mtypeset "Marker Block 2")
    3 (mtypeset "Marker Block 3")
    nil))

(defn find-last-placed-marker-at
  [my_x #_my_y my_z types]
  (let [atloc (some (fn [amarker]
                      (let [{type :type
                             marker_x :marker_x
                             marker_y :marker_y
                             marker_z :marker_z
                             placedem :elapsed_milliseconds
                             mpid :pid
                             placer-role :role} amarker]
                        (and (== marker_x my_x)
                             (== marker_z my_z)
                             amarker)))
                    (seglob/get-placed-markers))
        mtype (if atloc (get atloc :type))]
    (if (some (partial = mtype) types) atloc)))

(defn update-marker-semantics-and-report
  [pubs id reason em]
  ;; Generate a prediction of marker semantics
  ;; Report
  pubs #_(if (= reason :placement-behavior)
    (study2/predict-m6-asist pubs id reason em))) ;Study2 behavior temporary removed for study3

(defn maybe-learn-marker-semantics
  [pub id playername x y z neartoportal whereIam em]
  (let [neartoportal (or neartoportal
                         (vol/close-to-a-portal x z y 3)
                         (vol/close-to-a-portal x z y 6)
                         (vol/close-to-a-portal x z y 8)
                         (vol/close-to-a-portal x z y 10))]
    (cond (not neartoportal)
          pub ;;(do (when (dplev :all) (println "Can't find portal near" x y z "@" whereIam)) pub)

          (and whereIam (global/RTobject? whereIam) (a-room? whereIam))
          (let [p-fields @(global/RTobject-fields neartoportal)
                posPx (/ (+ @(get p-fields 'tl-x) @(get p-fields 'br-x)) 2.0)
                posPz (/ (+ @(get p-fields 'tl-y) @(get p-fields 'br-y)) 2.0)
                posPy @(get p-fields 'tl-z)
                all-nearby-markers (if neartoportal (find-nearby-markers posPx 60 posPz 6 ["Marker Block 1" "Marker Block 2" "Marker Block 3"]))
                marker-types (get-marker-types all-nearby-markers)]
            (when (dplev :all) (println "****** In maybe-learn-marker-semantics: portal@" posPx posPy posPz "marker-types=" marker-types))
            ;; Here we know that our participant entered a room with markers at the door
            (cond
              (and (marker-type? 1 marker-types)
                   (marker-type? 2 marker-types))
              pub                       ; Maybe we can maybe reason about the most recent of the two and who placed it

              (marker-type? 1 marker-types) ; Argues for semantics B
              (do
                (seglob/update-belief! "marker-semantics" playername {"A" 0.1, "B" 0.9})
                (update-marker-semantics-and-report pub id [:entered-room :semantics-B-marker-interpretation] em))

              (marker-type? 2 marker-types) ; Argues for semantics A
              (do (seglob/update-belief! "marker-semantics" playername {"A" 0.9, "B" 0.1})
                  (update-marker-semantics-and-report pub id [:entered-room :normal-marker-interpretation] em))

              :otherwise pub)))))


;;; Elided for now.  the placement version is in study2
(defn interpret-marker-removal
  [pid type marker_x marker_y marker_z em]
  nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Marker message handling

;;; "Event:MarkerPlaced"

;;; sub_type: Event:MarkerPlaced
;;; tbm=
;;; {:data {:mission_timer 10 : 51,
;;;         :elapsed_milliseconds 252699.0,
;;;         :playername Player724,
;;;         :type Marker Block 3,
;;;         :marker_x -2090.0,
;;;         :marker_y 60.0,
;;;         :marker_z 43.0},
;;;  :header {:timestamp 2021-06-29T21:01:44.351Z,
;;;           :message_type event,
;;;           :version 0.6},
;;;  :msg {:experiment_id bc50aea2-b913-4889-9fbd-f4ba2a72763c,
;;;        :trial_id 87da6996-7642-49bb-b114-8e428c87b5de,
;;;        :timestamp 2021-06-29T21:01:44.352Z,
;;;        :source simulator,
;;;        :sub_type Event:MarkerPlaced,
;;;        :version 0.5}}

(defn rita-handle-MarkerPlaced-message
  [tbm tb-version]
  (let [data (:data tbm)
        header (:header tbm)
        msg (:msg tbm)
        {em :elapsed_milliseconds
         marker_x :marker_x
         marker_y :marker_y
         marker_z :marker_z
         type :type} data
        pid (seglob/get-participant-id-from-data data)]
    (if em (seglob/update-last-ms-time em))
    (when (dplev :action :all)
      (println "Participant " pid "placed marker" type "at x=" marker_x "y=" marker_y "z=" marker_z))
    (ts/increment-strength-data-for pid (seglob/get-trial-number) em  :markers-placed)
    (register-marker-placement pid type marker_x marker_y marker_z em)
    #_(study2/interpret-marker-placement pid type marker_x marker_y marker_z em) ; Study2 disabled
    (update-marker-semantics-and-report nil nil :placement-behavior em)))


;;; message_type= event sub_type= Event:MarkerRemoved
;;; tbm= {:data
;;;        {:elapsed_milliseconds 130293.0,
;;;         :marker_z 39.0,
;;;         :mission_timer 12 : 53,
;;;         :marker_x -2138.0,
;;;         :type blue_abrasion, ; also :type green_novictim,
;;;                                     :type blue_novictim,
;;;                                     :type red_criticalvictim,
;;;                                     :type blue_bonedamage,
;;;                                     :type green_abrasion
;;;         :marker_y 60.0,
;;;         :playername Regor_Ffrac,
;;;         :participant_id x202203022},
;;;       :header {
;;;         :version 1.1,
;;;         :message_type event,
;;;         :timestamp 2022-03-02T18:37:33.547Z},
;;;         :msg {
;;;           :trial_id 55cd3a31-548a-4d5b-9376-6f78a93545d6,
;;;           :timestamp 2022-03-02T18:37:33.548Z,
;;;           :version 2.1,
;;;           :experiment_id f97a5942-790d-4b85-b5cb-fc648bba3cc1,
;;;           :sub_type Event:MarkerRemoved,
;;;           :source simulator}}

(defn rita-handle-marker-removal-message
  [tbm tb-version]
  (let [data (:data tbm)
        header (:header tbm)
        msg (:msg tbm)
        {em :elapsed_milliseconds
         marker_x :marker_x
         marker_y :marker_y
         marker_z :marker_z
         type :type} data
        pid (seglob/get-participant-id-from-data data)]
    (if em (seglob/update-last-ms-time em))
    (when (dplev :action :marker :all)
      (println "Participant " pid "removed marker" type "at x=" marker_x "y=" marker_y "z=" marker_z))
    (ts/increment-strength-data-for pid (seglob/get-trial-number) em  :markers-removed)
    (register-marker-removal pid type marker_x marker_y marker_z em)
    (interpret-marker-removal pid type marker_x marker_y marker_z em)
    (update-marker-semantics-and-report nil nil :removal-behavior em))
  　nil)


;;; Fin
