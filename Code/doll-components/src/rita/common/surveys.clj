;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.common.surveys
  "ASIST Survey."
  (:require
            [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.string :as string]
            [clojure.pprint :as pp :refer [pprint]]
            [clojure.java.io :as io]
            )
  (:gen-class))                                             ;; required for uberjar

;;NOTE: The use of the CSV file stored in the resouce/public directory is obsolete
;;  We are currently using the CSV file(s) stored in the data directory (e.g., study-3_spiral-2_pilot) which is
;;  initialized by experiment-control/cli
(def survey-metadata-file
    "The filename of the Survey metadata file, which describes the current survey data"
  ;;In RITA, resources are found in Code/resources
  (atom (io/resource (str "public/"
                          ;;"HSRData-survey-metadata.edn"
                          "pilot-survey-metadata.edn"
                          ))))

(def survey-metadata (atom (read-string (slurp @survey-metadata-file))))

(defn set-survey-metadata-file [metadata-file]
  (reset! survey-metadata-file metadata-file)
  (reset! survey-metadata (read-string (slurp metadata-file))))

;; (def survey-csv-file
;;   "The filename of the Survey CSV file."
;;   ;;In RITA, resources are found in Code/resources
;;   ;; (io/resource (str "public/" "ASIST_data_study_id_000001_surveys.csv"))
;;   (io/resource (str "public/"
;;                     ;; "SurveysNumeric_CondBtwn-na_CondWin-na_Trial-na_Team-na_Member-na_Vers-1.csv"
;;                     "HSRData_SurveysNumeric_CondBtwn-na_CondWin-na_Trial-na_Team-na_Member-na_Vers-1.csv")))

(def survey-data (atom ()))

(defn text-to-numeric-score [raw-score]
  (if (number? raw-score)
    raw-score
    (let [paren-number-string (re-find #"\(\d+\)$" raw-score)
          number-string (if paren-number-string
                          (re-find #"\d+" paren-number-string))]
      (if number-string
        (Integer/parseInt number-string)))))

(defn normalize-data-value [value]
  (let [value (string/trim value)]
    (cond
      ;;A number
      (re-matches #"\d+" value) (Integer/parseInt value)
      ;;A vector of numbers
      (re-matches #"[\d,]+" value) (vec (map #(Integer/parseInt %)
                                             (string/split value #",")))
      :else (let [numeric-value (text-to-numeric-score value)]
              (or numeric-value
                  value)))))
;; (map surveys/normalize-data-value '("9" "[1,2,3]" "Strongly disagree\n(1)" "Foo"))
;; (9 "[1,2,3]" 1 "Foo")

(defn trim-csv-data [csv-data]
  (map (fn [csv-row]
         (map normalize-data-value csv-row))
       csv-data))

(defn first-word [header]
  (let [space-pos (string/index-of header " ")]
    (if space-pos
      (subs header 0 space-pos)
      header)))

(def use-importid-as-question-id? false)

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (if use-importid-as-question-id? ;One of 2 conventions for the column header
              (map #(get (json/read-str %) "ImportId") (nth csv-data 2))
              (first csv-data))
            (map #(keyword (string/trim (string/upper-case (first-word %)))))
            repeat)
       (trim-csv-data
        (rest (rest ;;skip the psuedo-header rows (rows 2 & 3)
               (rest csv-data))))))

(defn normalized-subject-id
  "Coerce the subject ID to a standard format (since the underlying data has been changing)"
  [subject-id]
  (cond
    ;;For the upcoming HSR data (9/2020), we expect this to be in the form "P000021"
    ;;For the latest Pilot data (8/2020), we expect this to be in the form "000021"
    (number? subject-id) (format "%06d" subject-id)
    (string? subject-id) subject-id
    :else (str subject-id)))

(defn load-data [data-file]
  (assert data-file "The Survey CSV file is not found in the resources directory")
  (with-open [reader (io/reader data-file)]
    (doall
     (->>
      (csv/read-csv reader)
      csv-data->maps
      (map (fn [csv-record]
             (assoc csv-record
                    :subject (normalized-subject-id
                              (get csv-record (get @survey-metadata :subject-column))))))
      ))))

(defn load-the-survey-data []
  (let [csv-file (io/resource
                  (get @survey-metadata :csv-file))]
    (reset! survey-data (load-data csv-file))
    nil))

(defn survey-data-list []
  (if (empty? @survey-data)
    (load-the-survey-data))
  @survey-data)

(defn survey-data-for-subject
  "Returns the survey data, as a map, for the specified subject-id (string)"
  [subject-id]
  (some #(when (= (:subject %) subject-id) %)
        (survey-data-list)))

(defn all-subject-ids
  "Returns the list of all of the subject-id strings in the survey data file"
  []
  (filter #(not (= "" %))
          (map :subject (survey-data-list))))
;; For the 6/25/2020 Data:
;; rita.common.surveys> (all-subject-ids)
;; ("subject_id_000005"
;;  "subject_id_000006"
;;  "subject_id_000007"
;;  "subject_id_000008"
;;  "subject_id_000009"
;;  "subject_id_000010"
;;  "subject_id_000011"
;;  "subject_id_000012"
;;  "subject_id_000013"
;;  "subject_id_000014"
;;  "subject_id_000015"
;;  "subject_id_000016")


(defn spatial-survey-question-ids
  "Each survey question entry is a tuple, of the question ID and the transform"
  []
  ;; :reverse assumes a scale of 1 to 7
  (get @survey-metadata :spatial-survey-question-ids))


(defn spatial-survey-aggregate-score
  "This returns the cummulative score of all of the Spatial Survey questions.
  The LOWER the score, the better the subject's spatial abilities"
  [subject-id]
  (let [subject (survey-data-for-subject subject-id)
        lowest-per-question-score 1
        highest-per-question-score 7
        score-to-use-for-unanswered (int (/ (+ lowest-per-question-score
                                           highest-per-question-score) 2))
        scores (map (fn [[question-id transform]]
                      (let [raw-score (get subject question-id :score-not-found)
                            _ (assert (not (= raw-score :score-not-found))
                                      (str "Score not found for subject " subject-id
                                           " question " question-id))
                            score (or (text-to-numeric-score raw-score)
                                      score-to-use-for-unanswered)]
                        (if (and (integer? score)
                                 (<= lowest-per-question-score score highest-per-question-score))
                          (case transform
                            :ignore 0
                            :identity score
                            :reverse (- (+ lowest-per-question-score highest-per-question-score)
                                        score))
                          score-to-use-for-unanswered)))
                    (spatial-survey-question-ids))]
    (reduce + scores)))

;; For the 6/25/2020 Data:
;; rita.common.surveys> (map spatial-survey-aggregate-score (all-subject-ids))
;; (52 37 85 82 71 64 50 69 64 84 61 88)


(defn satisficing-survey-question-ids
  "Each survey question entry is a tuple, of the question ID and the transform"
  []
  ;; :reverse assumes a scale of 1 to 6
  (get @survey-metadata :satisficing-survey-question-ids))


(defn satisficing-survey-aggregate-score
  "This returns the cummulative score of all of the Satisficing Survey questions.
  The LOWER the score, the better the subject's satisficing abilities"
  [subject-id]
  (let [subject (survey-data-for-subject subject-id)
        lowest-per-question-score 1
        highest-per-question-score 6
        score-to-use-for-unanswered (int (/ (+ lowest-per-question-score
                                               highest-per-question-score) 2))
        scores (map (fn [[question-id transform]]
                      (let [score (get subject question-id)]
                        (if (and (integer? score)
                                 (<= lowest-per-question-score score highest-per-question-score))
                          (case transform
                            :ignore 0
                            :identity score
                            :reverse (- (+ lowest-per-question-score highest-per-question-score)
                                        score))
                          score-to-use-for-unanswered)))
                    (satisficing-survey-question-ids))]
    (reduce + scores)))

;; For the 6/25/2020 Data:
;; rita.common.surveys> (map satisficing-survey-aggregate-score (all-subject-ids))
;; (20 57 54 60 49 45 46 50 54 44 52 51)
