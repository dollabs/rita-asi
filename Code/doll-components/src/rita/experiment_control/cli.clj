;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.experiment-control.cli
  "RITA AttackModelGenerator main."
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
            [clojure.java.io :as io]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [pamela.parser :as parser]
            [pamela.utils :as putils]
            [rita.common.core :as rc :refer :all]
            [rita.common.surveys :as surveys]
            [pamela.tools.plant.interface :as pi]
            [pamela.tools.plant.connection :as pc]
            [pamela.tools.rmq-logger.log-player :as rmql]
            ;; [pamela.tools.utils.rabbitmq :as rmq] ;; TODO: deal with this redundancy!
            [clojure.java.io :as io]
            [java-time :as java-time])
  (:gen-class)) ;; required for uberjar

;; ********************************************************************************
;; README:
;; * Read (the top-section of) Documentation/Experiment-Control-Component.md
;; * This component is intended to be used in the REPL
;; * Edit the following variables for your environment:
;;    * test-data-dir
;;    * trial-database-file
;; * Load this file
;; * Call (-main)
;; * Call the functions for Steps 1 through 4 (in order).
;;    * Note that depending on the location of your test-data-dir, some of these steps may
;;      have already been done.
;; ********************************************************************************

(def current-connection-info
  "The current RMQ connection information"
  (atom nil))

(defn shutdown []
  (rmq/close-connection (:connection @current-connection-info))
  (reset! current-connection-info {}))

(def exp-ctl-cli-options (into [] (concat cli-options
                                          [["-x" "--expdir expdir" "Experiment Directory" :default false]
                                           ["-l" "--logfile logfile" "Log file to Run" :default false]])))

(def experiment-control-main-called? (atom false))

(declare publish-startup-rita-for-trial)

(defn -main [& args]
  (let [parsed (cli/parse-opts args exp-ctl-cli-options)
        opts (:options parsed)
        _ (pprint opts)
        help (:help opts)
        exchange (:exchange opts)
        exp-dir (:expdir opts)
        test-log-file (:logfile opts)
        opts-for-rmq (dissoc opts :expdir :logfile :mongo-db) ;cleanup irrelevant opts
        conn-info (rmq/make-channel exchange opts-for-rmq)
        config 'ExperimentControl
        ]

    (when (:errors parsed)
      (print-help (:summary parsed))
      (println (string/join \newline (:errors parsed)))
      (exit 0))

    (when help
      (print-help (:summary parsed))
      (exit 0))

    ;; (println "rabbitmq-keys-and-agents:")
    ;; (pprint (rabbitmq-keys-and-agents))

    (reset! current-connection-info conn-info)
    (reset! current-exchange exchange)
    (reset! current-app-id (str config))
    ;; This component currently doesn't subscribe to anything
    ;; (println "Setting up subscriptions")
    ;; (doseq [routing-key-symbol (subscriptions-for-component config)]
    ;;   (println "Subscribing to:" routing-key-symbol)
    ;;   (rmq/make-subscription (str routing-key-symbol) subscription-handler
    ;;                          (:channel conn-info) exchange))
    (reset! experiment-control-main-called? true)

    (when (and exp-dir test-log-file)
      (publish-startup-rita-for-trial exp-dir test-log-file)
      (System/exit 0))

    (println "App State")
    (println "----------")
    (clojure.pprint/pprint conn-info)
    (println "----------")))

;; ****************************************
;; Sandbox for RITA Experiment Control

(def study-number 3)

(def test-data-dir
  (case study-number
    2 "/Volumes/MyPassport4TBUSBC/ASIST-Data/study-2_2021.06-rmq"
    ;; 3 "/Volumes/MyPassport4TBUSBC/ASIST-Data/study-3_2022-rmq"
    3 "/Volumes/MyPassport4TBUSBC/ASIST-Data/study-3_2022-rmq-test"
    ))

(def study-2-test-trials
  "Assume that everthing else is in the (potential) training set."
  '(421 422
    429 430
    441 442
    453 454
    459 460
    475 476
    481 482
    495 496
    501 502
    517 518
    521 522
    529 530))

(defn test-trial? [trial-number]
  (assert (integer? trial-number))
  (some #(= trial-number %) study-2-test-trials))


(def trial-database-file
  (case study-number
    1 (str (fs/parent fs/*cwd*) "/evaluations/nov-2020/db.json")
    2 (str (fs/parent fs/*cwd*) "/evaluations/study-2/db.json")
    3 (str (fs/parent fs/*cwd*) "/evaluations/study-3/db.json")))

;; Grab the data at compile time, since it might not be there are run time
(defmacro trial-database-content []
  (if trial-database-file
    (json/read-str (slurp trial-database-file)
                   :key-fn keyword)
    {}))
(def trial-database (atom (trial-database-content)))

(def experiment-control-testing? false)

(defn decoded-filename [f]
  (let [filename (fs/base-name f true)
        decoded-map (assoc (into {} (map (fn [component]
                                    (let [[key val] (string/split component #"-" 2)]
                                      [(keyword key) val]))
                                         (string/split filename #"_")))
                           :filename filename)]
    decoded-map))

(defn get-trial-id-from-number [number-string]
  (when (and (empty? @trial-database)
             (not (nil? trial-database-file)))
    (println "Trial Database File:" trial-database-file)
    (assert (fs/exists? trial-database-file))
    (reset! trial-database (json/read-str (slurp trial-database-file)
                                          :key-fn keyword)))
  (get-in @trial-database [(keyword (str "Trial-" number-string))
                           :trial_id]))

;; Study-1 & 2
(defn trial-id-as-int [f]
  (let [trial-string (re-find #"\d+$" (get (decoded-filename f) :Trial ""))]
    (and trial-string
         (Integer/parseInt trial-string))))

(defn version-as-int [f]
  (let [version-string (re-find #"\d+$" (get (decoded-filename f) :Vers ""))]
    (and version-string
         (Integer/parseInt version-string))))

(defn latest-versions-only [list-of-files]
  (map #(last (second %))
       (sort-by #(Integer/parseInt (re-find #"\d+$" (first %)))
                (loop [log-files list-of-files
                       result {}]
                  (let [log-file (first log-files)]
                    (if (nil? log-file)
                      result
                      (recur (next log-files)
                             (let [decoded-fname (decoded-filename log-file)
                                   trial (get decoded-fname :Trial)
                                   ;;For "Training" trials, use the team number as the unique ID
                                   ;;Otherwise, use the trial number as the unique ID
                                   trial (if (= trial "Training") (get decoded-fname :Team) trial)
                                   trial-files (get result trial)]
                               (assoc result trial (sort-by version-as-int
                                                            (conj trial-files log-file)))))))))))

(def include-training-trials? false)

(defn relevant-files-in-directory [directory file-extension]
  (when (fs/directory? directory)
    (let [files-in-dir (fs/list-dir directory)
          log-files-in-dir (filter (fn [f]
                                     (let [f-base-name (fs/base-name f)]
                                       (and (re-matches (re-pattern (str ".*" file-extension)) f-base-name)
                                            (not (re-find #"Trial-na" f-base-name))
                                            (not (re-find #"FoV" f-base-name)) ;;These contain ONLY FoV messages.
                                            (not (re-find #"TrialPlanning" f-base-name))
                                            (not (re-find #"Trial-Competency" f-base-name))
                                            (or
                                             (number? (trial-id-as-int f-base-name))
                                             ;;Include the Training files
                                             (and include-training-trials?
                                                  (re-find #"Trial-Training" f-base-name))))))
                                   files-in-dir)]
      (latest-versions-only log-files-in-dir))))

(defn log-files-in-directory [directory]
  (relevant-files-in-directory directory "log"))


;; Study-2
(defn metadata-files-in-directory [directory]
  (relevant-files-in-directory directory "metadata"))


;; Study-1
(defn subject-members-in-directory
  "Returns a list of tuples: [subject-id list-of-log-files-with-that-subject],
  sorted by the subject-id."
  [directory]
  (let [log-files-in-dir (log-files-in-directory directory)]
    (sort-by #(Integer/parseInt (first %))
             (loop [log-files log-files-in-dir
                    result {}]
               (let [log-file (first log-files)]
                 (if (nil? log-file)
                   result
                   (recur (next log-files)
                          (let [decoded-fname (decoded-filename log-file)
                                subject (get decoded-fname :Member)
                                subject-files (get result subject)]
                            (assoc result subject (sort-by trial-id-as-int
                                                           (conj subject-files log-file)))))))))))

;; Study-2
(defn team-members-in-directory
  "Returns a list of tuples: [team-id list-of-log-files-with-that-team],
  sorted by the team-id."
  [directory]
  (let [log-files-in-dir (log-files-in-directory directory)]
    (sort-by #(Integer/parseInt (re-find #"\d+$" (first %)))
             (loop [log-files log-files-in-dir
                    result {}]
               (let [log-file (first log-files)]
                 (if (nil? log-file)
                   result
                   (recur (next log-files)
                          (let [decoded-fname (decoded-filename log-file)
                                team (get decoded-fname :Team)
                                team-files (get result team)]
                            (assoc result team (sort-by trial-id-as-int
                                                        (conj team-files log-file)))))))))))

;;For Study-1, the experimental "entity" is a subject (i.e., a person)
;;For Study-2, the experimental "entity" is a team (i.e., a collection of 3 persons)
(def entity-members-in-directory team-members-in-directory)

;; (def rmq-setup? (atom false))

;; (defn run-rmq-player1 [log-file]
;;   (let [host "localhost"
;;         port 5672
;;         exchange "rita"]
;;     (rmql/set-repl? true)
;;     ;;The default speedup is 10, which is what we want
;;     (when (not @rmq-setup?)
;;       (rmql/setup-rmq host port exchange "#")
;;       (reset! rmq-setup? true))
;;     (rmql/go log-file host port exchange "#" 0)
;;     ;;(rmql/-main "-e" "rita" "-s" 10 (str log-file))
;;     (while (not= rmql/events-scheduled rmql/events-count)
;;       ;;Patiently wait for the player to finish
;;       (Thread/sleep 1000))))

(def play-logfile-script (str (fs/parent fs/*cwd*) "/bin/play-logfile.sh"))

(defn run-rmq-player [log-file]
  (assert (fs/exists? log-file))
  (clojure.java.shell/sh play-logfile-script (str log-file))
  )

(def recompute-existing-rtd-files? false)
(def compute-dummy-rtd-and-lpm-files? false)

(defn compute-se-timing-data [log-file trial-id rtd-file]
  (cond
    ;;experiment-control-testing? (fs/touch rtd-file)

    (and (not recompute-existing-rtd-files?)
         (fs/exists? rtd-file))
    (println "Not recomputing the existing RTD file:" rtd-file)

    compute-dummy-rtd-and-lpm-files?
    (with-open [out-data (clojure.java.io/writer rtd-file)]
      (pprint {:this-is-a-dummy-rtd-file true} out-data))

    :else
    (do
      (fs/delete rtd-file) ;;since we'll use this file as a termination condition below
      (publish-message (:channel @current-connection-info)
                       trial-id
                       "start-timing-data-extraction"
                       {:rtd-filename rtd-file})
      (run-rmq-player log-file)
      ;;The State Estimator can fall behind, so we sleep until the RTD file is actually written
      (while (not (fs/exists? rtd-file))
        (Thread/sleep 2000))
      (Thread/sleep 10000) ;;probably not necessary, but why chance it?
      )))

;; ********************************************************************************
;; We assume the following file directory structure:
;; * If the data directory is /foo/bar/data
;; * Then the "parent" directory is considered to be /foo/bar
;; * And the timing directory of *.rtd files is /foo/bar/data-timing
;; * And the directory of all experiment definitions is /foo/bar/data-experiments
;; * And there is an "exp-NNNN" subdirectory under the *-experiments directory which contains the following:
;;    * learned-participant-model.edn
;;    * experiment-definition.edn
;;

;; Step 1 - Collect Timing Data
(defn collect-timing-data
  "Collect the Timing Data for State Estimation, pre-computing the times for each action"
  [data-dir]
  ;; Ensure that the source data directory exists
  (assert (fs/directory? data-dir))
  (when (and (not compute-dummy-rtd-and-lpm-files?)
             (not @experiment-control-main-called?))
    (-main))
  (let [timing-dir (str data-dir "-" "timing")
        _ (if (not (fs/exists? timing-dir)) (fs/mkdir timing-dir))
        log-files (log-files-in-directory data-dir)]
    (println "Collecting timing data for " (count log-files) "files...")
    (doseq [log-file (if experiment-control-testing?
                       (list (first log-files) (second log-files) #_(nth log-files 2)) ;;just do a few when testing
                       log-files)]
      (println "Collecting timing data for" (str log-file))
      (let [trial-number (:Trial (decoded-filename log-file))
            trial-id (get-trial-id-from-number trial-number)
            rtd-file (str timing-dir "/" (fs/base-name log-file true) ".rtd")]
        (println "Trial-ID:" trial-id)
        (println "RTD-File:" rtd-file)
        (compute-se-timing-data log-file trial-id rtd-file)
        (println "")
        ))))


(defn training-list-using-fixed-size [number-of-log-files train-percent]
  ;;Returns a sequence of boolean values, where each true is a training case corresponding
  ;;to the entry in the log-files sequence
  (let [training-vector (boolean-array number-of-log-files false)]
    (dotimes [i (Math/round (* number-of-log-files train-percent))]
      (loop []
        (let [random-index (Math/round (Math/floor (rand number-of-log-files)))]
          (if (aget training-vector random-index) ;already part of the training set
            (recur) ;so, we need to look for another
            (aset-boolean training-vector random-index true)))))
    (into () training-vector)))

;; For "easy" movement of these directories between machines, all of the file references in the
;; experiment-definition.edn file are relative to the parent-directory

;; Step 2 - Create the Experiment Definitions
;; This function has been superceded by create-experiment-grouped-by-entity
#_(defn create-experiment-ungrouped
  "Creates a single experiment"
  ([data-dir train-percent] (create-experiment-ungrouped data-dir train-percent true))
  ([data-dir train-percent fixed-size-test-set?]
   ;; If fixed-size-test-set? is true, then all of the experiments with the same train-percent will have the
   ;; exact same number of trials and tests.  If false, then the number of trials and tests will vary by
   ;; experiment, but will, ON AVERAGE, have (* number-of-log-files train-percent) training cases.
   ;; Ensure that the source data directory exists
   (println "***** WARNING: You probably want to use the super-duper create-experiment-grouped-by-subject function instead. *******")
   (assert (fs/directory? data-dir))
   (let [parent-dir (str (fs/parent data-dir))
         parent-dir-length (count parent-dir)
         filename-without-parent (fn [file]
                                   (subs (str file) (+ parent-dir-length 1)))
         log-files (log-files-in-directory data-dir)
         train-percent (if (float? train-percent)
                         train-percent
                         (/ train-percent 100.0))
         exp-top-dir (str data-dir "-experiments")
         _ (fs/mkdir exp-top-dir)
         existing-experiments (filter #(re-find #"exp-" (fs/base-name %))
                                      (fs/list-dir exp-top-dir))
         next-exp-number (+ 1 (count existing-experiments)) ;;assuming no gaps!
         new-exp-dir (str exp-top-dir (format "/exp-%04d" next-exp-number))]
     (assert (not (fs/directory? new-exp-dir)))
     (fs/mkdir new-exp-dir)
     ;; Create an experiment definition file
     (let [number-of-log-files (count log-files)
           all-indexes (range number-of-log-files)
           training-indexes (if fixed-size-test-set?
                              (training-list-using-fixed-size number-of-log-files train-percent)
                              (map (fn [i] (< (rand) train-percent))
                                   all-indexes))
           training-log-files (remove false? (map (fn [file include?]
                                                    (if include? file false))
                                                  log-files training-indexes))
           training-rtd-files (map #(str data-dir "-timing/" (fs/base-name % true) ".rtd")
                                   training-log-files)
           ;;Assume that the test files include ALL trials that are not in the training
           test-files (remove false? (map (fn [file include?]
                                            (if include? false file))
                                          log-files training-indexes))
           lpm-file (str new-exp-dir "/learned-participant-model.edn")
           experiment-definition {:parent-directory (str parent-dir)
                                  :training-log-files (map filename-without-parent training-log-files)
                                  :training-rtd-files (map filename-without-parent training-rtd-files)
                                  :test-files (map filename-without-parent test-files)
                                  :data-directory (filename-without-parent data-dir)
                                  :lpm-file (filename-without-parent lpm-file)
                                  :fixed-size-test-set? fixed-size-test-set?
                                  :training-percentage train-percent}]
       (with-open [out-data (clojure.java.io/writer (str new-exp-dir "/experiment-definition.edn"))]
         (pprint experiment-definition out-data))
       experiment-definition))))

(defn create-experiment-grouped-by-entity
  "Creates a single experiment"
  ([data-dir train-percent] (create-experiment-grouped-by-entity data-dir train-percent :train))
  ([data-dir train-percent test-or-train]
   ;; Note that train-percent is ignored when test-or-train = :test
   ;; Ensure that the source data directory exists
   (assert (fs/directory? data-dir))
   (let [fixed-size-test-set? true
         ;; If fixed-size-test-set? is true, then all of the experiments with the same train-percent will have the
         ;; exact same number of entities in the trials and tests.  If false, then the number of trials and tests will vary by
         ;; experiment, but will, ON AVERAGE, have (* number-of-log-files train-percent) training cases.
         parent-dir (str (fs/parent data-dir))
         parent-dir-length (count parent-dir)
         filename-without-parent (fn [file]
                                   (subs (str file) (+ parent-dir-length 1)))
         ;;log-files (log-files-in-directory data-dir)
         entity-grouped-log-files (case test-or-train
                                    ;;Don't include ANY of the test trials in these training experiments
                                    :train (filter #(let [trial-number (trial-id-as-int (first (second %)))]
                                                      (not (test-trial? trial-number)))
                                                   (entity-members-in-directory data-dir))
                                    :test (entity-members-in-directory data-dir))
         train-percent (if (float? train-percent)
                         train-percent
                         (/ train-percent 100.0))
         exp-top-dir (str data-dir "-experiments")
         _ (fs/mkdir exp-top-dir)
         existing-experiments (filter #(re-find #"exp-" (fs/base-name %))
                                      (fs/list-dir exp-top-dir))
         next-exp-number (+ 1 (count existing-experiments)) ;;assuming no gaps!
         new-exp-dir (case test-or-train
                       :train (str exp-top-dir (format "/exp-%04d" next-exp-number))
                       :test (str exp-top-dir "/evaluation-test"))]
     (when (= test-or-train :train)
       (assert (not (fs/directory? new-exp-dir))))
     (fs/mkdir new-exp-dir)
     ;; Create an experiment definition file
     (let [number-of-entities (count entity-grouped-log-files)
           all-indexes (range number-of-entities)
           training-indexes (if fixed-size-test-set?
                              (training-list-using-fixed-size number-of-entities train-percent)
                              (map (fn [i] (< (rand) train-percent))
                                   all-indexes))
           ;;List of [entity list-of-log-files]
           training-entities (case test-or-train
                               :train (remove false? (map (fn [file include?]
                                                    (if include? file false))
                                                          entity-grouped-log-files training-indexes))
                               :test (filter (fn [[team [file1]]]
                                               (not (test-trial? (trial-id-as-int file1))))
                                             entity-grouped-log-files))
           training-log-files (mapcat second training-entities)
           training-rtd-files (map #(str data-dir "-timing/" (fs/base-name % true) ".rtd")
                                   training-log-files)
           ;;For :train, assume that the test files include ALL trials that are not in the training
           ;;List of [entity list-of-log-files]
           test-entities (case test-or-train
                           :train (remove false? (map (fn [file include?]
                                                        (if include? false file))
                                                      entity-grouped-log-files training-indexes))
                           :test (filter (fn [[team [file1]]]
                                           (test-trial? (trial-id-as-int file1)))
                                         entity-grouped-log-files))
           test-files (mapcat second test-entities)
           lpm-file (str new-exp-dir "/learned-participant-model.edn")
           experiment-definition (merge
                                  {:original-parent-directory (str parent-dir) ;;provide this for historical documentation
                                   :training-log-files (map filename-without-parent training-log-files)
                                   :training-rtd-files (map filename-without-parent training-rtd-files)
                                   :test-files (map filename-without-parent test-files)
                                   :test-files-grouped-by-entity (map (fn [[entity-str files]]
                                                                        {:entity entity-str
                                                                         :test-files (map filename-without-parent files)})
                                                                      test-entities)
                                   :data-directory (filename-without-parent data-dir)
                                   :lpm-file (filename-without-parent lpm-file)
                                   :test-or-train test-or-train}
                                  (if (= test-or-train :train)
                                    {:fixed-size-test-set? fixed-size-test-set?
                                     :training-percentage train-percent}
                                    {}))]
       (with-open [out-data (clojure.java.io/writer (str new-exp-dir "/experiment-definition.edn"))]
         (pprint experiment-definition out-data))
       experiment-definition))))

;; To create 20 experiment definitions...
;; (dotimes [i 20] (create-experiment-grouped-by-entity test-data-dir 75))

(def recompute-existing-lpm-files? true)

;; Step 3 - Create the Learned Participant Models
(defn create-learned-participant-models
  "Create the Learned Participant Models for every defined experiment"
  [data-dir]
  ;; Note that data-dir is something like "/Volumes/projects/RITA/HSR-data-mirror/study-2_2021.06-rmq" and NOT the "-timing" directory.
  ;; Ensure that the source data directory exists
  (assert (fs/directory? data-dir))
  (when (and (not compute-dummy-rtd-and-lpm-files?)
             (not @experiment-control-main-called?))
    (-main))
  (let [exp-top-dir (str data-dir "-experiments")
        _ (assert (fs/directory? exp-top-dir))
        existing-experiments (filter #(let [dir-name (fs/base-name %)]
                                        (or (re-find #"exp-" dir-name)
                                            (= "evaluation-test" dir-name)))
                                     (fs/list-dir exp-top-dir))]
    (doseq [experiment-dir existing-experiments]
      (let [experiment-definition (read-string (slurp (str experiment-dir "/experiment-definition.edn")))
            ;; parent-directory (:parent-directory experiment-definition)
            parent-directory (str (fs/parent data-dir))
            full-pathname (fn [file]
                            (str parent-directory "/" file))
            lpm-file (full-pathname (:lpm-file experiment-definition))]
        (if (and (not recompute-existing-lpm-files?)
                 (fs/exists? lpm-file))
          (println "Not recomputing the existing LPM file:" lpm-file)
          (if compute-dummy-rtd-and-lpm-files?
            (with-open [out-data (clojure.java.io/writer lpm-file)]
              (pprint {:format-version "rita-learned-object-2.0"
                       :this-is-a-dummy-lpm-file true} out-data))
            (do
              (println "Computing LPM file:" lpm-file)
              (when (fs/exists? lpm-file)
                (println "Deleting existing LPM file:" lpm-file)
                (fs/delete lpm-file))
              (publish-message (:channel @current-connection-info)
                               "no-applicable-mission-id"
                               "create-learned-participant-model"
                               {:training-rtd-files (into [] (map full-pathname
                                                                  (:training-rtd-files experiment-definition)))
                                :lpm-file lpm-file})
              ;;Patiently wait until the LPM file is created
              (while (not (fs/exists? lpm-file))
                (Thread/sleep 2000))
              (Thread/sleep 10000) ;probably not necessary, but why chance it?
              )))))))

(defn create-learned-participant-model-for-all-rtd-files
  "Create a single Learned Participant Model for all of the RTD files"
  [data-dir]
  ;; Note that data-dir is something like "/Volumes/projects/RITA/HSR-data-mirror/study-2_2021.06-rmq" and NOT the "-timing" directory.
  ;; Ensure that the source data directory exists
  (assert (fs/directory? data-dir))
  (when (and (not compute-dummy-rtd-and-lpm-files?)
             (not @experiment-control-main-called?))
    (-main))
  (let [timing-dir (str data-dir "-" "timing")
        _ (assert (fs/directory? timing-dir))
        rtd-files (filter #(= (fs/extension %) ".rtd")
                          (fs/list-dir timing-dir))
        lpm-file (str timing-dir "/learned-participant-model.edn")]
    (when (fs/exists? lpm-file)
      (println "Overwriting the existing LPM file:" lpm-file))
    (if compute-dummy-rtd-and-lpm-files?
            (with-open [out-data (clojure.java.io/writer lpm-file)]
              (pprint {:format-version "rita-learned-object-2.0"
                       :this-is-a-dummy-lpm-file true} out-data))
            (do
              (println "Computing LPM file:" lpm-file)
              (when (fs/exists? lpm-file)
                (println "Deleting existing LPM file:" lpm-file)
                (fs/delete lpm-file))
              (publish-message (:channel @current-connection-info)
                               "no-applicable-mission-id"
                               "create-learned-participant-model"
                               {:training-rtd-files (into [] (map str rtd-files))
                                :lpm-file lpm-file})
              ;;Patiently wait until the LPM file is created
              (while (not (fs/exists? lpm-file))
                (Thread/sleep 2000))
              (Thread/sleep 10000) ;probably not necessary, but why chance it?
              ))))

;; Step 4 - Run RITA (with the appropriate LPM)
;; This step will likely be rewritten/replaced using the RITA test harness that Prakash has written
(defn run-rita-experiment
  "Run a single RITA experiment, consisting of one run for each of the trials in the test-files"
  [data-dir experiment-name]
  ;;Note that experiment-name is just the base-name of the experiment directory (e.g., "exp-0001")
  ;; Ensure that the source data directory exists
  (assert (fs/directory? data-dir))
  (when (not @experiment-control-main-called?)
    (-main))
  (let [exp-top-dir (str data-dir "-experiments")
        _ (assert (fs/directory? exp-top-dir))
        experiment-dir (str exp-top-dir "/" experiment-name)
        _ (assert (fs/directory? experiment-dir))
        experiment-definition (read-string (slurp (str experiment-dir "/experiment-definition.edn")))
        ;; parent-directory (:parent-directory experiment-definition)
        parent-directory (str (fs/parent data-dir))
        full-pathname (fn [file]
                            (str parent-directory "/" file))
        test-files (map full-pathname (:test-files experiment-definition))
        lpm-file (full-pathname (:lpm-file experiment-definition))
        _ (assert (fs/exists? lpm-file))
        lpm (read-string (slurp lpm-file))]
    (doseq [test-file test-files]
      (let [trial-number (:Trial (decoded-filename test-file))
            trial-id (get-trial-id-from-number trial-number)]
        (publish-message (:channel @current-connection-info)
                         trial-id
                         "startup-rita"
                         {:learned-participant-model lpm})
        (run-rmq-player test-file)
        ;; What do we need to do to capture the results
        ))))

;; (defn publish-startup-rita-for-trial [experiment-dir test-log-file]
;;   (let [parent-directory (fs/parent (fs/parent test-log-file))
;;         full-pathname (fn [file]
;;                         (str (str parent-directory) "/" file))
;;         experiment-definition (read-string (slurp (str experiment-dir "/experiment-definition.edn")))
;;         lpm-file (full-pathname (:lpm-file experiment-definition))
;;         _ (assert (fs/exists? lpm-file))
;;         lpm (slurp lpm-file)
;;         trial-number (:Trial (decoded-filename test-log-file))
;;         trial-id (get-trial-id-from-number trial-number)]
;;     (publish-message (:channel @current-connection-info)
;;                      trial-id
;;                      "startup-rita"
;;                      {:learned-participant-model lpm})))

;; Simplified version that makes no assumption that the experiment directory and the log file
;; directories are sibling/cousin directories
(defn publish-startup-rita-for-trial [experiment-dir test-log-file]
  ;;Just in case experiment-dir ends with a "/"
  (let [experiment-dir (if (= \/ (get experiment-dir (- (count experiment-dir) 1)))
                         (subs experiment-dir 0 (- (count experiment-dir) 1))
                         experiment-dir)

        lpm-file (str experiment-dir "/learned-participant-model.edn")
        _ (assert (fs/exists? lpm-file) "LPM file not found")
        lpm (slurp lpm-file)
        trial-number (:Trial (decoded-filename test-log-file))
        trial-id (get-trial-id-from-number trial-number)]
    (publish-message (:channel @current-connection-info)
                     trial-id
                     "startup-rita"
                     {:learned-participant-model lpm})))

;; Steps 1 through 3
(defn create-completely-new-experiments []
  ;; (def compute-dummy-rtd-and-lpm-files? true)
  (collect-timing-data test-data-dir)
  (dotimes [i 20] (create-experiment-grouped-by-entity test-data-dir 75))
  (create-experiment-grouped-by-entity test-data-dir 75 :test)
  (create-learned-participant-models test-data-dir))




;; ****************************************
;; Sandbox for RITA Survey Generation from CSV file


;(def study-metadata-directory "/Volumes/MyPassport4TBUSBC/ASIST-Data/study-2_pilot-2_2021.02")
;(def study-metadata-directory "/Volumes/MyPassport4TBUSBC/ASIST-Data/study-2_2021.06")
(def study-metadata-directory "/Volumes/MyPassport4TBUSBC/ASIST-Data/study-3_spiral-3_pilot")


;(def survey-csv-file "NotHSRData_Surveys0Numeric_Trial-na_Team-na_Member-na_CondBtwn-na_CondWin-na_Vers-1.csv")
;(def survey-csv-file "NotHSRData_Surveys0Numeric_Trial-na_Team-na_Member-na_CondBtwn-na_CondWin-na_Vers-1-with-missing-rows.csv")
;(def survey-csv-file "HSRData_Surveys0Numeric_Trial-na_Team-na_Member-na_CondBtwn-na_CondWin-na_Vers-1.csv")
(def survey-csv-file "NotHSRData_Surveys1Numeric_Trial-na_Team-na_Member-na_CondBtwn-na_CondWin-na_Vers-1.csv")

(def subject-ids (atom ()))

(declare check-for-uids)

(defn init-survey-analysis []
  (reset! surveys/survey-metadata {:csv-file survey-csv-file
                                   :subject-column :UNIQUEID
                                   ;; :spatial-survey-question-ids '((:QID13_1 :identity)
                                   ;;                                (:QID13_2 :reverse)
                                   ;;                                (:QID13_3 :identity)
                                   ;;                                (:QID13_4 :identity)
                                   ;;                                (:QID13_5 :identity)
                                   ;;                                (:QID13_6 :reverse)
                                   ;;                                (:QID13_7 :identity)
                                   ;;                                (:QID13_8 :reverse)
                                   ;;                                (:QID13_9 :identity)
                                   ;;                                (:QID13_10 :reverse)
                                   ;;                                (:QID13_11 :reverse)
                                   ;;                                (:QID13_12 :reverse)
                                   ;;                                (:QID13_13 :reverse)
                                   ;;                                (:QID13_14 :identity)
                                   ;;                                (:QID13_15 :reverse))
                                   :spatial-survey-question-ids '((:SBSOD_1 :identity)
                                                                  (:SBSOD_2 :reverse)
                                                                  (:SBSOD_3 :identity)
                                                                  (:SBSOD_4 :identity)
                                                                  (:SBSOD_5 :identity)
                                                                  (:SBSOD_6 :reverse)
                                                                  (:SBSOD_7 :identity)
                                                                  (:SBSOD_8 :reverse)
                                                                  (:SBSOD_9 :identity)
                                                                  (:SBSOD_10 :reverse)
                                                                  (:SBSOD_11 :reverse)
                                                                  (:SBSOD_12 :reverse)
                                                                  (:SBSOD_13 :reverse)
                                                                  (:SBSOD_14 :identity)
                                                                  (:SBSOD_15 :reverse))
                                   ;; :satisficing-survey-question-ids ((:q7_1 :identity)
                                   ;;                                   (:q7_2 :identity)
                                   ;;                                   (:q7_3 :identity)
                                   ;;                                   (:q7_4 :identity)
                                   ;;                                   (:q7_5 :identity)
                                   ;;                                   (:q7_6 :identity)
                                   ;;                                   (:q7_7 :identity)
                                   ;;                                   (:q7_8 :identity)
                                   ;;                                   (:q7_9 :identity)
                                   ;;                                   (:q7_10 :identity))
                                   })
  (let [csv-file (str study-metadata-directory "/" survey-csv-file)
        s-data (surveys/load-data csv-file)]
    (reset! surveys/survey-data s-data))
  (reset! subject-ids (surveys/all-subject-ids))
  (check-for-uids study-metadata-directory))

(defn trial-start-message? [msg]
  (and (= (get-in msg [:header :message_type]) "trial")
       (= (get-in msg [:msg :sub_type]) "start")))

(defn trial-start-message [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (let [message-seq (line-seq rdr)
          start-message (some (fn [msg-str]
                                (let [msg (json/read-str msg-str :key-fn keyword)]
                                  (if (trial-start-message? msg) msg)))
                              message-seq)]
      start-message)))

(defn participant-id-accessor [m]
  ;; There are many different ways of identifying a player!
  (or (get m :participant_id) (get m :uniqueid) (get m :unique_id)))

(defn uids-found-in-survey [trial-start-msg not-found?]
  (let [uids (map participant-id-accessor (get-in trial-start-msg [:data :client_info]))
        found-transform (if not-found? identity not)]
    (if (empty? uids)
      :no-uniqueids
      (filter (fn [uid]
                (found-transform (some #(= uid %)
                                       @subject-ids)))
              uids))))

(defn check-for-uids
  "This is a data integrity utility that identifies any metadata files that refer to a uniqueid
  not present in the survey CSV file."
  [dir]
  (let [files (metadata-files-in-directory dir)]
    (println "The following files have a problem with the mapping between the uniqueid(s) from the metadata to survey0 file.")
    (doseq [file files]
      (let [trial-start-msg (trial-start-message file)
            uids (uids-found-in-survey trial-start-msg false)]
        ;(when (= uids :no-uniqueids) (println uids))
        (when (or (keyword? uids) ;;:no-uniqueids
                  (and (coll? uids) (not (empty? uids))))
          (println (fs/base-name file)
                   (if (keyword? uids)
                     uids
                     (map #(if (= % "") "Empty-String-UID" %)
                          uids))))))))

(defn survey-message? [msg]
  (and (= (get-in msg [:header :message_type]) "status")
       (= (get-in msg [:msg :sub_type]) "Status:SurveyResponse")))

(defn sbsod-survey-message?
  "If this is a SBSOD survey message, return the participant ID.  Otherwise, false."
  [msg]
  (and (survey-message? msg)
       (let [survey-response (json/read-str (get-in msg [:data :survey_response])
                                            :key-fn keyword)]
         (and (or (get-in survey-response [:values :QID13_1] false)
                  (get-in survey-response [:values :SBSOD_1] false))
              (participant-id-accessor (get survey-response :values))))))

(defn insert-surveys-after-trial-start [original-metadata-file survey-metadata-file output-metadata-file]
  (with-open [o (io/writer output-metadata-file)]
    (with-open [rdr-original (clojure.java.io/reader original-metadata-file)]
      (let [seq (line-seq rdr-original)]
        (loop [original-line (first seq)
               original-next (next seq)
               insertion-state :beginning]
          (when (not (nil? original-line))
            (binding [*out* o]
              (println original-line))
            (case insertion-state
              :beginning
              (let [trial-start? (trial-start-message? (json/read-str original-line :key-fn keyword))]
                (if trial-start?
                  (do
                    (io/copy (io/file survey-metadata-file) o)
                    (recur (first original-next) (next original-next) :rest-of-original))
                  (recur (first original-next) (next original-next) :beginning)))
              :rest-of-original
              (recur (first original-next) (next original-next) :rest-of-original)
              )))))))

;; Testing: Used to examine existing (Session2) survey responses
(defn first-survey-response-message [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (let [message-seq (line-seq rdr)
          survey-message (some (fn [msg-str]
                                (let [msg (json/read-str msg-str :key-fn keyword)]
                                  (if (survey-message? msg) msg)))
                              message-seq)]
      survey-message)))

(defn first-sbsod-survey-response-message [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (let [message-seq (line-seq rdr)
          survey-message (some (fn [msg-str]
                                (let [msg (json/read-str msg-str :key-fn keyword)]
                                  (if (sbsod-survey-message? msg) msg)))
                              message-seq)]
      survey-message)))

(defn check-for-sbsod-survey-messages
  "This is a data integrity utility that identifies any metadata files that contain SBSOD survey data."
  [dir]
  (let [files (metadata-files-in-directory dir)]
    (doseq [file files]
      (let [first-sbsod (first-sbsod-survey-response-message file)]
        (println (if first-sbsod (sbsod-survey-message? first-sbsod) "NotFound")
                 (fs/base-name file))))))

(defn intervention-message? [msg]
  (and (= (get-in msg [:header :message_type]) "agent")
       (= (get-in msg [:msg :sub_type]) "Intervention:Chat")))

(defn trial-stop-msg? [msg]
  (and (= (get-in msg [:header :message_type]) "trial")
       (= (get-in msg [:msg :sub_type]) "stop")))

(defn intervention-messages [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (let [seq-of-lines (line-seq rdr)]
      (loop [messages []
             current-line (first seq-of-lines)
             remaining-lines (rest seq-of-lines)]
        (let [next-msg (json/read-str current-line :key-fn keyword)]
          (cond
            (trial-stop-msg? next-msg) messages
            (intervention-message? next-msg) (recur (conj messages next-msg) (second remaining-lines) (rest remaining-lines))
            :else (recur messages (second remaining-lines) (rest remaining-lines))))))))

(defn summarize-intervention-messages [filename]
  (let [messages (intervention-messages filename)]
    (loop [message-map {}
           current-msg (first messages)
           other-msgs (rest messages)]
      (let [content (get-in current-msg [:data :content])]
        (if current-msg
          (recur (update message-map content #(if (nil? %) 1 (+ % 1)))
                 (first other-msgs)
                 (rest other-msgs))
          message-map)))))

(defn summary-of-intervention-messages
  "This is a testing utility that identifies all of the intervention messages in each metadata file"
  [dir]
  (let [files (metadata-files-in-directory dir)]
    (doseq [file files]
      (let [intervention-summary (summarize-intervention-messages file)]
        (println "")
        (println "********************")
        (println "Interventions for" (fs/base-name file) ":")
        (if (empty? intervention-summary)
          (pprint "No interventions in this trial.")
          (pprint intervention-summary))))))

;; Used for testing...
(defn message-with-missing-topic [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (let [message-seq (line-seq rdr)
          message (some (fn [msg-str]
                                (let [msg (json/read-str msg-str :key-fn keyword)]
                                  (if (not (get msg :topic))
                                    msg)))
                              message-seq)]
      message)))



(def rita-survey-msg-generator-version "0.4")

(defn survey-response-for-uid [uid]
  (let [survey-data-for-uid (surveys/survey-data-for-subject uid)
        response {}]
    (assoc response
           :responseId (:responseid survey-data-for-uid);;Note the case diffs
           :values (assoc survey-data-for-uid
                          :spatial-survey-aggregate-score (surveys/spatial-survey-aggregate-score uid))
           ;;TODO: Maybe Add :labels, :displayedFields, :displayedValues ???
           )))

(defn get-player-identifiers
  "Any and all of the existing player identifiers."
  [uid trial-start-msg]
  (let [client_info (some #(and (= uid (participant-id-accessor %)) %) (get-in trial-start-msg [:data :client_info]))]
    (select-keys client_info [:uniqueid :unique_id :playerid :participant_id :participantid :playername :callsign])))

;; (defn timeinmilliseconds
;;   [RFC3339-timestring]
;;   (let [dt (clojure.instant/read-instant-date RFC3339-timestring)
;;         mstime (java.util.Date/.getTime dt)]
;;     mstime))

;;Give the survey messages a timestamp that is immediately after the trial/start message
(defn survey-timestamp-for-nth-player [trial-start-timestamp nth-player]
  (let [trial-start-time (java-time/instant trial-start-timestamp)
        survey-time (java-time/plus trial-start-time (java-time/millis (+ nth-player 1)))]
    (java-time/format survey-time)))

(def stringify-survey-response?
  "The Testbed stringifies the JSON cargo in order to not overtax ElastiSearch"
  true)

(defn survey-response-message [uid trial-start-msg nth-player]
  (let [trial-start-timestamp (get-in trial-start-msg [:msg :timestamp])
        survey-timestamp (survey-timestamp-for-nth-player trial-start-timestamp nth-player)
        msg-value (merge
                   (select-keys (get trial-start-msg :msg) ;;Clone these keys
                                [:trial_id :experiment_id :replay_parent_id :replay_parent_type :replay_id])
                   {:timestamp survey-timestamp
                   :sub_type "Status:SurveyResponse"
                   :source "RITASurveyMsgGenerator"
                   :version rita-survey-msg-generator-version})
        ;;Verify that the UID is valid for this message (of course it should be)
        _ (assert (some #(= uid %) (map participant-id-accessor (get-in trial-start-msg [:data :client_info]))))
        survey-data-for-uid? (not (nil? (surveys/survey-data-for-subject uid)))
        survey-response-transform (if stringify-survey-response? json/write-str identity)
        player-identifiers (get-player-identifiers uid trial-start-msg)
        survey-response (merge (survey-response-for-uid uid)
                               player-identifiers
                               {:surveyname "Surveys0"})]
    (if survey-data-for-uid?
      (merge
       {:msg msg-value
        "@timestamp" survey-timestamp
        :data {:survey_response (survey-response-transform survey-response)}
        "@version" "1"
        :header {:version "0.1"
                 :timestamp survey-timestamp
                 :message_type "status"}
        :topic "status/asistdataingester/surveyresponse" ;;What's appropriate for RITA?
        }
       (select-keys trial-start-msg [:host]))
      {})))



(def prepend-surveys-to-original-metadata? false) ;old behavior
(def insert-surveys-after-trial-start? true)

(defn increment-versions-in-dir [dir]
  (let [files (metadata-files-in-directory dir)]
    (doseq [file files]
      (let [[_ front version-string end] (re-find #"(\S+Vers-)(\d+)(\S+)" (str file))
            new-filename (str front (+ 1 (Integer/parseInt version-string)) end)]
        (fs/rename file new-filename)))))

;; (write-session1-survey-metadata-files study-metadata-directory)
(defn write-session1-survey-metadata-files [dir]
  (when (empty? @surveys/survey-data)
    (init-survey-analysis))
  (let [files (metadata-files-in-directory dir)
        survey-dir (str dir "/" "session1-surveys")]
    (if (not (fs/exists? survey-dir)) (fs/mkdir survey-dir))
    (doseq [file files]
      (let [metadata-base-name (fs/base-name file)
            trial-start-msg (trial-start-message file)
            survey-metadata-file (str survey-dir "/" "Session1-Survey-" metadata-base-name)
            survey-with-original-metadata-file (str survey-dir "/" metadata-base-name)
            uids (uids-found-in-survey trial-start-msg true)]
        (when (not (= :no-uniqueids uids))
          (println survey-metadata-file
                   uids)
          (when (not (and (seq? uids)
                          (= (count uids) 3)))
            (println "Didn't find the expected 3 UIDs in" metadata-base-name "Found only" uids))
          (with-open [out-data (io/writer survey-metadata-file)]
            (dotimes [n (count uids)]
              (let [uid (nth uids n)]
                (cond
                  (= "" uid) (println "Missing UID/uniqueid in" metadata-base-name)
                  :else (do
                          (println uid)
                          (let [msg (survey-response-message uid trial-start-msg n)]
                            (json/write msg out-data :escape-slash false)
                            (binding [*out* out-data]
                              (newline))))))))
          (cond
            prepend-surveys-to-original-metadata?
            ;;Concat the files
            (with-open [o (io/writer survey-with-original-metadata-file)]
              (io/copy (io/file survey-metadata-file) o)
              (io/copy (io/file file) o))

            insert-surveys-after-trial-start?
            (insert-surveys-after-trial-start file survey-metadata-file survey-with-original-metadata-file))
          )))))
