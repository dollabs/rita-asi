;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.measures
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
            [rita.state-estimation.rita-se-core :as rsc :refer :all] ; back off from refer :all
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.interventions :as intervene]
            [rita.state-estimation.markers :as markers]
            [rita.state-estimation.predictions :as predict]
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


;;; This is a Study 2 marker measure


(defn normalize-yaw
  [yaw]
  (let [corrected-yaw (+ yaw 90)]
    (cond (< corrected-yaw -179) (+ corrected-yaw 360)
          (> corrected-yaw 180) (- corrected-yaw 360)
          :otherwise corrected-yaw)))
;;;  (normalize-yaw -273)

(defn dxdzToAngleDegrees
  [dx dz]
  (let [dirn (cond
               (and (== dx 0) (== dz 0)) (do (when (dplev :warn :all) (println "dx and dz cannot both be zero to get a direction")) 0)
               (and (> dx 0) (== dz 0)) 0.0
               (and (== dx 0) (> dz 0)) 90.0
               (and (< dx 0) (== dz 0)) 180.0
               (and (== dx 0) (< dz 0)) 270.0
               (and (> dx 0) (> dz 0)) (* 180.0 (/ (Math/atan (/ dz (+ 0.0 dx))) 3.1415926536))
               (and (< dx 0) (> dz 0)) (+ 90.0 (- 90.0 (* 180.0 (/ (Math/atan (/ dz (- 0.0 dx))) 3.1415926536))))
               (and (< dx 0) (< dz 0)) (+ 180.0 (* 180.0 (/ (Math/atan (/ (- 0.0 dz) (- 0.0 dx))) 3.1415926536)))
               (and (> dx 0) (< dz 0)) (+ 270.0 (- 90.0 (* 180.0 (/ (Math/atan (/ (- 0.0 dz) dx)) 3.1415926536))))
               :otherwise 0.0)]
    (cond (> dirn 180) (- dirn 360)
          :otherwise dirn)))

(defn canonicalize-direction
  [angdirn]
  (cond (>= angdirn 180) (- angdirn 360)
        (<= angdirn -180) (+ angdirn 360)
        :otherwise angdirn))

;;; (def circletest [[2, 0] [2, 1] [1, 2] [0, 2] [-1, 2] [-2, 1] [-2, 0] [-2, -1] [-1, -2] [0, -2] [1, -2] [2, -1]])
;;; (when (dplev :all) (println (map (fn [p] (Math/round (apply dxdzToAngleDegrees p))) circletest)))

(def ^:dynamic *demeanor-training-data* [])

(defn reset-demeanor-training-data
  []
  (def ^:dynamic *demeanor-training-data* []))

(defn add-demeanor-training-data
  [newdata]
  (let [datastrings (into [] (map (fn [obj] (str obj)) newdata))
        csvstring (reduce (fn [v1 v2] (str v1 ", " v2)) datastrings)
        ;;lineasstring (with-out-str (println csvstring))
        ]
    (def ^:dynamic *demeanor-training-data* (conj *demeanor-training-data* csvstring))))

;;; (reset-demeanor-training-data)
;;; (add-demeanor-training-data [1 2 4.3 "True"])
;;; (add-demeanor-training-data [1.2 2.3 4.3 "False"])
;;; (exists-demeanor-training-data?)

(defn exists-demeanor-training-data?
  []
  (not (empty? *demeanor-training-data*)))

(defn save-demeanor-training-data
  [filename]
  (spit filename *demeanor-training-data*)  ; +++ Uncomment to gather demeanor data
  (reset-demeanor-training-data))

(defn interpret-demeanor
  [yaw motion-direction block-direction door-direction]
  (let [mdiff (Math/abs (canonicalize-direction (- motion-direction door-direction)))
        ydiff (Math/abs (canonicalize-direction (- yaw door-direction)))]
    (if ;;(and (< mdiff 35.9) (< ydiff 35.9))
        (or (< mdiff 30) (< ydiff 20))
      :enter-door
      :not-enter-door)))

(def ^:dynamic *m7-predictions-published* 0)

(defn reset-m7-predictions-published
  []
  (def ^:dynamic *m7-predictions-published* 0))

(defn get-m7-predictions-published
  []
  *m7-predictions-published*)

(defn inc-m7-predictions-published
  [& [n]]
  (def ^:dynamic *m7-predictions-published* (+ (or n 1) *m7-predictions-published*)))

;;; (reset-m7-predictions-published)
;;; (inc-m7-predictions-published)
;;; (inc-m7-predictions-published 5)
;;; (get-m7-predictions-published)

(defn predict-m7-asist
  [pubs id pid location elapsed_milliseconds door yorn reason ab]
  (let []
    (inc-m7-predictions-published)
    #_(when (dplev :prediction :all)    ;THIS WAS a Study2 thing
      (println "New M7 prediction (" (get-m7-predictions-published) ") so far"))
    (predict/add-prediction-message
     pubs
     id
     {:action (if yorn :m7_will_enter_room :m7_will_not_enter_room)
      :using location
      :subject pid
      :object door
      :hypothesis-id   "hyp0001-pending"
      :reason reason
      :elapsed_milliseconds elapsed_milliseconds
      :hypothesis-rank 0
      :bounds [0 50]
      :agent-belief ab})))

(defn maybe-report-m7-metric
  [pubs id participantid x y z oldx oldy oldz motion_x motion_y motion_z pitch yaw neartoportal role em]
  (let [m7-marker-prompt (seglob/get-next-m7-ground-truth-prompt em)]
    (if m7-marker-prompt
      (let [;; _ (when (dplev :all) (println "m7-marker-prompt=" m7-marker-prompt))
            {ret     (keyword "resolved_elapsed_time")
             tl_z    (keyword "Block Location: Z")
             gtval   (keyword "Ground Truth")
             ;; (keyword "Trial")
             pid     (keyword "Subject")
             tl_x    (keyword "Block Location: X")
             pname   (keyword "Door ID")
             setime  (keyword "Start Elapsed Time")
             measure (keyword "Measure")
             tid     (keyword "Trial ID")
             bl-type (keyword "Block Type")
             placed  (keyword "Block Placed By")} m7-marker-prompt
            tl-x (Integer/parseInt tl_x)
            tl-z (Integer/parseInt tl_z)]
    (if (and m7-marker-prompt
             (>= em (Integer/parseInt setime)) ; right time
             (= pid participantid))            ; right player
                                        ;(if (not (and (== x oldx) (== y oldy) (== z oldz)))
      (let [nearby-marker (markers/find-last-placed-marker-at  tl-x #_60 tl-z ["Marker Block 1" "Marker Block 2" "Marker Block 3"])
            {marker_x    :marker_x
             marker_y    :marker_y
             marker_z    :marker_z
             } nearby-marker
            neartoportal (or ;neartoportal
                             (vol/close-to-a-portal (or marker_x x) (or marker_z z) (or marker_y y) 3)
                             (vol/close-to-a-portal (or marker_x x) (or marker_z z) (or marker_y y) 6)
                             (vol/close-to-a-portal (or marker_x x) (or marker_z z) (or marker_y y) 8)
                             (vol/close-to-a-portal (or marker_x x) (or marker_z z) (or marker_y y) 10)
                             neartoportal)]
        (if nearby-marker
          (when (dplev :marker :all) (println "***** Marker found:" nearby-marker))
          (when (dplev :all) (println "***** No marker found near" tl-x tl-z 60)))
        (if (not neartoportal) (when (dplev :warn :all) (println "***** Portal not found for m7 near" x y z)))
        (seglob/set-next-m7-prompt-used!)
        (if nearby-marker
          (seglob/add-considered-marker! nearby-marker)
          (when (dplev :warn :all) (println "***** Nearby marker not found for prompt:" (pr-str m7-marker-prompt))))
        (let [ourpname (or (and neartoportal (eval/deref-field ['v-name] neartoportal :normal)) pname)
              _ (if (not (= ourpname pname))
                  (when (dplev :warn :all) (println "***** WARNING door name doesn't match: ours=" ourpname "prompt pname=" pname)))
              msbs (get (into {} (markers/select-marker-bindings (seglob/get-belief-state "marker-semantics"))) participantid)
              _ (when (dplev :warn :all) (println "maybe-report-m7-metric participant" participantid "marker-semantics=" msbs))
              nyaw (normalize-yaw yaw)
              {type        :type
               marker_x    :marker_x
               marker_y    :marker_y
               marker_z    :marker_z
               pem          :elapsed_milliseconds
               mpid        :pid
               placer-role :role} nearby-marker
              all-nearby-markers (if marker_x (markers/find-nearby-markers marker_x  marker_y marker_z 5 ["Marker Block 1" "Marker Block 2" "Marker Block 3"]))
              marker-types (markers/get-marker-types all-nearby-markers)
              _ (when (dplev :marker :all) (println "all-nearby-markers=" all-nearby-markers "marker-types=" marker-types))
              _ (if (> (count all-nearby-markers) 1)
                  (when (dplev :marker :all) (println "Found multiple markers of types:" marker-types)))
              callsign (seglob/pid2callsign (get m7-marker-prompt :subject)) ;; was (seglob/pid2callsign participantid)
              location {:location {:x tl-x
                                   :z tl-z
                                   :y 60
                                   :type bl-type
                                   :callsign placed}}
              {pxl 'tl-x pxr 'br-x pyl 'tl-y pyr 'br-y pzl 'tl-z pzr 'br-z} (if neartoportal (deref (:fields neartoportal)))
              p-x (if pxl (/ (+ @pxl @pxr) 2.0))
              p-z (if pyl (/ (+ @pyl @pyr) 2.0))
              motion-direction (dxdzToAngleDegrees motion_x motion_z)                   ; direction of motion
              block-direction  (dxdzToAngleDegrees (- tl-x x) (- tl-z z))               ; direction to the marker
              door-direction   (if p-x (dxdzToAngleDegrees (- p-x x) (- p-z z)))        ; direction to the door
              door-distance    (ras/straight-line-distance x 60 z p-x 60 p-z)
              demeanor (interpret-demeanor nyaw motion-direction block-direction door-direction)
              ]
          (if (or (= gtval "False") (= gtval "True"))
            (add-demeanor-training-data [(canonicalize-direction (- nyaw door-direction))
                                         (canonicalize-direction (- motion-direction door-direction))
                                         nyaw motion-direction door-direction door-distance gtval]))
          (when (dplev :all) (println "***** Handling m7 prompt: " m7-marker-prompt))
          ;; (when (dplev :all) (println "yaw=" nyaw "participant (" role ") at [" x z "] door at [" p-x p-z "] motion-direction=" motion-direction "block-direction=" block-direction "door-direction=" door-direction "demeanor=" demeanor))
          (cond (= participantid mpid)
                (predict-m7-asist pubs id participantid location em pname false [:marker-placed-by-same-participant] 0.8)

                (not (= role "Medical_Specialist"))
                (predict-m7-asist pubs id participantid location em pname false [:wrong-role-participant] 0.8)

                (or (and (or (= msbs "A") (> em 300000)) (= type "Marker Block 1") (not (= demeanor :enter-room)))
                    (and (= msbs "B") (= type "Marker Block 2") (not (= demeanor :enter-room)))
                    (= demeanor :not-enter-door))
                (let [ppubs (cond (and (= msbs "A") (= demeanor :not-enter-door) (= type "Marker Block 2"))
                                  (do
                                    (when (dplev :all) (println "Participant" participantid "ignoring marker block A->B"))
                                    #_(seglob/update-belief! "marker-semantics" participantid {"A" 0.1, "B" 0.9})
                                    #_(update-marker-semantics-and-report pubs id [:marker-interpretation] em)
                                    pubs))]
                  (predict-m7-asist ppubs id participantid location em pname false [:believes participantid [:indicates type :no-victim-inside]] 0.57))

                (or (and (or (= msbs "A") (> em 300000)) (= type "Marker Block 2"))
                    (and (= msbs "B") (= type "Marker Block 1"))
                    (= demeanor :enter-door))
                (let [ppubs (if (and (= msbs "B") (= demeanor :enter-door) (= type "Marker Block 2"))
                              (do
                                (when (dplev :all) (println "Participant" participantid "ignoring marker block B->A"))
                                #_(seglob/update-belief! "marker-semantics" participantid {"A" 0.9, "B" 0.1})
                                #_(update-marker-semantics-and-report pubs id [:marker-interpretation] em)
                                pubs))]
                  (predict-m7-asist ppubs id participantid location em pname true [:believes participantid [:indicates type :normal-victim-inside]] 0.69))

                ;; +++ add type 3 cases
                :else (predict-m7-asist pubs id participantid location em pname false nil 0.69))))
      pubs)))))
