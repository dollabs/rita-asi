;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.generative-planner.planning-support-algorithms
  "RITA Import Minecraft World."
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
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [rita.common.core :as rc :refer :all]
            [rita.state-estimation.volumes :as vol :refer :all]
            [rita.state-estimation.volumes :as imw :refer :all]
            ;; [rita.generative-planner.generative-planner :as amg :refer :all]
            [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
            [pamela.tools.belief-state-planner.montecarloplanner :as bs]
            [pamela.tools.belief-state-planner.ir-extraction :as irx]
            [clojure.java.io :as io])
  (:gen-class)) ;; required for uberjar

#_(in-ns 'rita.generative-planner.planning-support-algorithms)

;;;(def INF 99999.0)

#_(defn apsp
  [cmap]
  ;; Initialize the working array
  (let [num-vertices (count cmap)
        dist (make-array Float/TYPE num-vertices num-vertices)]
    ;; Load working array
    (dotimes [i num-vertices]
      (dotimes [j num-vertices]
        (aset-float dist i j (nth (nth cmap i) j))))
    ;; Floyd-Warshall
    (dotimes [k num-vertices]
      (dotimes [i num-vertices]
        (dotimes [j num-vertices]
          (if (< (+ (aget dist i k) (aget dist k j)) (aget dist i j))
            (aset-float dist i j (+ (aget dist i k) (aget dist k j)))))))
    ;; Unload result
    (map (fn [i]
          (map (fn [j] (aget dist i j)) (range num-vertices)))
         (range num-vertices))))

#_(defn printGraph
  [cmap]
  (let [num-vertices (count cmap)]
    (dotimes [i num-vertices]
      (dotimes [j num-vertices]
        (if (= (nth (nth cmap i) j) INF)
          (print " INF  ")
          (print (format "%3.2f" (double (nth (nth cmap i) j))) " ")))
      (println))))

;;; Driver code
#_(defn test
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
        (printGraph graph)
        (println "Input graph:")
        (println "Output solution:")
        (printGraph solution)
        :failed))))

;;; (test)

;;; Fin
