(ns restocsv.cli
  "results to CSV"
  (:require [clojure.tools.cli :as cli]
            [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.string :as string]
            [clojure.repl :refer [pst]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.pprint :as pp :refer [pprint]]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [avenir.utils :as au :refer [as-boolean]]
            [clojure.pprint :as pp :refer [pprint]]
            [clojure.data.xml :as xml]
            [environ.core :refer [env]]
            )
  (:gen-class))

(def repl true)

(def cli-options [["-i" "--input fn"    "input file"                           :default nil]
                  ["-d" "--dir dir"     "directory containing result files"    :default nil]
                  ["-o" "--output file" "output"                               :default nil]
                  ["-a" "--alldata"]
                  ["-m" "--moments"]
                  ["-?" "--help"]]
                  )

(defn testaction
  ""
  [& args]
  (.write *out* (format "%ntest %n" args)))

(defn generatecsv
  ""
  [& args]
  (.write *out* (format "%nGenerate the results as a csv file: %s%n" args)))

(def #^{:added "0.1.0"}
  actions
  "Valid restocsv command line actions"
  {"generate" (generatecsv)
   ;;"test" (testaction)
   })

(defn usage
  "Print restocsv command line help."
  {:added "0.1.0"}
  [options-summary]
  (->> (for [a (sort (keys actions))]
         (str "  " a "\t" (:doc (meta (get actions a)))))
    (concat [""
             "restocsv"
             ""
             "Usage: restocsv [options] action"
             ""
             "Options:"
             options-summary
             ""
             "Actions:"])
    (string/join \newline)))

(defn change-extn
  [astring newext]
  (let [index (string/last-index-of astring ".")]
    (if index (str (subs astring 0 index) "." newext) (str astring "." newext))))

(defn strip-extn
  [astring]
  (let [index (string/last-index-of astring ".")]
    (if index (subs astring 0 index) astring)))

(def selectcategories ["StateEstimation--enter-room"
                       "StateEstimation--exit-room"
                       "StateEstimation--next-room-to-visit"
                       ;;"StateEstimation--goto-room"
                       ;;"StateEstimation--open"
                       "StateEstimation--triage-victim"])

(def headingnames {"StateEstimation--enter-room" "enter room",
                   "StateEstimation--exit-room" "exit room",
                   "StateEstimation--next-room-to-visit", "next room",
                   ;;"StateEstimation--goto-room" "goto room"
                   ;;"StateEstimation--open" "open"
                   "StateEstimation--triage-victim" "triage"})

(defn mean
  [coll]
  (let [sum (apply + coll)
        count (count coll)]
    (if (pos? count) (float (/ sum count)) 0.0)))

(defn standard-deviation
  [coll]
  (let [avg (mean coll)
        squares (for [x coll] (let [x-avg (- x avg)] (* x-avg x-avg)))
        total (count coll)]
    (if (>= total 2)
      (-> (/ (apply + squares) (- total 1))
          (Math/sqrt))
      1)))                              ; not enough data to do better, only one sample!

(defn moments
  [data]
  [(mean data) (standard-deviation data)])

(defn select-data
  [collected]
  (let [selected (into {} (map (fn [selkey]
                                 (let [data (get collected selkey)
                                       {counts "counts"
                                        probability "probability"
                                        totals "totals"} data]
                                   [selkey (get probability "true")]))
                               selectcategories))]
    selected))

(defn extract-data
  [trial-map]
  ;; (println "trialmap=" trial-map)
  (let [pstats (get trial-map "prediction-stats")
        detail (get pstats "mission-stats-detail")
        extracted  (if detail (select-data detail))]
    extracted))

(defn extract-results
  [batch]
  ;; (println "processing batch run" runid)
  (let [extracted (into {} (remove nil?
                                   (map (fn [[runid trialmap]]
                                          (println "Processing run:" runid)
                                          (let [rundata (into {} (remove nil? (map (fn [[trialid datamap]]
                                                      (let [extdata (extract-data datamap)]
                                                        (if extdata [trialid extdata])))
                                                                          trialmap)))]
                                            (if (not (empty? rundata))
                                              [runid rundata]
                                              (println "No data found for run" runid))))
                                        batch)))]
    extracted))

(defn generate-uninterpreted-headings
  []
  (conj (into ["Run" "Trial"] (map #(get headingnames %) selectcategories)) "Mean"))

(defn generate-uninterpreted-data
  [data]
  (into [] (map (fn [[runid runmap]]
                  (map
                   (fn [[trialid trialmap]]
                     (let [catvals (map (fn [item] (get trialmap item)) selectcategories)
                           catvalsminusnils (remove nil? catvals)
                           meanval (if (not (empty? catvalsminusnils))
                                     (/ (apply + catvalsminusnils) (count catvalsminusnils)))]
                       catvals (conj (into [runid trialid] catvals) meanval)))
                   runmap))
            data)))

(defn generate-uninterpreted-csv
  [data]
  (let [headings (generate-uninterpreted-headings)
        data (generate-uninterpreted-data data)]
    ;; (println "headings=" headings)
    ;; (println "data=" data)
    (apply concat [headings] data)))

(defn generate-moments-headings
  []
  (into ["Run"] (apply concat (map (fn [x] [(get headingnames x) ""]) selectcategories))))

(defn transpose
  [data]
  (let [rowwidth (count (first data))
        columns (map (fn [pos]
                       (map (fn [datum] (nth datum pos))
                            data))
                     (range rowwidth))]
    columns))

(defn generate-moments-by-expt
  [data]
  (into [] (map (fn [[runid runmap]]
                  (into [runid]
                        (let [rowdata (map
                                       (fn [[trialid trialmap]]
                                         (into [] (map (fn [item] (get trialmap item)) selectcategories)))
                                       runmap)
                              columndata (transpose rowdata)
                              themoment (into [] (map
                                                  (fn [pos]
                                                    (let [column (remove nil? (nth columndata pos))]
                                                      (moments column)))
                                                  (range (count selectcategories))))]
                          (apply concat themoment))))
            data)))

(defn generate-moments-by-run
  [data]
  (let [headings (generate-moments-headings)
        ;;_ (println "headings=" headings)
        data (generate-moments-by-expt data)]
    (into [headings] data)))

(defn results-csv-generate
  "csv results generator"
  [& args]
  #_(printf "In results-csv-generate args= %s%n" args)
  (let [parsed (cli/parse-opts args cli-options)
        help (get-in parsed [:options :help])
        alld (get-in parsed [:options :alldata])
        mome (get-in parsed [:options :moments])
        inpu (get-in parsed [:options :input])
        dire (get-in parsed [:options :dir])
        outp (get-in parsed [:options :output])
        _ (do
            (def repl false)
            (when help
              (println (usage (:summary parsed)))
              (when-not repl
                (System/exit 0))))

        ;;_ (println "Results CSV Generator: " (:options parsed))
        ]

    (if (.exists (io/file inpu))
      (let [fileasstring (slurp inpu)
            obj (json/read-str fileasstring)]
        (println "Results file loaded from" inpu)
        ;; (pprint obj)
        (let [extracted (extract-results (into [] obj))
              simplecsv (generate-uninterpreted-csv extracted)
              momentsbyrun (generate-moments-by-run extracted)
              rdat (cond alld simplecsv mome momentsbyrun)]
          ;; (pprint momentsbyrun)
          ;; (println "extracted data:")
          ;; (pprint extracted)
          ;; (println "csv-read data:")
          ;; (pprint simplecsv)
          (if rdat
            (if outp
              (do
                (spit outp (with-out-str (csv/write-csv *out* rdat)))
                (println "csv data written to" outp))
              (csv/write-csv *out* simplecsv)))
          extracted))
      (do
        (println "Input file: " inpu "not found.")
        (System/exit 1))))
  nil)

(defn -main
  "restocsv"
  {:added "0.1.0"}
  [& args]
  (apply results-csv-generate args))

;;; Fin
