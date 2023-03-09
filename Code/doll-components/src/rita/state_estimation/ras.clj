;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.ras
  "RITA Algorithmic Support - a collection of useful algorithms."
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
;;;         [rita.common.surveys :as surveys]
;;;         [rita.state-estimation.volumes :as vol :refer :all]
;;;         [rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
;;;         [rita.state-estimation.rlbotif :as rlbotif]
;;;         [rita.state-estimation.statlearn :as slearn]
;;;         [rita.state-estimation.multhyp :as mphyp]
;;;         [rita.state-estimation.ritamessages :as ritamsg]
;;;         [rita.state-estimation.rita-se-core :as rsc :refer :all] ; back off from refer all +++
;;;         [rita.state-estimation.cognitiveload :as cogload]
;;;         [rita.state-estimation.interventions :as intervene]
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

#_(in-ns 'rita.state-estimation.ras)

(defn strfromsymbol
  [sym]
  (cond (string? sym) (string/trim sym)
        (keyword? sym) (string/trim (subs (str sym) 1))
        (symbol? sym) (string/trim (str sym))
        :otherwise sym))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;  Time

(def prvtime false)

(defn parse-mission-time
  "Convert the mission time string to an integer representing seconds remaining"
  [mt-string]
  (if (and mt-string
           (not (= mt-string "Mission Timer not initialized."))
           (string? mt-string))
    (let [[mins secs]
          (map (fn [x]
                 (Integer/parseInt (string/trim x)))
               (string/split mt-string #":"))
          seconds (+ (* mins 60) secs)]
      (when (not prvtime)
        (def prvtime true)
        (when (dplev :io :all) (println "Mission-time set to" seconds "seconds")))
      seconds)
    0))

(defn compute-epoch-from-ms
  [em]
  (if (< em (* 2 60 1000)) 0 ; Planning epoch
      (+ 1 (int (quot (- em (* 2 60 1000)) (* 3 60 1000))))))


;;; Time from timestamp

#_(def foo 0)

(defn timeinmilliseconds
  [RFC3339-timestring]
  #_(when (mod foo 2000)
    (println "In timeinmilliseconds with ts=" RFC3339-timestring))
  (let [dt (clojure.instant/read-instant-date RFC3339-timestring)
        mstime (java.util.Date/.getTime dt)]
    #_(when (mod foo 2000)
      (println "In timeinmilliseconds with mstime=" mstime))
    #_(def foo (+ foo 1))
    mstime))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Reasoning about knowledge

(def log2c (Math/log 2))

(defn description-length
  [prop]
  (- (/ (Math/log prop) log2c)))

(defn log2
  [n]
  (/ (Math/log n) log2c))

;;; (distribution-entropy {"foo" 0.1, "bar" 0.9}) => 0.4689955935892812
(defn distribution-entropy
  [pdf]
  (let [probabilities (vals pdf)
        sumdist (apply + probabilities)
        normalized (map (fn [pp] (/ pp sumdist)) probabilities)] ; normalize just in case -- should always be normalized
    (- (apply + (map (fn [pp] (* pp (log2 pp))) normalized)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; 3D computation

(defn straight-line-distance
  [x1 y1 z1 x2 y2 z2]
  (if (not (and (number? x1)(number? y1)(number? z1)(number? x2)(number? y2)(number? z2)))
    (do (when (dplev :error :all) (println "****** ERROR: In straight-line-distance with x1y1z1x2y2z2=" x1 y1 z1 x2 y2 z2))
        999999.0) ; then return a large value
    (let [xdif (- x2 x1)
          ydif (- y2 y1)
          zdif (- z2 z1)]
      (Math/sqrt (+ (* xdif xdif) (* ydif ydif) (* zdif zdif))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; statistics over sequences

(defn average
  [lst]
  (/ (reduce + lst) (count lst)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Data structure algorithms

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Given a sequence of sequences, like [[1 2 3] [1 3 2] [2 3 4] [2 5 6] [3 1 2]]
;;; produces a sequence in which each head of the sebsequence is associated with (mergefn matches)
;;; Example: (into {} (merge-data-by-head [[1 2 3] [1 3 2] [2 3 4] [2 5 6] [3 1 2]] identity))

(defn merge-data-by-head
  [data mergefn]
  (when (dplev :all)
    (println "in merge-data-by-head with data:")
    (pprint data))
  (loop [results []
         values data]
    (if (empty? values)
      (into {} results)
      (let [match (first (first values))
            matches (filter (fn [[head data]] (= head match)) values)
            remaining (filter (fn [[head data]] (not (= head match))) values)
            newres (mergefn matches)]
        (recur (conj results [match newres]) remaining)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Floyd-Warshall


(def INF 9999.0)

(defn get-INF
  []
  INF)

(defn INF?
  [x]
  (== x INF))

(defn apsp
  [cmap]
  (let [num-vertices (count cmap)
        cached-apsp-file (seglob/get-building-model-apsp-pathname)
        data (if (.exists (io/file cached-apsp-file)) (read-string (slurp cached-apsp-file)))]
    (if data
      (do (if (dplev :all :apsp) (pprint data))
          data)
      ;; Initialize the working array
      (let [onepercent (max 1 (int (/ num-vertices 100)))
            dist (make-array Float/TYPE num-vertices num-vertices)]
        ;; Load working array
        (dotimes [i num-vertices]
          (dotimes [j num-vertices]
            (aset-float dist i j (nth (nth cmap i) j))))
        ;; Floyd-Warshall
        (println "apsp calculation - go get some sleep" num-vertices)
        (dotimes [k num-vertices]
          (dotimes [i num-vertices]
            (dotimes [j num-vertices]
              (if (< (+ (aget dist i k) (aget dist k j)) (aget dist i j))
                (aset-float dist i j (+ (aget dist i k) (aget dist k j))))))
          (if (= (mod k onepercent) 0)
            (println "*** Calculating apsp " (int (/ (* 100 k) num-vertices)) "percent")))
        ;; Unload result
        (println "Saving apsp data to " cached-apsp-file)
        (let [dataresult (into [] (map (fn [i]
                                         (map (fn [j] (aget dist i j)) (range num-vertices)))
                                       (range num-vertices)))
              _ (spit cached-apsp-file dataresult)
              reread (read-string (slurp cached-apsp-file))]
          (if (not (= dataresult reread))
            (println "cached-apsp-file didn't read back correctly"))
          dataresult)))))

(defn printGraph
  "Prints out an apsp distance map. prints out decimal part to save printout space."
  [cmap]
  (when (dplev :apsp :all)
    (let [num-vertices (count cmap)]
      (dotimes [i num-vertices]
        (dotimes [j num-vertices]
          (if (= (nth (nth cmap i) j) INF)
            (print " INF ")
            (print (format "%3d" (int (nth (nth cmap i) j))) " ")))
        (println)))))

;;; Test for apsp
(defn apsp-test
  []
  (let [graph [[0.00  5.00  INF  10.00]
               [INF   0.00  3.00  INF]
               [INF   INF   0.00  1.00]
               [INF   INF   INF   0.00]]
        correctSolution [[0.00  5.00  8.00  9.00]
                         [INF   0.00  3.00  4.00]
                         [INF   INF   0.00  1.00]
                         [INF   INF   INF  0.00]]
        solution (apsp graph)]
    (if (not (= solution correctSolution))
      (do
        (when (dplev :apsp :all)
          (println "Input graph:")
          (printGraph graph)
          (println "Output solution:")
          (printGraph solution))
        :failed))))

;;; (apsp-test)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Statistical

(defn mean
  [coll]
  (if (empty? coll)
    0
    (let [sum (apply + coll)
          count (count coll)]
      (if (pos? count) (float (/ sum count)) 0.0))))

(defn standard-deviation
  [coll]
  (if (empty? coll)
    0
    (let [avg (mean coll)
          squares (for [x coll] (let [x-avg (- x avg)] (* x-avg x-avg)))
          total (count coll)]
      (if (>= total 2)
        (-> (/ (apply + squares) (- total 1))
            (Math/sqrt))
        0))))                              ; not enough data to do better, only one sample!

(defn variance
  [coll]
  (let [sd (standard-deviation coll)]
    (* sd sd)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Volume geometry

(defn coordinates-in-volume
  [x y z avolume]
  (let [x2tl (eval/deref-field ['tl-x] avolume :normal)
        y2tl (eval/deref-field ['tl-y] avolume :normal)
        z2tl (eval/deref-field ['tl-z] avolume :normal)
        x2br (eval/deref-field ['br-x] avolume :normal)
        y2br (eval/deref-field ['br-y] avolume :normal)
        z2br (eval/deref-field ['br-z] avolume :normal)]
    (if (or (not (number? x))
            (not (number? y))
            (not (number? z))
            (not (number? x2tl))
            (not (number? y2tl))
            (not (number? z2tl))
            (not (number? x2br))
            (not (number? y2br))
            (not (number? z2br)))
      (when (dplev :error :all) (println "Something wrong with" avolume x y z x2tl y2tl z2tl x2br y2br z2br))
      (and
       (>= x x2tl)(>= y y2tl)(>= z z2tl)
       (<= x x2br)(<= y y2br)(<= z z2br)
       avolume))))

(defn coordinates-at-least-partially-in-volume
  [x y z avolume]
  (let [x2tl (eval/deref-field ['tl-x] avolume :normal)
        y2tl (eval/deref-field ['tl-y] avolume :normal)
        z2tl (eval/deref-field ['tl-z] avolume :normal)
        x2br (eval/deref-field ['br-x] avolume :normal)
        y2br (eval/deref-field ['br-y] avolume :normal)
        z2br (eval/deref-field ['br-z] avolume :normal)]
    (and (or
          (and (>= x x2tl)(>= y y2tl)(>= z z2tl))
          (and (<= x x2br)(<= y y2br)(<= z z2br)))
         avolume)))

;;; +++ bring over functions atr this level later +++

;;; Fin
