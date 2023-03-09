;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.statlearn
  "RITA Statistical Learning."
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
            [rita.state-estimation.ras :as ras]
            [random-seed.core :refer :all]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [rita.common.core :as rc :refer :all]
            ;;[rita.common.surveys :as surveys]
            ;;[rita.state-estimation.volumes :as vol :refer :all]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
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

#_(in-ns 'rita.state-estimation.statlearn)

;;; Put back format version 1.0 as a comment +++

(defrecord StatData2 [format-version
                      ;;meta-data {:experiment-id,:trial-id,:testbed-version,:group-number,:trial-number,:name,:experiment-author
                      ;;:experiment-mission,:experimenter,:condition,:notes,:study-number,:map-name}
                      meta-data
                      subjects          ; vector of participant-ids
                      ;; room-statistics {"Medical-Specialist"     {room-visit-order [], room-enter-order [], room-room-times [] time-in-room []},
                      ;;                  :hazard {room-visit-order [], room-enter-order [], room-room-times [] time-in-room []},
                      ;;                  :search {room-visit-order [], room-enter-order [], room-room-times [] time-in-room []}}
                      room-statistics
                      ;; role-time {:pid1 {:role-med-time 0, :role-hazard-time 0, :role-search-time 0} ...}
                      role-time
                      ;; search-role {:pid1 {:elapsed-time-array [], :victims-moved []} ...}
                      search-role
                      ;; hazard-role {:pid1 {elapsed-time-array [], :rv []} ...} rv=[rubble-destroyed, victims-revealed]
                      hazard-role
                      ;; medic-role {:pid1 {elapsed-time-array [], :victim-saved-points []} ...}
                      medic-role])

;;;(def format-version "rita-sl-data-1.0") ; Study 1 single player, single role
(def format-version "rita-sl-data-2.0") ; Study 2 added multiple subjects and multiple roles

(defn initialize-stat-record
  [experiment-id tb-version trial-id grp trialno name
   expt-auth expt-mission experimenter
   condition notes study map-name subj-vec]
  (seglob/set-stat-record! (seglob/StatData2.
                            format-version ; format-version
                            ;; meta-data
                            {:experiment-id experiment-id,
                             :testbed-version tb-version,
                             :trial-id trial-id,
                             :group-number grp,
                             :trial-number trialno,
                             :name name,
                             :experiment-author expt-auth,
                             :experiment-mission expt-mission,
                             :experimenter experimenter,
                             :condition condition,
                             :notes notes,
                             :initial-role-strategy (seglob/get-role-strategy)
                             :study-number study
                             :map-name map-name}
                            subj-vec        ; subjects
                            ;; room-statistics
                            (into {} (map (fn [role]
                                            {role {:room-visit-order (atom []),
                                                   :room-enter-order (atom []),
                                                   :room-room-times (atom []),
                                                   :time-in-room (atom [])}})
                                          ["Medical_Specialist"
                                           "Hazardous_Material_Specialist"
                                           "Search_Specialist"
                                           "Transport_Specialist"
                                           "Engineering_Specialist"
                                           "None"]))
                            ;; role-time
                            (into {} (map (fn [pid] ; Array of pairs [starttime duration]
                                            {pid {"Search_Specialist" (atom []),
                                                  "Medical_Specialist" (atom []),
                                                  "Hazardous_Material_Specialist" (atom [])
                                                  "Transport_Specialist" (atom [])
                                                  "Engineering_Specialist" (atom [])
                                                  "None" (atom [])}})
                                          subj-vec))
                            ;; search-role/Evacuator
                            (into {} (map (fn [pid]
                                            {pid {:elapsed-time-array (atom [0]),
                                                  :victims-moved (atom[0])
                                                  :markers-placed (atom[0])
                                                  :markers-removed (atom[0])}})
                                          subj-vec))
                            ;; hazard-role/Engineer
                            (into {} (map (fn [pid]
                                            {pid {:elapsed-time-array (atom [0]),
                                                  :rv (atom [[0, 0]])
                                                  :victims-moved (atom[0])
                                                  :markers-placed (atom[0])
                                                  :markers-removed (atom[0])}})
                                          subj-vec))
                            ;; medic-role
                            (into {} (map (fn [pid]
                                            {pid {:elapsed-time-array (atom [0]),
                                                  :victim-saved-points (atom [0])
                                                  :markers-placed (atom[0])
                                                  :markers-removed (atom[0])}})
                                          subj-vec)))))

(defn reset-stat-record
  []
  (seglob/set-stat-record! nil)
  (seglob/set-learned-participant-model! "../data/default.lpm")
  (seglob/set-rita-temporal-data! nil))

(defn get-experiment-mission
  []
  (if seglob/*stat-record*
    (:experiment-id (.meta-data seglob/*stat-record*))))

(defn get-room-visited-order
  [role]
  (if seglob/*stat-record*
    (:room-visit-order (get (.room-statistics seglob/*stat-record*) role))))

(defn add-role-time!
  [pid role start duration]
  (if seglob/*stat-record*
    (let [rt (.role-time seglob/*stat-record*)
          pidrt (get rt pid)
          previous (get pidrt role)]
      (cond
        (string? previous)
        (when (dplev :all) (println "****** In add-role-time! pid=" pid "role=" role "start=" start "duration=" duration "previous=" previous))

        (instance? clojure.lang.Atom previous)
        (reset! previous (conj @previous [start duration]))

        (empty? previous)
        (when (dplev :all) (println "****** In add-role-time! pid=" pid "role=" role "start=" start "duration=" duration "previous=" previous))

        :otherwise
        (when (dplev :all) (println "****** In add-role-time! pid=" pid "role=" role "start=" start "duration=" duration "previous=" previous))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; What can be achieved in the three roles!

(defn add-score!
  [pid score etimsecs scoreboard]
  ;;(when (dplev :all) (println "add score pid=" pid " score=" score "et=" etimsecs))
  (if seglob/*stat-record*
    (let [pid (seglob/strfromsymbol pid) ; check correct format
          med (get (.medic-role seglob/*stat-record*) pid)]
      (if (not med)
        (when (dplev :all) (println "pid=" pid "**** not found **** in medic-role=" (.medic-role seglob/*stat-record*) "scoreboard=" scoreboard))
        (let [vsp @(:victim-saved-points med)
              etm @(:elapsed-time-array med)]
          (when (not (== score (last vsp))) ; score didn't change for this participant
            (reset! (:elapsed-time-array med) (conj etm etimsecs))
            (reset! (:victim-saved-points med) (conj vsp score))))))))

(defn add-moved!
  [pid etimsecs at vid]
  (if seglob/*stat-record*
    (let [pid (seglob/strfromsymbol pid) ; check correct format
          plc (get (.search-role seglob/*stat-record*) pid)]
      (if (not plc)
        (when (dplev :all) (println "pid=" pid "**** not found **** in search-role=" (.search-role seglob/*stat-record*)))
        (let [vmv @(:victims-moved plc)
              etm @(:elapsed-time-array plc)]
          (reset! (:elapsed-time-array plc) (conj etm etimsecs))
          (reset! (:victims-moved plc) (conj vmv (+ (last vmv) 1))))))))

(defn add-cleared!
  [pid etimsecs at]
  (if seglob/*stat-record*
    (let [pid (seglob/strfromsymbol pid) ; check correct format
          haz (get (.hazard-role seglob/*stat-record*) pid)]
      (if (not haz)
        (when (dplev :all) (println "pid=" pid "not found in hazard-role=" (.hazard-role seglob/*stat-record*)))
        (let [dra @(:rv haz)
              etm @(:elapsed-time-array haz)]
          (reset! (:elapsed-time-array haz) (conj etm etimsecs))
          (reset! (:rv haz) (conj dra [(+ (first (last dra)) 1) 0])))))))

(defn set-room-visited-order
  [role order]
  (if (and role seglob/*stat-record*)
    (reset! (:room-visit-order (get (.room-statistics seglob/*stat-record*) role))  order)))

(defn last-visited-room
  [role]
  ;;(when (dplev :all) (println "last-visited-room: role=" role "room-statistics=" (.room-statistics  seglob/*stat-record*)))
  (if seglob/*stat-record*
    (let [rolestats (get (.room-statistics  seglob/*stat-record*) role)
          _ (if (not rolestats) (when (dplev :all) (println "***** PROBLEM, role" role "not found in" seglob/*stat-record*)))
          lvroom (if rolestats @(:room-visit-order rolestats))]
      (if (not (empty? lvroom))
        (last lvroom)))))

(defn add-room-visited
  [role aroom]
  (if (and role seglob/*stat-record*)
    (reset! (:room-visit-order (get (.room-statistics seglob/*stat-record*) role))
            (conj @(:room-visit-order (get (.room-statistics seglob/*stat-record*) role))
                  aroom))))

(defn get-time-in-room
  [role]
  (if (and role seglob/*stat-record*)
    @(:time-in-room (get (.room-statistics seglob/*stat-record*) role))))

(defn get-room-enter-order
  [role]
  (if (and role seglob/*stat-record*)
    @(:room-enter-order (get (.room-statistics seglob/*stat-record*) role))))

(defn set-room-enter-order
  [role order]
  (if (and seglob/*stat-record* role)
    (reset! (:room-enter-order (get (.room-statistics seglob/*stat-record*) role))
            order)))

(defn last-in-room
  [role]
  ;;(when (dplev :all) (println "role=" role "stats=" (.room-statistics seglob/*stat-record*)))
  (if (and role seglob/*stat-record*)
    (let [stats (get (.room-statistics seglob/*stat-record*) role)
          inroom (if stats @(:room-enter-order stats))]
      (if (not (empty? inroom))
        (last inroom)))))

(defn add-room-enter
  [role aroom]
  (if (and role seglob/*stat-record*)
    (reset! (:room-enter-order (get (.room-statistics seglob/*stat-record*) role))
            (conj @(:room-enter-order (get (.room-statistics  seglob/*stat-record*) role))
                  aroom))))

(defn get-time-in-room
  [role]
  (if (and role seglob/*stat-record*)
    @(:time-in-room (get (.room-statistics  seglob/*stat-record*) role))))

(defn set-time-in-room
  [role tir]
  (if (and role seglob/*stat-record*)
    (reset! (:time-in-room (get (.room-statistics  seglob/*stat-record*) role))
            tir)))

(defn add-time-in-room
  [role tir]
  (if (and role seglob/*stat-record*)
    (reset! (:time-in-room (get (.room-statistics seglob/*stat-record*) role))
            (conj @(:time-in-room (get (.room-statistics seglob/*stat-record*) role))
                  tir))))

;(defn set-player-name [name] (reset! (.player-name  seglob/*stat-record*) name))


(defn distance-between-rooms
  [aroom anotherroom]
  (let [rdmap (get (seglob/get-room-apsp) aroom)
        _ (if (empty? rdmap) (when (dplev :error :all) (println "In distance-between-rooms: room-apsp failure" aroom (seglob/get-room-apsp))))
        dist (get rdmap anotherroom :notfound)]
    (if (= dist :notfound)
      (when (dplev :error :all) (println "room-apsp failure from" aroom anotherroom))
      dist)))


(defn digest-collected-data
  [collected]
  ;; (when (dplev :all) (println "In digest-collected-data with collected=" collected))
  (let [rstats (.room-statistics collected)]
    (doseq [[role collected] rstats]
      (let [tir @(:time-in-room collected)
            rvo @(:room-visit-order collected)
            [room-visit-order room-room-time]
              (loop [room-visit-order [] room-room-time [] start-time nil time-in-room tir room-visit-seq rvo]
              ;; (when (dplev :all) (println "digest-collected-data" room-visit-order room-room-time start-time time-in-room room-visit-seq))
              (if (empty? room-visit-seq)
                [room-visit-order room-room-time]
                (let [prevroom (if (not (empty? room-visit-order))
                                 (last room-visit-order))
                      [room arriveat] (first room-visit-seq)
                      roomtime (if (and prevroom (= prevroom (first (first time-in-room))))
                                 (second (first time-in-room))
                                 0)
                      ;; _ (when (dplev :all) (println "prevroom=" prevroom "roomtime=" roomtime))
                      trajettime (if prevroom
                                   (max 30000 (- (- arriveat start-time) roomtime))) ; why do we need the max? +++
                      dist (if prevroom (distance-between-rooms prevroom room))
                      dist (if (number? dist) dist 0)]
                  (recur (conj room-visit-order room) ; room-visit-order
                         (if prevroom
                           (conj room-room-time [prevroom room trajettime dist (/ dist (/ trajettime 1000.0))])
                           room-room-time)            ; room-room-time
                         arriveat                     ; start-time
                         (if (and prevroom (= prevroom (first (first time-in-room))))
                           (rest time-in-room)
                           time-in-room)              ; time-in-room
                         (rest room-visit-seq)))))]   ; room-visit-seq
        ;; (when (dplev :all) (println "room-visit-order=" room-visit-order))
        ;; (when (dplev :all) (println "room-room-time=" room-room-time))
        (reset! (:room-visit-order collected) room-visit-order)
        (reset! (:room-room-times collected) room-room-time)))
    nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Reading and writing stat digested and undigested data


(defn read-stat-data
  [filename]
  (if (not (.exists (clojure.java.io/as-file filename)))
    (do (when (dplev :all) (println "*** File not found:" filename)) nil)
    (let [data (read-string (slurp filename))]
      (if (and data (= (:format-version data) format-version))
        data
        (do (when (dplev :all) (println "Data format (" (:format-version data)
                     ") does not match (" format-version ")")) nil)))))

(defn subjects-string
  [subjects-vec]
  (apply str (map (fn [subj] (str "-" (str subj))) subjects-vec)))

(defn deatom-statistics
  [stats]
  (into {}
        (map
         (fn [[role stat]] {role {:room-visit-order @(:room-visit-order stat)
                                  :room-enter-order @(:room-enter-order stat)
                                  :room-room-times  @(:room-room-times stat)
                                  :time-in-room     @(:time-in-room stat)}})

         stats)))

(defn deatom-roletime
  [rts]
  (into {}
        (map
         (fn [[pid rt]] {pid {"Search_Specialist" @(get rt "Search_Specialist")
                              "Transport_Specialist" @(get rt "Transport_Specialist")
                              "Medical_Specialist" @(get rt "Medical_Specialist")
                              "Engineering_Specialist" @(get rt "Engineering_Specialist")
                              "Hazardous_Material_Specialist" @(get rt "Hazardous_Material_Specialist")}})
         rts)))

(defn deatom-search
  [searches]
  (into {}
        (map
         (fn [[pid search]] {pid {:elapsed-time-array @(:elapsed-time-array search)
                                  :victims-moved @(:victims-moved search)}})
         searches)))

(defn deatom-hazard
  [hazards]
  (into {}
        (map
         (fn [[pid hazard]] {pid {:elapsed-time-array @(:elapsed-time-array hazard)
                                  :rv @(:rv hazard)}})
         hazards)))

(defn deatom-medic
  [medics]
  (into {}
        (map
         (fn [[pid medic]] {pid {:elapsed-time-array @(:elapsed-time-array medic)
                                 :victim-saved-points @(:victim-saved-points medic)}})
         medics)))

(defn write-stat-data
  []
  (digest-collected-data seglob/*stat-record*)
  (let [d seglob/*stat-record*
        data {:format-version (.format-version d)
              :meta-data (.meta-data d),
              :subjects (.subjects d),
              :room-statistics (deatom-statistics (.room-statistics d)),
              :role-time (deatom-roletime (.role-time d)),
              :search-role (deatom-search (.search-role d)),
              :hazard-role (deatom-hazard (.hazard-role d)),
              :medic-role (deatom-medic (.medic-role d))}
        filename (or seglob/*rita-temporal-data*
                     (str  (:trial-id (.meta-data d)) "-"
                           (:study-number (.meta-data d)) "-"
                           (:trial-number (.meta-data d))
                           (subjects-string (.subjects d)) ".edn"))]
    (when (dplev :all) (println "Writing training data output to" filename))
    (spit filename (pr-str data))
    (if (not (= data (read-stat-data filename)))
      (when (dplev :all) (println "Data failed to read back correctly (" (or seglob/*rita-temporal-data* filename) ")")))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Learning models

(defn make-ngrams
  [list-of-values n]
  (loop [pairs []
         values list-of-values]
    (if (< (count values) n)
      pairs
      (recur (conj pairs [(into [] (take (- n 1) values)) (nth values (- n 1))]) (rest values)))))

;;; (make-trigrams  [1 2 3 4 5 6 7 8 9]) =>
;;;   [[[1 2] 3] [[2 3] 4] [[3 4] 5] [[4 5] 6] [[5 6] 7] [[6 7] 8] [[7 8] 9]]

(defn make-trigrams
  [list-of-values]
  (make-ngrams list-of-values 3))

;;;  (make-bigrams  [1 2 3 4 5 6 7 8 9]) =>
;;;  [[[1] 2] [[2] 3] [[3] 4] [[4] 5] [[5] 6] [[6] 7] [[7] 8] [[8] 9]]

(defn make-bigrams
  [list-of-values]
  (make-ngrams list-of-values 2))

;;; (make-pdf [:A :B :C :D :C :B :A :C :E :G :E :D :C :B]) =>
;;; {:A 0.14285714285714285, :B 0.21428571428571427, :C 0.2857142857142857,
;;;  :D 0.14285714285714285, :E 0.14285714285714285, :G 0.07142857142857142}

(defn make-pdf
  [list-of-things]
  (let [numthings (count list-of-things)]
    (into {}
          (map (fn [athing]
                 {athing (/ (count (filter #(= % athing) list-of-things))(float numthings))})
               list-of-things))))

;; (make-ngram-model [[[:foo] :bar] [[:foo] :baz] [[:foo] :quux]
;;                    [[:foo2] :bar] [[:foo2] :baz] [[:foo2] :quux]]) =>
;; {[:foo] {:bar 0.3333333333333333, :baz 0.3333333333333333, :quux 0.3333333333333333},
;;  [:foo2] {:bar 0.3333333333333333, :baz 0.3333333333333333, :quux 0.3333333333333333}}

(defn make-ngram-model
  [list-of-pairs]
  (ras/merge-data-by-head
   list-of-pairs
   (fn [matches] (make-pdf (map second matches)))))

;; (def test1 [{:experiment-mission "foo" :room-visit-order ["a" "b" "c"]}
;;             {:experiment-mission "foo" :room-visit-order ["a" "b" "f"]}
;;             {:experiment-mission "foo2" :room-visit-order ["a" "b" "d"]}
;;             {:experiment-mission "foo2" :room-visit-order ["a" "b" "g"]}])
;; (build-bigram-models-for-room-visits test1) =>
;;    {"foo" {["a"] {"b" 1.0}, ["b"] {"c" 0.5, "f" 0.5}},
;;     "foo2" {["a"] {"b" 1.0}, ["b"] {"d" 0.5, "g" 0.5}}}
;; (build-trigram-models-for-room-visits test1) =>
;;    {"foo" {["a" "b"] {"c" 0.5, "f" 0.5}}, "foo2" {["a" "b"] {"d" 0.5, "g" 0.5}}}

;;  (into {} (map (fn [role vector-of-stat-records]
;; (let [metadata (:meta-data vector-of-stat-records)
;;         subjects (:subjects  vector-of-stat-records)
;;         room-sttistics (:room-statistics vector-of-stat-records)]
;;               vector-of-stat-records-by-role)))

(defn build-ngram-models-for-room-visits
  [vector-of-stat-records worker]
  (let [visit-by-mission-by-role
        (into [] (apply concat
                        (map (fn [d]
                               (let [metadata (:meta-data d)
                                     mission (:experiment-mission metadata)
                                     statistics (:room-statistics d)
                                     res (into {} (map (fn  [[role role-stats]]
                                                         {role [mission (worker (:room-visit-order role-stats))]})
                                                       statistics))]
                                 res))
                             vector-of-stat-records)))
        ;;_ (when (dplev :all) (println "visit-by-mission-by-role=" visit-by-mission-by-role))
        rmpairs (ras/merge-data-by-head
                 (map (fn [[role [mission visit-by-mission]]]
                        ;; (when (dplev :all) (println "role=" role "visit-by-mission=" visit-by-mission))
                        [[role mission] visit-by-mission])
                      visit-by-mission-by-role)
                 identity)

        ;; _ (when (dplev :all) (println "rmpairs=" rmpairs))
        mergedpairs (map (fn [[head data]] [head (apply concat (map second data))]) rmpairs)

        ;;_ (when (dplev :all) (println "mergedpairs=" mergedpairs))
        learned (into {}
                      (map (fn [[rolemission ngram-datasets]]
                             {rolemission (make-ngram-model ngram-datasets)})
                           mergedpairs))
        ;; _ (when (dplev :all) (println "learned=" learned))
        ]
    ;; (when (dplev :all) (println "Learned by role ngram models:" learned))
    learned))

;;; (pprint (build-models-for-room-visits  example-datasets))

(defn build-bigram-models-for-room-visits
  [vector-of-stat-records]
  (build-ngram-models-for-room-visits vector-of-stat-records make-bigrams))

(defn build-trigram-models-for-room-visits
  [vector-of-stat-records]
  (build-ngram-models-for-room-visits vector-of-stat-records make-trigrams))

(defn build-models-for-room-visits
  [vector-of-stat-records]
  [(build-bigram-models-for-room-visits vector-of-stat-records)
   (build-trigram-models-for-room-visits vector-of-stat-records)])

(defn best-of
  [match]
  (apply max-key val match))

;;; (def models (build-models-for-room-visits test1))
;;; models => [{"foo" {["a"] {"b" 1.0}, ["b"] {"c" 0.5, "f" 0.5}},
;;;             "foo2" {["a"] {"b" 1.0}, ["b"] {"d" 0.5, "g" 0.5}}}
;;;            {"foo" {["a" "b"] {"c" 0.5, "f" 0.5}},
;;;             "foo2" {["a" "b"] {"d" 0.5, "g" 0.5}}}]
;;; (predict-next "foo" ["a" "b"] models) => ["f" 0.5]
;;; (predict-next "foo2" ["a" "b"] models) => ["g" 0.5]

;;; Given a model pair generated by 'build-models-for-room-visits' a vector containing the previous
;;; two rooms visited, yields a prediction for the next room and the propability.

(defn predict-next
  [role mission prevtwo models]
  (let [[bigram trigram] models
        trimatch ((get trigram [role mission]) prevtwo)
        bimatch ((get bigram [role mission]) [(last prevtwo)])]
    (cond trimatch (best-of trimatch)
          bimatch (best-of bimatch)
          :otherwise nil)))

;;; best-of returns the best bet with the probability, up to us if we want to follow it.

;;; We can convert the probabilities to description lengths and make an APSP matrix
;;; The planner can then find a minimul DL solution.  This would be our learned model.

(def test2 [{:experiment-mission "foo" :time-in-room  [["a" 5000] ["b" 6000] ["c" 7000]]
             :room-visit-order ["a" "b" "c"] :room-room-times [["A" "B" 1500 4.0 2.6]["B" "C" 7000 11.5 1.6]]}
            {:experiment-mission "foo" :time-in-room  [["a" 4000] ["b" 5000] ["f" 6000]]
             :room-visit-order ["a" "b" "f"] :room-room-times [["C" "D" 1500 11.0 7.3]["D" "E" 3000 14.2 4.7]]}
            {:experiment-mission "foo2" :time-in-room [["a" 3000] ["b" 4000] ["d" 5000]]
             :room-visit-order ["a" "b" "d"] :room-room-times [["A" "B" 1500 4.0 2.6]["D" "E" 3000 14.2 4.7]]}
            {:experiment-mission "foo2" :time-in-room [["a" 2000] ["b" 3000] ["g" 4000]]
             :room-visit-order ["a" "b" "g"] :room-room-times [["B" "C" 7000 11.5 1.6]["C" "D" 1500 11.0 7.3]]}])


(defn moments-seconds
  [data]
  [(/ (ras/mean data) 1000.0) (/ (ras/standard-deviation data) 1000.0)])

(defn moments
  [data]
  [(ras/mean data) (ras/standard-deviation data)])

;;; (overall-time-in-room test2) =>
;;; {"foo" [5.5 1.0488088481701516], "foo2" [3.5 1.0488088481701516]}
(defn overall-time-in-room
  [datasets]
  (let [data-by-role-mission (into [] (apply concat (map (fn [d]
                                                           (let [metadata (:meta-data d)
                                                                 mission (:experiment-mission metadata)
                                                                 statistics (:room-statistics d)
                                                                 res (into [] (map (fn [[role role-stats]]
                                                                                     [[role mission]
                                                                                      (:time-in-room role-stats)])
                                                                                   statistics))]
                                                             res))
                                                         datasets)))
        ;; _ (when (dplev :all) (println "data-by-role-mission=" data-by-role-mission))
        learned (into {} (map (fn [[rolemission data]]
                                ;; (if (not (empty? data)) (when (dplev :all) (println "rolemission=" rolemission "data=" data)))
                                (let [extracted (ras/merge-data-by-head
                                                    data
                                                    (fn [matches]
                                                      ;; (when (dplev :all) (println "matches=" matches))
                                                      (map second  matches)))]
                                  ;; (when (dplev :all) (println "extracted=" extracted))
                                  {rolemission (moments-seconds
                                                (apply concat (map second extracted)))}))
                              data-by-role-mission))]
    ;; (when (dplev :all) (println "Learned overall tir by role/mission:" learned))
    learned))

;;; (pprint (overall-time-in-room example-datasets))


;;; (by-room-time-in-room test2) =>
;;; {"foo" {"a" [4.5 0.7071067811865476], "b" [5.5 0.7071067811865476], "c" [7.0 0.001], "f" [6.0 0.001]},
;;;  "foo2" {"a" [2.5 0.7071067811865476], "b" [3.5 0.7071067811865476], "d" [5.0 0.001], "g" [4.0 0.001]}}

(defn by-room-time-in-room
  [datasets]
  (let [data-by-role-mission (into [] (apply concat (map (fn [d]
                                                           (let [metadata (:meta-data d)
                                                                 mission (:experiment-mission metadata)
                                                                 statistics (:room-statistics d)
                                                                 res (into [] (map (fn [[role role-stats]]
                                                                                     [[role mission]
                                                                                      (:time-in-room role-stats)])
                                                                                   statistics))]
                                                             res))
                                                         datasets)))
        learned (into {} (map (fn [[rolemission data]]
                                {rolemission (map second (ras/merge-data-by-head
                                                          data
                                                          (fn [matches]
                                                            ;; (when (dplev :all) (println "matches=" matches))
                                                            {(first (first matches)) (moments-seconds
                                                                                      (map second  matches))})))})
                              data-by-role-mission))]
    ;; (when (dplev :all) (println "Learned tir by role/mission:" learned))
    learned))


;;; (pprint (by-room-time-in-room example-datasets))

;;; (overall-speed-between-rooms test2) =>
;;; {"foo" [4.05 2.522564832335015], "foo2" [4.05 2.522564832335015]}

(defn overall-speed-between-rooms
  [datasets]
  (let [data-by-role-mission (into [] (apply concat (map (fn [d]
                                                           (let [metadata (:meta-data d)
                                                                 mission (:experiment-mission metadata)
                                                                 statistics (:room-statistics d)
                                                                 res (into [] (map (fn [[role role-stats]]
                                                                                     [[role mission]
                                                                                      (:room-room-times role-stats)])
                                                                                   statistics))]
                                                             res))
                                                         datasets)))
        ;;_ (when (dplev :all) (println "data-by-role-mission=" data-by-role-mission))
        learned (into {} (map (fn [[rolemission data]]
                                {rolemission (moments (map second (ras/merge-data-by-head
                                                                   data
                                                                   (fn [matches]
                                                                     ;;(when (dplev :all) (println "matches=" matches))
                                                                     (ras/mean (map (fn [x] (nth x 4)) matches))))))})
                              data-by-role-mission))]
    ;; (when (dplev :all) (println "Learned osbr:" learned))
    learned))

;;; (room-to-room-speed test2) =>
;;; {"foo" [5.5 1.0488088481701516], "foo2" [3.5 1.0488088481701516]}

;;; (pprint (overall-speed-between-rooms example-datasets))

(defn room-to-room-speed
  [datasets]
  (let [data-by-role-mission (into [] (apply concat (map (fn [d]
                                                           (let [metadata (:meta-data d)
                                                                 mission (:experiment-mission metadata)
                                                                 statistics (:room-statistics d)
                                                                 res (into [] (map (fn [[role role-stats]]
                                                                                     [[role mission]
                                                                                      (map
                                                                                       (fn [[pr tr dist time speed]]
                                                                                         [[pr tr] speed])
                                                                                       (:room-room-times role-stats))])
                                                                                   statistics))]
                                                             res))
                                                         datasets)))
        ;; _ (when (dplev :all) (println "data-by-role-mission=" data-by-role-mission))
        learned (into {} (map (fn [[rolemission data]]
                                {rolemission (ras/merge-data-by-head
                                              data
                                              (fn [matches]
                                                ;;(when (dplev :all) (println "matches=" matches))
                                                (moments
                                                 (map second matches)))
                                              #_(fn [matches]
                                                (when (dplev :all) (println "matches=" matches))
                                                (ras/merge-data-by-head
                                                 (apply concat (map second matches))
                                                 (fn [matches] (moments (map second matches))))))})
                              data-by-role-mission))]
    ;; (when (dplev :all) (println "Learned r2rs:" learned))
    learned))

;;; (pprint (room-to-room-speed example-datasets))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Score related data:

;;; During the run we want to be able to measure individulat performance against norms.  These contribute
;;; to the participant strength.
;;; Overall we want to factor out individules and look at combined metrics.
;;; overall score [mean sd]
;;; role-time map for each participant how much time was spent in each role
;;; ** Average/SD time per role in general, average time per role given a strategy

;;; role-efficiency for each participant based on role {search munber-of-people-moved, hazard number-of-rubble destroyed, medic number of people-points gained}
;;; the role efficiency numbers can be divided by time spent to get a per participant efficiency for each role
;;; the efficiency values can be averaged over all of the training set participants to get a mean/sd efficiency average/strategy, and overall.
;;; The individual scores can be established using deviation of an individual from the mean divided by sd.
;;; The individual instantaneous measures can be compared against the learned averages to incrementally
;;; adjust the performance of the player.

;;; These give us overall values for the end of game statistics.
;;; ** Average/SD final score in general and given a strategy.
;;; ** Average/SD rubble destroyed in general and given a strategy.
;;; ** Average/SD people moved in general and given a strategy.
;;; These are ofr examining perfromance expectations over the course of the run.
;;; ** Average/SD score at time intervals (say every 30 seconds = 30 samples, in general and per strategy
;;; ** Average/SD people moved at time intervals, as above, in general and per strategy
;;; ** Average/SD rubble destroyed at time intervals, as above, in general and per strategy
;;; The former 3 are just the last value of the final 3.  If we compute the final 3 we can lookup the former 3

  ;; (let {data (map (fn [dataset]
  ;;                   [(:experiment-mission dataset)
  ;;                    (map (fn [t]

;;; (generate-model-from-datasets test2)
;;; {:room-visit-order [{"foo" {["a"] {"b" 1.0}, ["b"] {"c" 0.5, "f" 0.5}},
;;;                      "foo2" {["a"] {"b" 1.0}, ["b"] {"d" 0.5, "g" 0.5}}}
;;;                     {"foo" {["a" "b"] {"c" 0.5, "f" 0.5}},
;;;                      "foo2" {["a" "b"] {"d" 0.5, "g" 0.5}}}],
;;;  :overall-time-in-room {"foo" [5.5 1.0488088481701516], "foo2" [3.5 1.0488088481701516]},
;;;  :time-in-room-by-room {"foo" {"a" [4.5 0.7071067811865476],
;;;                                "b" [5.5 0.7071067811865476],
;;;                                "c" [7.0 0.001],
;;;                                "f" [6.0 0.001]},
;;;                         "foo2" {"a" [2.5 0.7071067811865476],
;;;                                 "b" [3.5 0.7071067811865476],
;;;                                 "d" [5.0 0.001], "g" [4.0 0.001]}},
;;;  :overall-speed-between-rooms {"foo" [4.05 2.522564832335015], "foo2" [4.05 2.522564832335015]},
;;;  :room-to-room-speeds {"foo" {["A" "B"] [2.6 1], ["B" "C"] [1.6 1], ["C" "D"] [7.3 1], ["D" "E"] [4.7 1]},
;;;                        "foo2" {["A" "B"] [2.6 1], ["D" "E"] [4.7 1], ["B" "C"] [1.6 1], ["C" "D"] [7.3 1]}}}


;;;(def learned-model-format "rita-learned-object-1.0")

(def learned-model-format "rita-learned-object-2.0")

(defn twoone
  [two one]
  (case two
    "Search_Specialist" (if (= one "Medical_Specialist")             :ssm :ssh)
    "Transport_Specialist" (if (= one "Medical_Specialist")          :ssm :ssh)
    "Medical_Specialist" (if (= one "Search_Specialist")             :mms :mmh)
    "Engineering_Specialist" (if (= one "Medical_Specialist")        :hhm :hhs)
    "Hazardous_Material_Specialist" (if (= one "Medical_Specialist") :hhm :hhs)
    (do
      ;; (when (dplev :all) (println "one=" one "two=" two ":bad"))
      :hsm)))

(defn role-strategy
  [& roles]
  (let [[r1 r2 r3] (take-last 3 roles)]
    (when (dplev :all) (println "Initial roles=" roles "Using roles=" r1 r2 r3))
    (when (not (== (count roles) 3))
      (when (dplev :all) (println "****** DATA PROBLEM: roles=" roles "(last three used) ******")))
    (cond
      ;; Three different roles
      (and (not (= r1 r2)) (not (= r1 r3)) (not (= r2 r3)))
      :hsm

      ;; Three identical roles
      (and (= r1 r2) (= r1 r3))
      (case r1
        "Search_Specialist" :sss
        "Transport_Specialist" :sss
        "Medical_Specialist" :mmm
        "Engineering_Specialist" :hhh
        "Hazardous_Material_Specialist" :hhh
        (do (when (dplev :all) (println "r1=" r1 "r2=" r2 "r3=" r3 ":bad")) :hsm))

      ;; Two of one + 1 other
      :otherwise
      (cond (= r1 r2) (twoone r1 r3)
            (= r1 r3) (twoone r1 r2)
            (= r2 r3) (twoone r2 r1)
            :otherwise (do (when (dplev :all) (println "r1=" r1 "r2=" r2 "r3=" r3 ":bad")) :hsm)))))

;; (role-strategy "Search_Specialist" "Search_Specialist" "Search_Specialist")
;; (role-strategy "Medical_Specialist" "Medical_Specialist" "Medical_Specialist")
;; (role-strategy "Hazardous_Material_Specialist" "Hazardous_Material_Specialist" "Hazardous_Material_Specialist")
;; (role-strategy "Search_Specialist" "Medical_Specialist"  "Search_Specialist")
;; (role-strategy "Search_Specialist" "Search_Specialist"  "Medical_Specialist")
;; (role-strategy "Medical_Specialist" "Search_Specialist" "Search_Specialist")
;; (role-strategy "Search_Specialist" "Hazardous_Material_Specialist" "Search_Specialist")
;; (role-strategy "Search_Specialist" "Search_Specialist" "Hazardous_Material_Specialist")
;; (role-strategy "Hazardous_Material_Specialist" "Search_Specialist" "Search_Specialist")
;; (role-strategy "Search_Specialist" "Medical_Specialist" "Medical_Specialist")
;; (role-strategy "Medical_Specialist" "Search_Specialist" "Medical_Specialist")
;; (role-strategy "Medical_Specialist" "Medical_Specialist" "Search_Specialist")
;; (role-strategy "Medical_Specialist" "Hazardous_Material_Specialist" "Medical_Specialist")
;; (role-strategy "Medical_Specialist" "Medical_Specialist" "Hazardous_Material_Specialist")
;; (role-strategy "Hazardous_Material_Specialist" "Medical_Specialist" "Medical_Specialist")
;; (role-strategy "Search_Specialist" "Hazardous_Material_Specialist" "Hazardous_Material_Specialist")
;; (role-strategy "Hazardous_Material_Specialist" "Search_Specialist" "Hazardous_Material_Specialist")
;; (role-strategy "Hazardous_Material_Specialist" "Hazardous_Material_Specialist" "Search_Specialist")
;; (role-strategy "Hazardous_Material_Specialist" "Hazardous_Material_Specialist" "Medical_Specialist")
;; (role-strategy "Medical_Specialist" "Hazardous_Material_Specialist" "Hazardous_Material_Specialist")
;; (role-strategy "Hazardous_Material_Specialist" "Medical_Specialist" "Hazardous_Material_Specialist")

(def example-datasets [{:format-version "rita-sl-data-2.0",
                        :meta-data {:experiment-mission "Saturn_B",
                                    :trial-id "9d5e964a-75ea-4962-abe3-4cff2ef3a21d",
                                    :name "TM000111_T000421",
                                    :initial-role-strategy :allroles,
                                    :experiment-author "AT/AC",
                                    :study-number "2",
                                    :group-number "2",
                                    :map-name "Saturn_1.6_3D",
                                    :experimenter "AT/AC",
                                    :testbed-version "2.0.0-dev.524-dac6ca2",
                                    :experiment-id "902ab6f1-90c0-4cf2-ab60-6a9f7fa365b7",
                                    :trial-number "T000421",
                                    :condition "2",
                                    :notes ["none"]},
                        :subjects '("E000331" "E000332" "E000333"),
                        :room-statistics {"Medical_Specialist"
                                          {:room-visit-order ["/Saturn.SunDollarCoffee" "/Saturn.Room108" "/Saturn.ComputerFarm"],
                                           :room-enter-order [],
                                           :room-room-times [["/Saturn.SunDollarCoffee" "/Saturn.Room108" 1500 0 0.0]
                                                             ["/Saturn.Room108" "/Saturn.ComputerFarm" 1500 17.661903381347656 11.774602254231771]],
                                           :time-in-room []},
                                          "Hazardous_Material_Specialist"
                                          {:room-visit-order ["/Saturn.Kitchen" "/Saturn.Room103" "/Saturn.Room102" "/Saturn.Room101" "/Saturn.HerbalifeConfRoom" "/Saturn.KingChrissOffice" "/Saturn.KingsTerrace" "/Saturn.Library" "/Saturn.AmwayConfRoom" "/Saturn.MaryKayConfRoom"],
                                           :room-enter-order [],
                                           :room-room-times [["/Saturn.Kitchen" "/Saturn.Room103" 1500 0 0.0]
                                                             ["/Saturn.Room103" "/Saturn.Room102" 1500 12.0 8.0]
                                                             ["/Saturn.Room102" "/Saturn.Room101" 1500 0 0.0]
                                                             ["/Saturn.Room101" "/Saturn.HerbalifeConfRoom" 1500 16.816654205322266 11.211102803548178]
                                                             ["/Saturn.HerbalifeConfRoom" "/Saturn.KingChrissOffice" 1500 0 0.0]
                                                             ["/Saturn.KingChrissOffice" "/Saturn.KingsTerrace" 1500 4.0 2.6666666666666665]
                                                             ["/Saturn.KingsTerrace" "/Saturn.Library" 1500 39.0 26.0]
                                                             ["/Saturn.Library" "/Saturn.AmwayConfRoom" 1500 0 0.0]
                                                             ["/Saturn.AmwayConfRoom" "/Saturn.MaryKayConfRoom" 1500 9.0 6.0]],
                                           :time-in-room []},
                                          "Search_Specialist"
                                          {:room-visit-order ["/Saturn.Lobby" "/Saturn.O101" "/Saturn.O100" "/Saturn.BreakRoom" "/Saturn.DataStorage" "/Saturn.StorageASE" "/Saturn.StorageBSE" "/Saturn.StorageDSW" "/Saturn.StorageBNE" "/Saturn.NStorageA" "/Saturn.StorageBNW" "/Saturn.Room104" "/Saturn.Room105" "/Saturn.Room110"],
                                           :room-enter-order [],
                                           :room-room-times [["/Saturn.Lobby" "/Saturn.O101" 1500 0 0.0]
                                                             ["/Saturn.O101" "/Saturn.O100" 1500 11.0 7.333333333333333]
                                                             ["/Saturn.O100" "/Saturn.BreakRoom" 1500 8.236067771911621 5.490711847941081]
                                                             ["/Saturn.BreakRoom" "/Saturn.DataStorage" 1500 15.0 10.0]
                                                             ["/Saturn.DataStorage" "/Saturn.StorageASE" 1500 0 0.0]
                                                             ["/Saturn.StorageASE" "/Saturn.StorageBSE" 1500 28.0 18.666666666666668]
                                                             ["/Saturn.StorageBSE" "/Saturn.StorageDSW" 1500 13.0 8.666666666666666]
                                                             ["/Saturn.StorageDSW" "/Saturn.StorageBNE" 1500 19.03840446472168 12.692269643147787]
                                                             ["/Saturn.StorageBNE" "/Saturn.NStorageA" 1500 4.0 2.6666666666666665]
                                                             ["/Saturn.NStorageA" "/Saturn.StorageBNW" 1500 4.0 2.6666666666666665]
                                                             ["/Saturn.StorageBNW" "/Saturn.Room104" 1500 0 0.0]
                                                             ["/Saturn.Room104" "/Saturn.Room105" 1500 13.0 8.666666666666666]
                                                             ["/Saturn.Room105" "/Saturn.Room110" 1500 40.0 26.666666666666668]
                                                             ],
                                           :time-in-room []},
                                          "None" {:room-visit-order [],
                                                  :room-enter-order [],
                                                  :room-room-times [],
                                                  :time-in-room []}},
                        :role-time {"E000331" {"Search_Specialist" [[2.039 704.693] [706.732 117.601]],
                                               "Medical_Specialist" [[824.333 75.667]],
                                               "Hazardous_Material_Specialist" []},
                                    "E000332" {"Search_Specialist" [],
                                               "Medical_Specialist" [[-0.001 316.483] [316.482 583.518]],
                                               "Hazardous_Material_Specialist" []},
                                    "E000333" {"Search_Specialist" [],
                                               "Medical_Specialist" [],
                                               "Hazardous_Material_Specialist" [[4.337 247.245] [251.582 297.349] [548.931 351.069]]}},
                        :search-role {"E000331" {:elapsed-time-array [0 36.004 48.882 91.231 136.379 149.079 157.18 177.53 241.282 250.929 271.88 290.68 449.63 524.98 762.63 772.682 789.329], :victims-moved [0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16]},
                                      "E000332" {:elapsed-time-array [0],
                                                 :victims-moved [0]},
                                      "E000333" {:elapsed-time-array [0],
                                                 :victims-moved [0]}},
                        :hazard-role {"E000331" {:elapsed-time-array [0],
                                                 :rv [[0 0]]},
                                      "E000332" {:elapsed-time-array [0],
                                                 :rv [[0 0]]},
                                      "E000333" {:elapsed-time-array [0 35.021 36.579 37.779 52.13 68.081 69.481 70.68 73.23 74.678 76.032 142.228 143.428 144.628 145.829 147.029 148.229 155.379 156.78 157.979 163.63 164.829 181.829 184.478 188.629 189.828 191.43 194.229 195.429 196.628 197.828 199.131 200.429 201.628 205.329 206.528 207.729 209.329 217.778 218.98 220.58 280.179 282.729 286.029 287.229 289.528 295.879 300.829 302.228 312.13 313.429 314.629 316.679 323.08 324.279 369.929 375.729 376.928 378.128 391.23 392.429 393.628 406.58 407.928 411.428 412.629 413.829 437.13 438.329 457.979 459.378 460.579 485.879 487.078 498.03 499.23 502.329 503.529 504.732 512.179 513.379 589.929 591.129 592.528 593.728 612.929 614.128 648.779 649.979 662.23 663.578 664.779 685.629 686.829 690.278 702.729 704.179 708.329 710.379 711.578 722.829 724.428 761.029 762.68 763.879 796.679 798.078 810.779 811.978 813.23 829.829 831.279 832.778 888.035 889.679 891.129],
                                                 :rv [[0 0] [1 0] [2 0] [3 0] [4 0] [5 0] [6 0] [7 0] [8 0] [9 0] [10 0] [11 0] [12 0] [13 0] [14 0] [15 0] [16 0] [17 0] [18 0] [19 0] [20 0] [21 0] [22 0] [23 0] [24 0] [25 0] [26 0] [27 0] [28 0] [29 0] [30 0] [31 0] [32 0] [33 0] [34 0] [35 0] [36 0] [37 0] [38 0] [39 0] [40 0] [41 0] [42 0] [43 0] [44 0] [45 0] [46 0] [47 0] [48 0] [49 0] [50 0] [51 0] [52 0] [53 0] [54 0] [55 0] [56 0] [57 0] [58 0] [59 0] [60 0] [61 0] [62 0] [63 0] [64 0] [65 0] [66 0] [67 0] [68 0] [69 0] [70 0] [71 0] [72 0] [73 0] [74 0] [75 0] [76 0] [77 0] [78 0] [79 0] [80 0] [81 0] [82 0] [83 0] [84 0] [85 0] [86 0] [87 0] [88 0] [89 0] [90 0] [91 0] [92 0] [93 0] [94 0] [95 0] [96 0] [97 0] [98 0] [99 0] [100 0] [101 0] [102 0] [103 0] [104 0] [105 0] [106 0] [107 0] [108 0] [109 0] [110 0] [111 0] [112 0] [113 0] [114 0] [115 0]]}},
                        :medic-role {"E000331" {:elapsed-time-array [0 885.732],
                                                :victim-saved-points [0 10.0]},
                                     "E000332" {:elapsed-time-array [0 60.753 101.479 109.886 145.682 160.131 168.13 182.629 199.13 215.93 223.93 232.029 240.131 294.133 384.029 408.88 416.984 425.031 433.13 445.88 483.479 499.13 526.581 551.83 582.132 659.182 682.881 714.331 765.08 775.83 783.779 879.23 888.03],
                                                :victim-saved-points [0 10.0 20.0 30.0 80.0 90.0 100.0 110.0 120.0 130.0 140.0 150.0 160.0 170.0 220.0 230.0 240.0 250.0 260.0 270.0 320.0 330.0 340.0 350.0 360.0 370.0 380.0 390.0 440.0 450.0 460.0 470.0 480.0]},
                                     "E000333" {:elapsed-time-array [0],
                                                :victim-saved-points [0]}}}
                       {:format-version "rita-sl-data-2.0",
                        :meta-data {:experiment-mission "Saturn_A",
                                    :trial-id "9d5e964a-75ea-4962-abe3-4cff2ef3a21d",
                                    :name "TM000111_T000421",
                                    :initial-role-strategy :allroles,
                                    :experiment-author "AT/AC",
                                    :study-number "2",
                                    :group-number "2",
                                    :map-name "Saturn_1.6_3D",
                                    :experimenter "AT/AC",
                                    :testbed-version "2.0.0-dev.524-dac6ca2",
                                    :experiment-id "902ab6f1-90c0-4cf2-ab60-6a9f7fa365b7",
                                    :trial-number "T000421",
                                    :condition "2",
                                    :notes ["none"]},
                        :subjects '("E000331" "E000332" "E000333"),
                        :room-statistics {"Medical_Specialist"
                                          {:room-visit-order ["/Saturn.SunDollarCoffee" "/Saturn.Room108" "/Saturn.ComputerFarm"],
                                           :room-enter-order [],
                                           :room-room-times [["/Saturn.SunDollarCoffee" "/Saturn.Room108" 1500 0 0.0]
                                                             ["/Saturn.Room108" "/Saturn.ComputerFarm" 1500 17.661903381347656 11.774602254231771]],
                                           :time-in-room []},
                                          "Hazardous_Material_Specialist"
                                          {:room-visit-order ["/Saturn.Kitchen" "/Saturn.Room103" "/Saturn.Room102" "/Saturn.Room101" "/Saturn.HerbalifeConfRoom" "/Saturn.KingChrissOffice" "/Saturn.KingsTerrace" "/Saturn.Library" "/Saturn.AmwayConfRoom" "/Saturn.MaryKayConfRoom"],
                                           :room-enter-order [],
                                           :room-room-times [["/Saturn.Kitchen" "/Saturn.Room103" 1500 0 0.0]
                                                             ["/Saturn.Room103" "/Saturn.Room102" 1500 12.0 8.0]
                                                             ["/Saturn.Room102" "/Saturn.Room101" 1500 0 0.0]
                                                             ["/Saturn.Room101" "/Saturn.HerbalifeConfRoom" 1500 16.816654205322266 11.211102803548178]
                                                             ["/Saturn.HerbalifeConfRoom" "/Saturn.KingChrissOffice" 1500 0 0.0]
                                                             ["/Saturn.KingChrissOffice" "/Saturn.KingsTerrace" 1500 4.0 2.6666666666666665]
                                                             ["/Saturn.KingsTerrace" "/Saturn.Library" 1500 39.0 26.0]
                                                             ["/Saturn.Library" "/Saturn.AmwayConfRoom" 1500 0 0.0]
                                                             ["/Saturn.AmwayConfRoom" "/Saturn.MaryKayConfRoom" 1500 9.0 6.0]],
                                           :time-in-room []},
                                          "Search_Specialist"
                                          {:room-visit-order ["/Saturn.Lobby" "/Saturn.O101" "/Saturn.O100" "/Saturn.BreakRoom" "/Saturn.DataStorage" "/Saturn.StorageASE" "/Saturn.StorageBSE" "/Saturn.StorageDSW" "/Saturn.StorageBNE" "/Saturn.NStorageA" "/Saturn.StorageBNW" "/Saturn.Room104" "/Saturn.Room105" "/Saturn.Room110"],
                                           :room-enter-order [],
                                           :room-room-times [["/Saturn.Lobby" "/Saturn.O101" 1500 0 0.0]
                                                             ["/Saturn.O101" "/Saturn.O100" 1500 11.0 7.333333333333333]
                                                             ["/Saturn.O100" "/Saturn.BreakRoom" 1500 8.236067771911621 5.490711847941081]
                                                             ["/Saturn.BreakRoom" "/Saturn.DataStorage" 1500 15.0 10.0]
                                                             ["/Saturn.DataStorage" "/Saturn.StorageASE" 1500 0 0.0]
                                                             ["/Saturn.StorageASE" "/Saturn.StorageBSE" 1500 28.0 18.666666666666668]
                                                             ["/Saturn.StorageBSE" "/Saturn.StorageDSW" 1500 13.0 8.666666666666666]
                                                             ["/Saturn.StorageDSW" "/Saturn.StorageBNE" 1500 19.03840446472168 12.692269643147787]
                                                             ["/Saturn.StorageBNE" "/Saturn.NStorageA" 1500 4.0 2.6666666666666665]
                                                             ["/Saturn.NStorageA" "/Saturn.StorageBNW" 1500 4.0 2.6666666666666665]
                                                             ["/Saturn.StorageBNW" "/Saturn.Room104" 1500 0 0.0]
                                                             ["/Saturn.Room104" "/Saturn.Room105" 1500 13.0 8.666666666666666]
                                                             ["/Saturn.Room105" "/Saturn.Room110" 1500 40.0 26.666666666666668]
                                                             ],
                                           :time-in-room []},
                                          "None" {:room-visit-order [],
                                                  :room-enter-order [],
                                                  :room-room-times [],
                                                  :time-in-room []}},
                        :role-time {"E000331" {"Search_Specialist" [[2.039 704.693] [706.732 117.601]],
                                               "Medical_Specialist" [[824.333 75.667]],
                                               "Hazardous_Material_Specialist" []},
                                    "E000332" {"Search_Specialist" [],
                                               "Medical_Specialist" [[-0.001 316.483] [316.482 583.518]],
                                               "Hazardous_Material_Specialist" []},
                                    "E000333" {"Search_Specialist" [],
                                               "Medical_Specialist" [],
                                               "Hazardous_Material_Specialist" [[4.337 247.245] [251.582 297.349] [548.931 351.069]]}},
                        :search-role {"E000331" {:elapsed-time-array [0 36.004 48.882 91.231 136.379 149.079 157.18 177.53 241.282 250.929 271.88 290.68 449.63 524.98 762.63 772.682 789.329], :victims-moved [0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16]},
                                      "E000332" {:elapsed-time-array [0],
                                                 :victims-moved [0]},
                                      "E000333" {:elapsed-time-array [0],
                                                 :victims-moved [0]}},
                        :hazard-role {"E000331" {:elapsed-time-array [0],
                                                 :rv [[0 0]]},
                                      "E000332" {:elapsed-time-array [0],
                                                 :rv [[0 0]]},
                                      "E000333" {:elapsed-time-array [0 35.021 36.579 37.779 52.13 68.081 69.481 70.68 73.23 74.678 76.032 142.228 143.428 144.628 145.829 147.029 148.229 155.379 156.78 157.979 163.63 164.829 181.829 184.478 188.629 189.828 191.43 194.229 195.429 196.628 197.828 199.131 200.429 201.628 205.329 206.528 207.729 209.329 217.778 218.98 220.58 280.179 282.729 286.029 287.229 289.528 295.879 300.829 302.228 312.13 313.429 314.629 316.679 323.08 324.279 369.929 375.729 376.928 378.128 391.23 392.429 393.628 406.58 407.928 411.428 412.629 413.829 437.13 438.329 457.979 459.378 460.579 485.879 487.078 498.03 499.23 502.329 503.529 504.732 512.179 513.379 589.929 591.129 592.528 593.728 612.929 614.128 648.779 649.979 662.23 663.578 664.779 685.629 686.829 690.278 702.729 704.179 708.329 710.379 711.578 722.829 724.428 761.029 762.68 763.879 796.679 798.078 810.779 811.978 813.23 829.829 831.279 832.778 888.035 889.679 891.129],
                                                 :rv [[0 0] [1 0] [2 0] [3 0] [4 0] [5 0] [6 0] [7 0] [8 0] [9 0] [10 0] [11 0] [12 0] [13 0] [14 0] [15 0] [16 0] [17 0] [18 0] [19 0] [20 0] [21 0] [22 0] [23 0] [24 0] [25 0] [26 0] [27 0] [28 0] [29 0] [30 0] [31 0] [32 0] [33 0] [34 0] [35 0] [36 0] [37 0] [38 0] [39 0] [40 0] [41 0] [42 0] [43 0] [44 0] [45 0] [46 0] [47 0] [48 0] [49 0] [50 0] [51 0] [52 0] [53 0] [54 0] [55 0] [56 0] [57 0] [58 0] [59 0] [60 0] [61 0] [62 0] [63 0] [64 0] [65 0] [66 0] [67 0] [68 0] [69 0] [70 0] [71 0] [72 0] [73 0] [74 0] [75 0] [76 0] [77 0] [78 0] [79 0] [80 0] [81 0] [82 0] [83 0] [84 0] [85 0] [86 0] [87 0] [88 0] [89 0] [90 0] [91 0] [92 0] [93 0] [94 0] [95 0] [96 0] [97 0] [98 0] [99 0] [100 0] [101 0] [102 0] [103 0] [104 0] [105 0] [106 0] [107 0] [108 0] [109 0] [110 0] [111 0] [112 0] [113 0] [114 0] [115 0]]}},
                        :medic-role {"E000331" {:elapsed-time-array [0 885.732],
                                                :victim-saved-points [0 10.0]},
                                     "E000332" {:elapsed-time-array [0 60.753 101.479 109.886 145.682 160.131 168.13 182.629 199.13 215.93 223.93 232.029 240.131 294.133 384.029 408.88 416.984 425.031 433.13 445.88 483.479 499.13 526.581 551.83 582.132 659.182 682.881 714.331 765.08 775.83 783.779 879.23 888.03],
                                                :victim-saved-points [0 10.0 20.0 30.0 80.0 90.0 100.0 110.0 120.0 130.0 140.0 160.0 180.0 270.0 290.0 330.0 340.0 350.0 360.0 370.0 380.0 390.0 400.0 450.0 360.0 370.0 380.0 390.0 440.0 450.0 460.0 470.0 480.0]},
                                     "E000333" {:elapsed-time-array [0],
                                                :victim-saved-points [0]}}}
                       {:format-version "rita-sl-data-2.0",
                        :meta-data {:experiment-mission "Saturn_A",
                                    :trial-id "9d5e964a-75ea-4962-abe3-4cff2ef3a21d",
                                    :name "TM000111_T000421",
                                    :initial-role-strategy :allroles,
                                    :experiment-author "AT/AC",
                                    :study-number "2",
                                    :group-number "2",
                                    :map-name "Saturn_1.6_3D",
                                    :experimenter "AT/AC",
                                    :testbed-version "2.0.0-dev.524-dac6ca2",
                                    :experiment-id "902ab6f1-90c0-4cf2-ab60-6a9f7fa365b7",
                                    :trial-number "T000421",
                                    :condition "2",
                                    :notes ["none"]},
                        :subjects '("E000331" "E000332" "E000333"),
                        :room-statistics {"Medical_Specialist"
                                          {:room-visit-order ["/Saturn.SunDollarCoffee" "/Saturn.Room108" "/Saturn.ComputerFarm"],
                                           :room-enter-order [],
                                           :room-room-times [["/Saturn.SunDollarCoffee" "/Saturn.Room108" 1500 0 0.0]
                                                             ["/Saturn.Room108" "/Saturn.ComputerFarm" 1500 17.661903381347656 11.774602254231771]],
                                           :time-in-room []},
                                          "Hazardous_Material_Specialist"
                                          {:room-visit-order ["/Saturn.Kitchen" "/Saturn.Room103" "/Saturn.Room102" "/Saturn.Room101" "/Saturn.HerbalifeConfRoom" "/Saturn.KingChrissOffice" "/Saturn.KingsTerrace" "/Saturn.Library" "/Saturn.AmwayConfRoom" "/Saturn.MaryKayConfRoom"],
                                           :room-enter-order [],
                                           :room-room-times [["/Saturn.Kitchen" "/Saturn.Room103" 1500 0 0.0]
                                                             ["/Saturn.Room103" "/Saturn.Room102" 1500 12.0 8.0]
                                                             ["/Saturn.Room102" "/Saturn.Room101" 1500 0 0.0]
                                                             ["/Saturn.Room101" "/Saturn.HerbalifeConfRoom" 1500 16.816654205322266 11.211102803548178]
                                                             ["/Saturn.HerbalifeConfRoom" "/Saturn.KingChrissOffice" 1500 0 0.0]
                                                             ["/Saturn.KingChrissOffice" "/Saturn.KingsTerrace" 1500 4.0 2.6666666666666665]
                                                             ["/Saturn.KingsTerrace" "/Saturn.Library" 1500 39.0 26.0]
                                                             ["/Saturn.Library" "/Saturn.AmwayConfRoom" 1500 0 0.0]
                                                             ["/Saturn.AmwayConfRoom" "/Saturn.MaryKayConfRoom" 1500 9.0 6.0]],
                                           :time-in-room []},
                                          "Search_Specialist"
                                          {:room-visit-order ["/Saturn.Lobby" "/Saturn.O101" "/Saturn.O100" "/Saturn.BreakRoom" "/Saturn.DataStorage" "/Saturn.StorageASE" "/Saturn.StorageBSE" "/Saturn.StorageDSW" "/Saturn.StorageBNE" "/Saturn.NStorageA" "/Saturn.StorageBNW" "/Saturn.Room104" "/Saturn.Room105" "/Saturn.Room110"],
                                           :room-enter-order [],
                                           :room-room-times [["/Saturn.Lobby" "/Saturn.O101" 1500 0 0.0]
                                                             ["/Saturn.O101" "/Saturn.O100" 1500 11.0 7.333333333333333]
                                                             ["/Saturn.O100" "/Saturn.BreakRoom" 1500 8.236067771911621 5.490711847941081]
                                                             ["/Saturn.BreakRoom" "/Saturn.DataStorage" 1500 15.0 10.0]
                                                             ["/Saturn.DataStorage" "/Saturn.StorageASE" 1500 0 0.0]
                                                             ["/Saturn.StorageASE" "/Saturn.StorageBSE" 1500 28.0 18.666666666666668]
                                                             ["/Saturn.StorageBSE" "/Saturn.StorageDSW" 1500 13.0 8.666666666666666]
                                                             ["/Saturn.StorageDSW" "/Saturn.StorageBNE" 1500 19.03840446472168 12.692269643147787]
                                                             ["/Saturn.StorageBNE" "/Saturn.NStorageA" 1500 4.0 2.6666666666666665]
                                                             ["/Saturn.NStorageA" "/Saturn.StorageBNW" 1500 4.0 2.6666666666666665]
                                                             ["/Saturn.StorageBNW" "/Saturn.Room104" 1500 0 0.0]
                                                             ["/Saturn.Room104" "/Saturn.Room105" 1500 13.0 8.666666666666666]
                                                             ["/Saturn.Room105" "/Saturn.Room110" 1500 40.0 26.666666666666668]
                                                             ],
                                           :time-in-room []},
                                          "None" {:room-visit-order [],
                                                  :room-enter-order [],
                                                  :room-room-times [],
                                                  :time-in-room []}},
                        :role-time {"E000331" {"Search_Specialist" [[2.039 704.693] [706.732 117.601]],
                                               "Medical_Specialist" [[824.333 75.667]],
                                               "Hazardous_Material_Specialist" []},
                                    "E000332" {"Search_Specialist" [],
                                               "Medical_Specialist" [[-0.001 316.483] [316.482 583.518]],
                                               "Hazardous_Material_Specialist" []},
                                    "E000333" {"Search_Specialist" [],
                                               "Medical_Specialist" [],
                                               "Hazardous_Material_Specialist" [[4.337 247.245] [251.582 297.349] [548.931 351.069]]}},
                        :search-role {"E000331" {:elapsed-time-array [0 36.004 48.882 91.231 136.379 149.079 157.18 177.53 241.282 250.929 271.88 290.68 449.63 524.98 762.63 772.682 789.329], :victims-moved [0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16]},
                                      "E000332" {:elapsed-time-array [0],
                                                 :victims-moved [0]},
                                      "E000333" {:elapsed-time-array [0],
                                                 :victims-moved [0]}},
                        :hazard-role {"E000331" {:elapsed-time-array [0],
                                                 :rv [[0 0]]},
                                      "E000332" {:elapsed-time-array [0],
                                                 :rv [[0 0]]},
                                      "E000333" {:elapsed-time-array [0 35.021 36.579 37.779 52.13 68.081 69.481 70.68 73.23 74.678 76.032 142.228 143.428 144.628 145.829 147.029 148.229 155.379 156.78 157.979 163.63 164.829 181.829 184.478 188.629 189.828 191.43 194.229 195.429 196.628 197.828 199.131 200.429 201.628 205.329 206.528 207.729 209.329 217.778 218.98 220.58 280.179 282.729 286.029 287.229 289.528 295.879 300.829 302.228 312.13 313.429 314.629 316.679 323.08 324.279 369.929 375.729 376.928 378.128 391.23 392.429 393.628 406.58 407.928 411.428 412.629 413.829 437.13 438.329 457.979 459.378 460.579 485.879 487.078 498.03 499.23 502.329 503.529 504.732 512.179 513.379 589.929 591.129 592.528 593.728 612.929 614.128 648.779 649.979 662.23 663.578 664.779 685.629 686.829 690.278 702.729 704.179 708.329 710.379 711.578 722.829 724.428 761.029 762.68 763.879 796.679 798.078 810.779 811.978 813.23 829.829 831.279 832.778 888.035 889.679 891.129],
                                                 :rv [[0 0] [1 0] [2 0] [3 0] [4 0] [5 0] [6 0] [7 0] [8 0] [9 0] [10 0] [11 0] [12 0] [13 0] [14 0] [15 0] [16 0] [17 0] [18 0] [19 0] [20 0] [21 0] [22 0] [23 0] [24 0] [25 0] [26 0] [27 0] [28 0] [29 0] [30 0] [31 0] [32 0] [33 0] [34 0] [35 0] [36 0] [37 0] [38 0] [39 0] [40 0] [41 0] [42 0] [43 0] [44 0] [45 0] [46 0] [47 0] [48 0] [49 0] [50 0] [51 0] [52 0] [53 0] [54 0] [55 0] [56 0] [57 0] [58 0] [59 0] [60 0] [61 0] [62 0] [63 0] [64 0] [65 0] [66 0] [67 0] [68 0] [69 0] [70 0] [71 0] [72 0] [73 0] [74 0] [75 0] [76 0] [77 0] [78 0] [79 0] [80 0] [81 0] [82 0] [83 0] [84 0] [85 0] [86 0] [87 0] [88 0] [89 0] [90 0] [91 0] [92 0] [93 0] [94 0] [95 0] [96 0] [97 0] [98 0] [99 0] [100 0] [101 0] [102 0] [103 0] [104 0] [105 0] [106 0] [107 0] [108 0] [109 0] [110 0] [111 0] [112 0] [113 0] [114 0] [115 0]]}},
                        :medic-role {"E000331" {:elapsed-time-array [0 885.732],
                                                :victim-saved-points [0 50.0]},
                                     "E000332" {:elapsed-time-array [0 60.753 101.479 109.886 145.682 160.131 168.13 182.629 199.13 215.93 223.93 232.029 240.131 294.133 384.029 408.88 416.984 425.031 433.13 445.88 483.479 499.13 526.581 551.83 582.132 659.182 682.881 714.331 765.08 775.83 783.779 879.23 888.03],
                                                :victim-saved-points [0 10.0 20.0 30.0 80.0 90.0 100.0 110.0 120.0 130.0 140.0 150.0 160.0 170.0 170.0 170.0 180.0 190.0 200.0 210.0 220.0 230.0 240.0 250.0 260.0 270.0 280.0 290.0 340.0 350.0 360.0 370.0 380.0]},
                                     "E000333" {:elapsed-time-array [0],
                                                :victim-saved-points [0]}}}])

(defn make-vec
  [size initial-value]
  (loop [n size myvec []]
    (if (<= n 0) myvec (recur (- n 1) (conj myvec (atom initial-value))))))

;;; (make-vec 3 42)

(defn first-time-role
  [list-of-times]
  (if (empty? list-of-times) 901 (first (first list-of-times))))

(defn stretchto
  [avec dcount]
  (let [numscores (count avec)]
    (loop [stretchby (- dcount numscores)
           thisvec avec]
      (if (> stretchby 0)
        (recur (- stretchby 1) (conj thisvec (last thisvec)))
        thisvec))))

(declare moments)

(defn rstrategy-to-score-moments
  [datasets]
  ;; Sample rate is 10 seconds, 17 minutes = 1020 seconds = 102 buckets
  (let [mapofvecs (into {} (map (fn [x] {x (make-vec 103 [])}) [:sss :hhh :mmm :mmh :mms :ssh :ssm :hhs :hhm :hsm]))]
    (doseq [adataset datasets]
      (if (empty? (get adataset :subjects))
        (do
          (when (dplev :all)
            (println "Warning: Trial" (get (get adataset :meta-data) :trial-number) "Bad or empty rtd dataset.")
            (pprint adataset)))
        (let [{subjects :subjects
               role-time :role-time
               medic-role :medic-role
               metadata :meta-data} adataset
              subject-roles (into [] (map (fn [subj]
                                            (let [subjroles (get role-time subj)
                                                  ssrole (first-time-role (get subjroles "Search_Specialist"))
                                                  tsrole (first-time-role (get subjroles "Transport_Specialist"))
                                                  msrole (first-time-role (get subjroles "Medical_Specialist"))
                                                  esrole (first-time-role (get subjroles "Engineering_Specialist"))
                                                  hsrole (first-time-role (get subjroles "Hazardous_Material_Specialist"))]
                                              (cond (and (< ssrole msrole) (< ssrole hsrole)) "Search_Specialist"
                                                    (and (< tsrole msrole) (< tsrole hsrole)) "Trransport_Specialist"
                                                    (and (< msrole ssrole) (< msrole hsrole)) "Medical_Specialist"
                                                    (and (< hsrole ssrole) (< hsrole msrole)) "Hazardous_Material_Specialist"
                                                    (and (< esrole ssrole) (< esrole msrole)) "Engineering_Specialist"
                                                    :otherwise (do
                                                                 (when (dplev :all) (println "Trial" (get metadata :trial-number) "Participant" subj "has unknown role"))
                                                                 "Unknown_Role"))))
                                          subjects))
              ;;_ (when (dplev :all) (println "Subject roles=" subject-roles))
              story-strategy (apply role-strategy subject-roles)
              ;;_ (when (dplev :all) (println "story-strategy=" story-strategy))
              scoresbyplayer (into [] (map (fn [player]
                                             (let [playerdata (get medic-role player)
                                                   {eta :elapsed-time-array
                                                    vsp :victim-saved-points} playerdata
                                                   scores (if (and (> (count eta) 0) (= (count eta) (count vsp)))
                                                            (loop [prvbucket 0
                                                                   running-total 0
                                                                   buckets []
                                                                   timescored eta
                                                                   scoreat vsp]
                                                              ;;(when (dplev :all) (println "timescored=" timescored "scoreat=" scoreat))
                                                              (if (and (not (empty? timescored)) (not (empty? scoreat)))
                                                                (let [bucket (int (/ (first timescored) 10))]
                                                                  ;;(when (dplev :all) (println "timescored=" (first timescored) "scoreat=" (first scoreat) "bucket=" bucket))
                                                                  (if (> bucket (+ prvbucket 1))
                                                                    (recur (+ prvbucket 1)
                                                                           running-total
                                                                           (conj buckets running-total)
                                                                           timescored
                                                                           scoreat)
                                                                    (recur bucket
                                                                           (first scoreat)
                                                                           (conj buckets (first scoreat))
                                                                           (rest timescored) (rest scoreat))))
                                                                buckets)))
                                                   scores (stretchto scores 91)]
                                               ;; (when (dplev :all) (println "scores=" scores))
                                               scores))
                                           subjects))
              ;;_ (when (dplev :all) (println "scoresbyplayers=" scoresbyplayer))
              summedscoresbyplayers (into [] (apply map (fn [& data] (apply + data)) scoresbyplayer))]
          (when (dplev :all) (println "Story strategy" story-strategy "summedscoresbyplayers=" summedscoresbyplayers))
          (let [scores summedscoresbyplayers]
            (dotimes [bucket (count scores)]
              (let [score (nth scores bucket)]
                (reset! (nth (get mapofvecs story-strategy) bucket)
                        (conj @(nth (get mapofvecs story-strategy) bucket) score))))))))

            ;; (dotimes [bucket (count scores)]
            ;;   (let [score (nth scores bucket)
            ;;         values @(nth (get mapofvecs story-strategy) bucket)]
            ;;     (reset! (nth (get mapofvecs story-strategy) bucket)
            ;;             (moments values))))

    (into {} (map (fn [[strategy moms]]
                    {strategy (into [] (map (fn [collected] (moments @collected)) moms))})
                  mapofvecs))))
    #_mapofvecs

;;; (rstrategy-to-score-moments  example-datasets)

;;; Gives a very simplistic estimate of the score at any point in time up to 15 minutes
;;; based on a straight line to the expected result.  This corresponds to the learned version
;;; This stand-in is to cover the case wher ethere is not a learned model.

(defn get-strategy-moments
  [strategy elapsed-milliseconds]
  (let [[final-score sd] (case strategy
                           :hsm         (if (== (seglob/get-trial-number) 1)
                                          [533.33 150.27]
                                          [686.00 101.90])
                                        ;[383.46 83.85]
                           :ssh         [371.64 61.62]
                           :mmh         [385.00 20.00]
                           :hhs         [385.00 36.97]
                           :hhm         [443.33 109.69]
                           :ssm         [384.29 75.47]
                           :mms         [410.00 75.00]
                           :hhh         [386.07 77.83]
                           :mmm         [386.07 77.83]
                           :sss         [386.07 77.83]

                           :allroles    [386.07 77.83]
                           :one-of-each [383.46 83.85]
                           :h1s2        [371.64 61.62]
                           :h1m2        [385.00 20.00]
                           :h2s1        [385.00 36.97]
                           :h2m1        [443.33 109.69]
                           :m1s2        [384.29 75.47]
                           :m2s1        [410.00 75.00]
                           :h3          [386.07 77.83]
                           :m3          [386.07 77.83]
                           :s3          [386.07 77.83]
                           [386.07 77.83])
        estimated-current-score (/ (* final-score elapsed-milliseconds) (* 17 60 1000.0))
        proratedsd (/ (* final-score elapsed-milliseconds) (* 17 60 1000.0))]
    [estimated-current-score proratedsd final-score sd]))

(defn get-score-moments-at
  [lpm strategy elapsed-milliseconds]
  (let [model (get lpm :role-strategy-to-score-moments)
        model-for-strategy (get model strategy)
        bucket (Math/round (/ elapsed-milliseconds (* 10 1000.0))) ; 10 second buckets
        moments (or (and model-for-strategy
                         (> (count model-for-strategy) bucket)
                         (nth model-for-strategy bucket))
                    ())
        est-moments (if (empty? moments)
                      (get-strategy-moments strategy elapsed-milliseconds)
                      [(first moments)                      ; predicted score for the ms time
                       (second moments)                     ; standard deviation for the model at ms time
                       (first (last model-for-strategy))    ; predicted mean final score
                       (second (last model-for-strategy))])] ; sd for final score
    (if (empty? moments)
      (when (dplev :warn) (println "** Learned moments not found for strategy=" strategy "model-for-strategy=" model-for-strategy "buckey=" bucket "using estimated moments=" est-moments))
      (when (dplev :all) (println "From model instantaneous and final score moments:" est-moments)))
    est-moments))

(defn generate-model-from-datasets
  [datasets]
  (when (dplev :all) (println "Processing" (count datasets) "rtd data files"))
  (let [_ (when (dplev :all) (println "room-visit-order..."))
        room-visit-order (build-models-for-room-visits datasets)
        _ (when (dplev :all) (println "time-in-room..."))
        time-in-room (overall-time-in-room datasets)
        _ (when (dplev :all) (println "per-room-times..."))
        per-room-times (by-room-time-in-room datasets)
        _ (when (dplev :all) (println "speed-between-rooms..."))
        speed-between-rooms (overall-speed-between-rooms datasets)
        _ (when (dplev :all) (println "per-pair-room-to-room-speed..."))
        per-pair-room-to-room-speed (room-to-room-speed datasets) ;+++ needs work
        _ (when (dplev :all) (println "role-strategy-to-score-moments..."))
        role-strategy-to-score-moments (rstrategy-to-score-moments datasets)]
    {:format-version learned-model-format
     :room-visit-order room-visit-order
     :overall-time-in-room time-in-room
     :time-in-room-by-room per-room-times
     :overall-speed-between-rooms speed-between-rooms
     :room-to-room-speeds per-pair-room-to-room-speed
     :role-strategy-to-score-moments role-strategy-to-score-moments}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Learned victim processing rates

;; (defn victim-save-rate
;;   [datasets]
;;   (let [initial-config (


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; learned model I/O

(defn read-learned-data
  [filename]
  (if (not (.exists (clojure.java.io/as-file filename)))
    (do (when (dplev :all :error :io) (println "*** File not found:" filename)) nil)
    (let [data (read-string (slurp filename))]
      (if (and data (= (:format-version data) learned-model-format))
        (do (when (dplev :all :io) (println "Reading learned model from" filename))
            data)
        (do (when (dplev :all :error :io) (println "Data format (" (:format-version data)
                     ") does not match (" learned-model-format ")"))
            nil)))))

;;; (write-learned-data test2 "learned-data.foo")

(defn write-learned-data
  [datasets filename]
  (let [data (generate-model-from-datasets datasets)]
    (spit filename (pr-str data))
    (let [reread-data (read-learned-data filename)]
      #_(when (not (= data reread-data))
          (when (dplev :all)
            (println "Learned Data failed to read back correctly (" filename ")")
            (pprint data)
            (pprint reread-data)))
      data)))

;;; Outside interface
(defn load-learned-model
  [path]
  (if (and path (.exists (io/file path)))
    ;; Read in the default, but can be overwritten by the communicated model.
    (seglob/set-learned-model! (read-learned-data path) false)
    (if path (when (dplev :all) (println "Learned model " path "was not found")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Import training data

(defn read-training-data-from-list-of-files
  [list-of-pathnames]
  (let [datasets (remove nil? (into [] (map
                                        (fn [pn]
                                          (let [content (read-stat-data pn)]
                                            (if (empty? content)
                                              (when (dplev :all) (println "*** Bad RTD file:" pn)))
                                            content))
                                        list-of-pathnames)))]
    datasets))

(defn load-training-data-from-directory
  [rootpath topath]
  (let [traindir (clojure.java.io/file rootpath)]
    (cond (.isDirectory traindir)
          (let [list-of-files (into [] (.list traindir))
                list-of-pathnames (map (fn [fn] (str traindir "/" fn)) list-of-files)
                datasets (read-training-data-from-list-of-files list-of-pathnames)
                model (write-learned-data datasets topath)]
            (when (dplev :all) (println "Generated model from datasets:")
                  (pprint model)))

          :otherwise (println rootpath " is not a directory"))))

;;; (def tdata (load-training-data-from-directory "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train" "model.lpm"))
;;; (def *rtd-data* (load-training-data-from-directory "/Volumes/projects/RITA/HSR-data-mirror/study-2_2021.06-rmq-timing"  "model.lpm")

;;; Fin
