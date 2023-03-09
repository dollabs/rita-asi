;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.teamstrength
  "Observations from the testbed."
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
            ;[rita.state-estimation.volumes :as vol :refer :all]
            ;[rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            [rita.state-estimation.ras :as ras]
            ;;[rita.state-estimation.rlbotif :as rlbotif]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            ;;[rita.state-estimation.interventions :as intervene]
            ;[rita.state-estimation.rita-se-core :as rsc]
            ;[rita.state-estimation.cognitiveload :as cogload]
            ;[rita.state-estimation.interventions :as intervene]
            ;;[rita.state-estimation.predictions :as predict]

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

#_(in-ns 'rita.state-estimation.teamstrength)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Participant strength tracking

;; All participant strengths are in standard deviations from the mean. 0.0, therefore means "mean".
;; Mean and standard deviations are learned from the traning sets.  If the mean for some measure is 10
;; and the standard deviation is 2, a participant who gets 12 would therefore have "exceeds-by-sds" = 1.0
;; A participants who gets 6n would therefore have "exceeds-by-sds" = -2.0.  It is up to the callers to ensure
;; that correct values are supplied.  "Strength" is number of standard deviations above the mean a participant is.
;; A strength of 0.0 is "avarage".

;;; This is where we decide how much of the history to use and how to weight recent performance

;;; Beware, if the trial lengths change these have to be carefully adjusted.

(def windowsize 5)
(def trial-duration-ms (* 17 60 1000))         ; For study 3 this means 2 minute planning followed by 15 minutes for the game
(def planning-epoch-duration-ms (* 2 60 1000)) ; The first epoch is shorted than the others (two minutes)
(def epoch-duration-ms (* 3 60 1000))          ; Three minutes in milliseconds (except for epoch 0
(def num-epochs (+ 1 (/ 15 3)))                ; 1 planning epoch and 5 game epochs
(def num-trials 2)                             ; Two trials




;;; Here is what we track in player performance

(def external-strength-rep-map
  {:number-of-relocated-victims :rv,   ; Moved to a more convenient place (not to the evacuation location
   :number-of-evacuated-victims :ev,   ; Number of triaged victims moved to the correct evacuation loc
   :evacuated-critical-victims  :ec,   ; Number of triaged critical victims moved to the correct evacuation location
   :number-of-victims-evacuated-to-wrong-location :ie, ; Like :ev but to the wrong evacuation area.
   :awakened-victims            :av,   ; Number of victims awakened (Medic plus one other)
   :triaged-victims             :tv,   ; Number of victims triages (Medic only)
   :triaged-critical-victims    :tc,   ; Number of victims triages (Medic only)
   :rubble-removed              :rr,   ; Number of rubble blocks removed (Engineer only)
   :markers-placed              :mp,   ; Number of markers placed
   :markers-removed             :mr,   ; Number of markers removed
   :spoken-words                :sw,   ; Number of spoken words
   :spoken-utterances           :su,   ; Number of times the participant spoke
   :sumdist-medic               :sm,   ; Sum of distances to medic
   :sumdist-transporter         :st,   ; Sum of distances distance to transported
   :sumdist-engineer            :se,   ; Sum of distances to the engineer
   :numdist-medic               :nm,   ; Number of medic distance samples
   :numdist-transporter         :nt,   ; Number of transporter samples
   :numdist-engineer            :ne,   ; Number of engineer samples
   ;; +++ addtime medic trapped
   ;; +++ add dist to other participants.
   })

(def internal-map-strength-map (clojure.set/map-invert external-strength-rep-map))

(defn initialize-player-strength-history
  [pid]
  {pid (into []
             (take num-trials
                   (repeatedly (fn []
                                 (into []
                                       (take num-epochs
                                             (repeatedly (fn []
                                                           (into {}
                                                                 (map (fn [key] {key (atom 0)})
                                                                      (vals external-strength-rep-map)))))))))))})

(defn add-player-strength-data
  [strengthdata]
  (seglob/set-player-strength-data (merge (seglob/get-player-strength-data) strengthdata)))

(defn add-trial-1-initial-strength-data
  [team-members]
  (doseq [tm team-members]
    (let [empty-strength-data (initialize-player-strength-history tm)]
      (if (dplev :teamstrength :all)
        (println "Adding trial 1 player strength for" tm))
      (add-player-strength-data empty-strength-data))))

(def error-atom (atom 666))             ; Represents an error value for missing data

(defn strength-data-lookup-aux
  [db pid trial epoch external-category]
  (if (or (< epoch 0) (> epoch 5))  ; +++ constants
    (when (dplev :error :all)
      (println "ERROR: epoch=" epoch " is out of bounds in strength-data-lookup-aux")
      error-atom)
    (let [player-data (get db pid) ; (seglob/get-player-strength-data)
          trialdata (and player-data (nth player-data (- trial 1)))
          epoch-map (and trialdata (nth trialdata epoch))
          datum (and epoch-map (get epoch-map external-category))]
      (cond
        (not player-data)
        (do
          (if (and (dplev :all :error)
                   (dont-repeat 'strength-data-lookup-aux pid trial epoch external-category))
            (println "In strength-data-lookup-aux, no entry in the strength data db for participant " pid "valid keys are" (keys db)))
          error-atom)

        (not trialdata)
        (do
          (if (and (dplev :all :error)
                   (dont-repeat 'strength-data-lookup-aux pid trial epoch external-category))
            (println "In strength-data-lookup-aux, no entry for trial" trial "in the strength data db for participant " pid))
          error-atom)

        (not epoch-map)
        (do
          (if (and (dplev :all :error)
                   (dont-repeat 'strength-data-lookup-aux pid trial epoch external-category))
            (println "In strength-data-lookup-aux, no entry for epoch" epoch "in the strength data db for participant " pid))
          error-atom)

        (not datum)
        (do
          (if (and (dplev :all :error)
                   (dont-repeat 'strength-data-lookup-aux pid trial epoch external-category))
            (println "In strength-data-lookup-aux, no entry for ext. category" external-category "in the strength data db for participant " pid))
          error-atom)

        :otherwise
        datum))))

(defn increment-strength-data-aux
  [pid trial epoch increment-by external-category]
  (if (not (get (seglob/get-player-strength-data) pid))
    (if (dplev :error :teamstrength :all)
      (println "ERROR: ***** Bad player pid" pid "unknown player")))
  (let [db (seglob/get-player-strength-data)
        datum (strength-data-lookup-aux db pid trial epoch external-category)
        value (if (instance? clojure.lang.Atom datum) @datum -1)
        new-val (+ value increment-by)]
    (if (not (instance? clojure.lang.Atom datum))
      (if (and (dplev :error :teamstrength :all)
               (dont-repeat 'increment-strength-data-aux pid trial epoch external-category datum))
        (println "ERROR in increment-strength-data-aux: Bad player-strength value" datum "found where an Atom was expected"))
      (do
        (if (dplev :debugteamstrength :all)
          (println "Incrementing" external-category "for" pid "to" new-val))
        (reset! datum new-val)))))

(defn get-strength-data-for
  [db pid trial em category]
  (let [external-category (get external-strength-rep-map category)
        epoch (if (>= em planning-epoch-duration-ms) (+ 1 (quot (- em planning-epoch-duration-ms) epoch-duration-ms)) 0)
        datum (strength-data-lookup-aux db pid trial epoch external-category)]
    (cond
      (not external-category)
      (do (if (dplev :warn :teamstrength :all)
            (println "WARN: Unknown strength category " category "supplied to increment-strength-data-for"))
          0)

      (>= epoch num-epochs)
      (do (if (dplev :warn :teamstrength :all)
            (println "WARN: The elapsed-milliseconds " em "has passed the end of the trial "))
          0)

      (or (> trial 2) (< trial 1))
      (do (if (and (dplev :error :all)
                   (dont-repeat 'increment-strength-data-aux pid trial epoch external-category datum))
            (println "ERROR: Trial number " trial "must be be either 1 or 2"))
          0)

      :otherwise
      (cond  (not (instance? clojure.lang.Atom datum))
             (do
               (if (and (dplev :error :teamstrength :all)
                        (dont-repeat 'increment-strength-data-aux pid trial epoch external-category datum))
                 (do (println "ERROR in get-strength-data-for" [pid trial em category] ": Bad player-strength value" datum "found where an Atom was expected")
               0)))

             (not (number? @datum))
             (if (and (dplev :error :teamstrength :all)
                      (dont-repeat 'increment-strength-data-aux pid trial epoch external-category datum))
               (do (println "ERROR in get-strength-data-for: Bad player-strength value" @datum "in" datum "found where an Number was expected")
                 0))

             :otherwise
             @datum))))

(defn set-strength-data-for
  [db pid trial em category nuval]
  (let [external-category (get external-strength-rep-map category)
        epoch (if (>= em planning-epoch-duration-ms) (+ 1 (quot (- em planning-epoch-duration-ms) epoch-duration-ms)) 0)
        datum (strength-data-lookup-aux db pid trial epoch external-category)]
    (cond
      (not external-category)
      (do (if (dplev :error :warn :teamstrength :all)
            (println "WARN: Unknown strength category " category "supplied to increment-strength-data-for")))

      (>= epoch num-epochs)
      (do (if (dplev :warn :teamstrength :all)
            (println "WARN: The elapsed-milliseconds " em "has passed the end of the trial ")))

      (or (> trial 2) (< trial 1))
      (do (if (and (dplev :error :all)
                   (dont-repeat 'set-strength-data-aux pid trial epoch external-category datum))
            (println "ERROR: Trial number " trial "must be be either 1 or 2")))

      :otherwise
      (cond  (not (instance? clojure.lang.Atom datum))
             (do
               (if (and (dplev :error :teamstrength :all)
                        (dont-repeat 'set-strength-data-aux pid trial epoch external-category datum))
                 (do (println "ERROR in set-strength-data-for: Bad player-strength" [pid trial em category] ": value" datum "found where an Atom was expected"))))

             (not (number? @datum))
             (if (and (dplev :error :teamstrength :all)
                      (dont-repeat 'set-strength-data-aux pid trial epoch external-category datum))
               (do (println "ERROR in set-strength-data-for: Bad player-strength value" @datum "in" datum "found where an Number was expected")))

             :otherwise
             (reset! datum nuval)))))

(defn get-cumulative-strength-data-for
  [db pid trial em category]
  nil)


(defn increment-strength-data-for
  [pid trial em category & [n]]
  (let [external-category (get external-strength-rep-map category)
        increment-by (if (number? n) n 1)
        epoch (if (> em planning-epoch-duration-ms) (+ 1 (quot (- em planning-epoch-duration-ms) epoch-duration-ms)) 0)]
    (cond
      (not external-category)
      (do (if (dplev :debug-teamstrength :warn :all)
            (println "WARN: Unknown strength category " category "supplied to increment-strength-data-for"))
          0)

      (>= epoch num-epochs)
      (do (if (dplev :debug-teamstrength :warn :all) ;+++ remove :error later to stop nuisance errors past the end of the run
            (println "WARN: The elapsed-milliseconds " em "has passed the end of the trial "))
          0)

      (string? trial)
      (if (and (dplev :debug-teamstrength :error :all)
               (dont-repeat 'increment-strength-data-for pid trial epoch external-category))
        (println "ERROR: Trial number " trial "cannot be a string - it must be a number (1 or 2)")
        0)

      (or (> trial 2) (< trial 1))
      (do (if (and (dplev :debug-teamstrength :error :all)
                   (dont-repeat 'increment-strength-data-for pid trial epoch external-category))
            (println "ERROR: Trial number " trial "must be be either 1 or 2"))
          0)

      :otherwise
      (do
        (if (dplev :debug-teamstrength :all)
          (println "TEAMSTRENGTH: Adding" category "strength data for" pid))
        (increment-strength-data-aux pid trial epoch increment-by external-category)))))

(defn convert-epoch-ms
  [epoch]
  (if (== epoch 0)
      0
      (+ planning-epoch-duration-ms (* (- epoch 1) epoch-duration-ms))))

(defn get-distance-categories
  [arole]
  (let [suffix (case arole
                  "Search_Specialist"             "transporter"
                  "Engineering_Specialist"        "engineer"
                  "Medical_Specialist"            "medic"
                  "Transport_Specialist"          "transporter"
                  "Hazardous_Material_Specialist" "engineer"
                  (do
                    (if (dplev :all :error :warn)
                      (println "ERROR: Unknown role type" arole))
                    nil))]
  [(keyword (str "sumdist-" suffix))
   (keyword (str "numdist-" suffix))]))

(defn record-distance
  [pidme pidother dist em]
  (cond
    (seglob/get-mission-started)            ; Ignore if mission not yet started
    (do
      (when (not (seglob/get-assigned-role pidother))
        (println "pidother=" pidother "has no role assignment, possibilities are as follows:")
        (pprint (seglob/get-roles-assigned)))
      (let [catother (get-distance-categories (seglob/get-assigned-role pidother))]
        #_(if (dplev :all :teamstrength)
          (println "playerdist" pidme pidother "=" dist "at" em))
        (increment-strength-data-for pidme (seglob/get-trial-number) em (first catother) dist); distance
        (increment-strength-data-for pidme (seglob/get-trial-number) em (second catother) 1))))) ; count of samples

(defn get-ps-value
  [db pid trial epoch category]
  (get-strength-data-for db pid trial (convert-epoch-ms epoch) category))

(defn set-ps-value
  [db pid trial epoch category nuval]
  (set-strength-data-for db pid trial (convert-epoch-ms epoch) category nuval))

(defn get-cumulative-ps-value
  [pid category]
  (let [db (seglob/get-player-strength-data)
        trial (seglob/get-trial-number)
        data (into [] (map #(get-ps-value db pid trial % category) (range 0 6)))]
    (reduce + data)))

(defn average-distance
  [pid1 role2 epoch trial]
  (let [db (seglob/get-player-strength-data)
        distance-categories (get-distance-categories role2)
        total-distance (get-ps-value db pid1 trial epoch (first distance-categories))
        number-of-measurements (get-ps-value db pid1 trial epoch (second distance-categories))]
    (cond (and (number? total-distance)
               (number? number-of-measurements)
               (not (==  number-of-measurements 0)))
          (/ total-distance number-of-measurements)

          (nil? total-distance)
          (when (dplev :error :all)
            (println "ERROR: (get-ps-value db" pid1 trial epoch (first distance-categories) ") is NULL")
            16)

          (nil? number-of-measurements)
          (when (dplev :error :all)
            (println "ERROR: (get-ps-value db" pid1 trial epoch (second distance-categories) ") is NULL")
            16)

          (not (number? total-distance))
          (when (dplev :error :all)
            (println "ERROR: (get-ps-value db" pid1 trial epoch (first distance-categories) ") =" total-distance "is not a number")
            16)

          (not (number? number-of-measurements))
          (when (dplev :error :all)
            (println "ERROR: (get-ps-value db" pid1 trial epoch (second distance-categories) ") =" number-of-measurements "is not a number")
            16)

            :otherwise
            0)))

(defn get-average-distance
  [role1 role2 em & [start-epoch end-epoch trial]] ; if start-epoch or end-epoch is nil use current-epoch, if trial is nil use current trial
   (let [rolemap (seglob/get-roles-assigned)
         role1pid (some #(if (= (val %) role1) (key %)) rolemap)
         role2pid (some #(if (= (val %) role2) (key %)) rolemap)
         start-epoch (if start-epoch start-epoch (ras/compute-epoch-from-ms em))
         end-epoch (if end-epoch end-epoch (ras/compute-epoch-from-ms em))
         trial (if trial trial (seglob/get-trial-number))
         results (map
                  #(average-distance role1pid role2 % trial)
                  (range start-epoch (+ end-epoch 1)))
         ad (ras/average results)]
     ad))

;; This is for Trial 1 where the new team members should not already be in the database.  If they are
;; we will just leave them, but if not we will add them.
(defn verify-no-current-team-players-in-db
  [teammembers tmdata]
  (doseq [tm teammembers]
    (println "TEAMSTRENGTH: verify-no-current-team-players-in-db Handling team member" tm)
    (let []
      (cond (empty? (get tmdata tm))
            (add-trial-1-initial-strength-data [tm]) ; normal case, add the participant

            :otherwise
            ;; Data is here and it shouldn't be, but we'll use it.
            (do
              ;;; +++anomaly candidate.  Nothing to add, it is already there.
              (if (dplev :teamstrength :all :warn)
                (println "Anomaly: Participant " tm "already has data which should not occur in trial-1")))))))

;; This is for Trial 2, the players should already be in the db, but if not we add the missing cases.
;; This could happen is a player had to be substituted.
(defn verify-all-current-team-players-in-db
  [teammembers tmdata]
  (doseq [tm teammembers]
    (println "TEAMSTRENGTH: verify-all-current-team-players-in-db: Handling team member" tm)
    (let []
      (cond (not (get tmdata tm))
            (do                         ; They should be there, but we will add them.
              ;;; +++anomaly candidate
              (if (dplev :teamstrength :all :warn)
                (println "Anomaly: Participant " tm "is not represented in the data from trial 1"))

              (add-trial-1-initial-strength-data [tm])))))) ; Add "newbie" data

(defn bad-pd-format?
  [prior-participant-strength]
  false) ; +++ do a real test here


(defn enatom-participant-strength-data
  [data]
  (cond
    (map? data)
    (into {} (map (fn [[k v]] {k (enatom-participant-strength-data v)}) data))

    (vector? data)
    (into [] (map enatom-participant-strength-data data))

    (list? data)
    (into () (map enatom-participant-strength-data data))

    (number? data)
    (atom data)

    :otherwise data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; ingesting and digesting training data from traing trials (no assistant)

;;; Put the data that was read in, if any, in as the current database
;;; Then add the new team members, if they are not already there.

(defn learn-moments-from-training-data
  []
  (let [td (seglob/get-training-data-data)
        dbm (seglob/get-learned-mean-data)
        dbs (seglob/get-learned-sd-data)
        particips (keys td)]
    (doseq [trial (range 1 3)]             ; Trials 1 and 2
      (doseq [epoch (range 0 6)]           ; Planning epoch (0), mission epochs (1-5) ;; +++ constants
        (doseq [variable (keys external-strength-rep-map)] ; use internal names
          ;; Now collect data from all participants, compute mean and sd and update the mean and sd dbs.
          (let [tdata (into [] (map (fn [part] [part (get-ps-value td part trial epoch variable)]) particips))
                tran  (into []
                            (remove nil?
                                    (map (fn [[part val]]
                                           (if (== 0 (get-ps-value td part trial 0 :sumdist-transporter))
                                             val))
                                         tdata)))
                eng   (into []
                            (remove nil?
                                    (map (fn [[part val]]
                                           (if (== 0 (get-ps-value td part trial 0 :sumdist-engineer))
                                             val))
                                         tdata)))
                med   (into []
                            (remove nil?
                                    (map (fn [[part val]]
                                           (if (== 0 (get-ps-value td part trial 0 :sumdist-medic))
                                             val))
                                         tdata)))
                tdmntran (ras/mean tran)
                tdmneng  (ras/mean eng)
                tdmnmed  (ras/mean med)
                tdsdtran (ras/standard-deviation tran)
                tdsdeng  (ras/standard-deviation eng)
                tdsdmed  (ras/standard-deviation med)]
            (if (dplev :all :error)
              (if (not (and (== (count tran) (count eng)) (== (count tran) (count med))))
                (println "ERROR: unequal distribution of roles, eng=" (count eng) "tran=" (count tran) "med=" (count med))))
            (set-ps-value dbm "Transport_Specialist" trial epoch variable tdmntran)
            (set-ps-value dbs "Transport_Specialist" trial epoch variable tdsdtran)
            (set-ps-value dbm "Engineering_Specialist" trial epoch variable tdmneng)
            (set-ps-value dbs "Engineering_Specialist" trial epoch variable tdsdeng)
            (set-ps-value dbm "Medical_Specialist" trial epoch variable tdmnmed)
            (set-ps-value dbs "Medical_Specialist" trial epoch variable tdsdmed)))))
    #_(do (println "Results of learning from training data (mean)")
        (pprint dbm)
        (println "Results of learning from training data (sd)")
        (pprint dbs))))

;;; Easy access functions for intervention guards and handlers
;;; Accept various forms where a pid is required.
(defn translate-pid
  [pid]
  (case pid
    ;; Allow internal names
    :tran                    (seglob/get-participant-id-from-call-sign "Green")
    :med                     (seglob/get-participant-id-from-call-sign "Red")
    :eng                     (seglob/get-participant-id-from-call-sign "Blue")
    ;; Allow call signs
    "Green"                  (seglob/get-participant-id-from-call-sign pid)
    "Blue"                   (seglob/get-participant-id-from-call-sign pid)
    "Red"                    (seglob/get-participant-id-from-call-sign pid)
    ;; Allow role names
    "Transport_Specialist"   (seglob/get-participant-id-from-call-sign "Green")
    "Engineering_Specialist" (seglob/get-participant-id-from-call-sign "Blue")
    "Medical_Specialist"     (seglob/get-participant-id-from-call-sign pid)
    ;; Allow pids !
    pid))

(defn get-ps-moments
  [role epoch variable]
  (let [dbm (seglob/get-learned-mean-data)
        dbs (seglob/get-learned-sd-data)]
    [(get-ps-value dbm role (seglob/get-trial-number) epoch variable)
     (get-ps-value dbs role (seglob/get-trial-number) epoch variable)]))

(defn get-learned-bounds
  [role epoch variable numsd]
  (let [[mean sd] (get-ps-moments role epoch variable)]
    [(- mean (* numsd sd)) (+ mean (* numsd sd))]))

(defn get-ps-var
  [pid epoch variable]
  (let [pid (translate-pid pid)
        psdb (seglob/get-player-strength-data)
        trial (seglob/get-trial-number)]
    (if (or (empty? psdb) (empty? (keys psdb)))
      (do (when (and (dplev :all :error) (dont-repeat :get-ps-var pid epoch variable))
            (print "ERROR: in get-ps-var player-strength-data is missing. pid=" pid "epoch="epoch "variable=" variable "psdb=" psdb))
          0)
      (get-ps-value psdb pid trial epoch variable))))

(defn compared-to-training
  [pid epoch variable numsd]
  (let [role (seglob/get-assigned-role pid)
        pidvalue (get-ps-var pid epoch variable)
        ;; _ (println "In compared-to-learning with pid=" pid "role=" role "pidvalue=" pidvalue " variable=" variable)
        [lower upper] (get-learned-bounds role epoch variable numsd)]
    (cond (> pidvalue upper)  :above
          (< pidvalue lower)  :below)
          :otherwise          :within))

(defn above-learned-range
  [pid epoch variable numsd]
  (let [pid (translate-pid pid)]
    (= (compared-to-training pid epoch variable numsd) :above)))

(defn below-learned-range
  [pid epoch variable numsd]
  (let [pid (translate-pid pid)]
    (= (compared-to-training pid epoch variable numsd) :below)))

(defn marker-usage
  [pid epoch numsd]
  (let [pid (translate-pid pid)
        placement (compared-to-training pid epoch :markers-placed  numsd)
        removal   (compared-to-training pid epoch :markers-removed numsd)]
    [placement removal]))

;;; Process training data
(defn set-training-data-data [training-strength-data]
  (println "In set-training-data-data") ; +++ REMOVEME
  ;; First of all set the read-in player strength data,
  (let [tdd (enatom-participant-strength-data training-strength-data)]
    (seglob/set-training-data-data tdd)
    (println "Loaded training strength data from" (count (keys tdd)) "participants")
    ;; establish mean and sd datastructures
    (seglob/set-learned-mean-data (into {} [(initialize-player-strength-history "Transport_Specialist")
                                            (initialize-player-strength-history "Engineering_Specialist")
                                            (initialize-player-strength-history "Medical_Specialist")]))
    (seglob/set-learned-sd-data (into {} [(initialize-player-strength-history "Transport_Specialist")
                                          (initialize-player-strength-history "Engineering_Specialist")
                                          (initialize-player-strength-history "Medical_Specialist")]))
    (learn-moments-from-training-data)))

;;; Put the data that was read in, if any, in as the current database
;;; Then add the new team members, if they are not already there.

(defn set-participant-strength-data [prior-participant-strength teammembers trialno]
  ;; (println "In set-participant-strength-data with prior-participant-data=" prior-participant-strength "teammembers="teammembers "trialno=" trialno) ; +++ REMOVEME
  (if (dplev :all :io)
    (println "In set-participant-strength-data teammembers=" teammembers "trialno=" trialno))
  ;; First of all set the read-in player strength data, then check to see if we need to add the
  ;; new players, making sure not to have two copies of anyone.
  (let [pps (enatom-participant-strength-data prior-participant-strength)]
    (seglob/set-player-strength-data pps)
    (cond
      (empty? pps)
      (if (== trialno 1)
        (add-trial-1-initial-strength-data teammembers)
        (do
          ;; anomaly candidate!  For trial 2 we should have data!!! ++++
          (add-trial-1-initial-strength-data teammembers)))

      (bad-pd-format? pps)
      (do
        (if (dplev :error :teamstrength :all)
          (println "The team-strength data that was read from file had the wring format"))
        (add-trial-1-initial-strength-data teammembers))

      :otherwise
      (cond (== trialno 1)                ; Add the new members unless they are already there
            (verify-no-current-team-players-in-db teammembers pps)

            (== trialno 2)                ; Don't add unless they are missing
            (verify-all-current-team-players-in-db teammembers pps)

            :otherwise
            (if (dplev :error :teamstrength :all)
              (println "TEAMSTRENGTH What is this trial number:" trialno))))))

(defn deatom-participant-strength-data
  [data]
  (cond
    (map? data)
    (into {} (map (fn [[k v]] {k (deatom-participant-strength-data v)}) data))

    (vector? data)
    (into [] (map deatom-participant-strength-data data))

    (list? data)
    (into () (map deatom-participant-strength-data data))

    (instance? clojure.lang.Atom data)
    @data

    :otherwise data))

;;; If there is no history, use the inherited prior, otherwise if window history exists, ignore the prior
(defn strength-weighted-average
  [prior history]
  (if (== (count history) 0)
    (or prior 0.0)                      ; If no history, then prior, if no prior then 0.0
    (let [prior (or prior 0.0)
          historysize (count history)
          priorweight (if (>= historysize windowsize) 0.0 (- windowsize historysize))
          usedata (min historysize windowsize)
          recent-performance (/ (reduce + (take-last usedata history)) usedata)]
      (if (dplev :all) (println "historysize=" historysize "priorweight=" priorweight "usedata=" usedata "recent-performance=" recent-performance))
      (/ (+ (* prior priorweight)
            (* recent-performance usedata))
         (+ priorweight usedata)))))

;;; (strength-weighted-average 1.2 [1 2 3 4 5 6 7])
;;; (strength-weighted-average nil [1 2 3 4 5 6 7])

(defn update-participant-strength
  [participant-id exceeds-by-sds]
  (let [prior-strength (seglob/get-participant-strength (keyword participant-id) nil)
        history (conj (seglob/get-player-performance-history participant-id []) exceeds-by-sds)
        estimated-strength (strength-weighted-average prior-strength history)]
    (seglob/set-player-performance-histories
     (merge (seglob/get-player-performance-histories) {participant-id history}))
    estimated-strength))

(defn current-participant-strength
  [participant-id]
  (let [prior-strength (seglob/get-participant-strength (keyword participant-id) nil)
        history (seglob/get-player-performance-history participant-id [])]
    (strength-weighted-average prior-strength history)))

;;; Strength of the team is the average of the strength of the team participants (+++ perhaps weight unevenly by role?)
(defn current-team-strength
  []
  (if (not (empty? (seglob/get-team-members)))
    (/ (reduce + (map current-participant-strength (seglob/get-team-members))) (count (seglob/get-team-members)))
    (do
      (if (dplev :warn :all) (println "*** WARNING: trying to get team strength when no team has been registered!"))
      0.0)))

;;; A simple way to attribute performance evenly over the team members.
(defn update-team-strength
  [exceeds-by-sds]
  (doseq [member (seglob/get-team-members)]
    (update-participant-strength member exceeds-by-sds))
  (current-team-strength))

;;; How we model and track team strength, which consists of player strength

(defn save-participants-strength
  [path]
  ;; Iterate through all participants and reset their to prior to the current estimate
  (doseq [akey (keys (seglob/get-player-performance-histories))]
    (let [strength (current-participant-strength akey)]
      (if (dplev :all) (println "strength=" strength "akey=" akey "*participant-strength*=" (seglob/get-participant-strength))
          (seglob/set-participant-strength (keyword akey) strength))))
  ;; Save to the participant strength file
  (if (dplev :io :all) (println "saving participant-strength-file-to: " (seglob/get-participant-strength-file)))
  (let [dir (fs/parent (seglob/get-participant-strength-file))]
    (when (not (fs/exists? dir))
      (fs/mkdir dir)
      (if (dplev :all) (println "CWD" fs/*cwd*))
      (if (dplev :all) (println "Created runtime dir" (fs/absolute dir))))
    (spit (seglob/get-participant-strength-file) (pr-str (seglob/get-participant-strength-data)))))


(defn save-team-strength
  []
  ;; Iterate through all participants and reset their to prior to the current estimate
  ;;  (doseq [akey (keys (seglob/get-player-performance-histories))]
  ;;    (let [strength (current-participant-strength akey)]
  ;;      (if (dplev :all) (println "strength=" strength "akey=" akey "*participant-strength*=" (seglob/get-participant-strength))
  ;;          (seglob/set-participant-strength (keyword akey) strength))))
  ;; Save to the participant strength file
  (if (dplev :io :all) (println "saving participant-strength-file-to: " (seglob/get-participant-strength-file)))
  (let [dir (fs/parent (seglob/get-participant-strength-file))]
    (when (not (fs/exists? dir))
      (fs/mkdir dir)
      (if (dplev :all) (println "CWD" fs/*cwd*))
      (if (dplev :all) (println "Created runtime dir" (fs/absolute dir))))
    (spit (seglob/get-participant-strength-file)
          (pr-str (deatom-participant-strength-data (seglob/get-player-strength-data))))))



;;; Fin
