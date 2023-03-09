;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.interventionengine
  "Generation of interventions."
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
            ;;[rita.state-estimation.volumes :as vol :refer :all]
            ;[rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            [rita.state-estimation.ras :as ras]
            [rita.state-estimation.teamstrength :as ts]
            ;;[rita.state-estimation.rlbotif :as rlbotif]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.multhyp :as mphyp]
            ;[rita.state-estimation.rita-se-core :as rsc :refer :all] ; back off from refer all +++
            [rita.state-estimation.cognitiveload :as cogload]
            ;; [rita.generative-planner.generative-planner :as amg :refer :all]
            ;; [rita.generative-planner.desirable-properties :as dp :refer :all]
            [rita.state-estimation.ritamessages :as ritamsg]
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

#_(in-ns 'rita.state-estimation.interventionengine)

;;; Parameters

(def ^:dynamic *intervention-parameters*
  {:minimum-time-between-interventions (* 120 1000), ;ideally not more than one intervention per person in 2 minutes (8 interventions max)
   })


(defn convert-role-pid
  [ivrole]
  (case ivrole
    :tran (seglob/get-participant-id-from-call-sign "Green")
    :med  (seglob/get-participant-id-from-call-sign "Red")
    :eng  (seglob/get-participant-id-from-call-sign "Blue")
    "Green" (seglob/get-participant-id-from-call-sign "Green") ; these shouldn't be needed or possible +++ DEBUG ME
    "Red"   (seglob/get-participant-id-from-call-sign "Red")
    "Blue"  (seglob/get-participant-id-from-call-sign "Blue")
    (do
      (println "ERROR: Unhandled ivrole" ivrole "in convert-role-pid convert-role-pid")
      (seglob/get-participant-id-from-call-sign "Red"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Leadership

;;;+++ push these into secoredata

;;; Wordcounts from ASR
(def wordcount-leadership-scores [])
(def utterancecount-leadership-scores [])


(defn vote-leader
  [source pid leader confidence em]
  (let [epoch (ras/compute-epoch-from-ms em)
        knowledge (seglob/get-leadership-votes)
        other-evidence-in-epoch (get knowledge [source epoch pid])]
    (println source "voted pid=" pid "leader-value=" leader "confidence=" confidence "in epoch=" epoch)
    (seglob/set-leadership-votes
     (merge knowledge
            {[source epoch pid] (conj other-evidence-in-epoch [leader confidence])}))))

(defn considered-leader
  [x]
  (let [pid (convert-role-pid x)
        wcls (first (first wordcount-leadership-scores))
        ucls (first (first utterancecount-leadership-scores))
        leader (or
                (and (empty? wordcount-leadership-scores) (= x :direct))
                (and (empty? utterancecount-leadership-scores) (= x :direct))
                (= pid wcls)
                (= pid ucls))]
    (when (not leader)
      (when (dplev :all)
        (println "In considered-leader:" pid "was not found to be the leader")
        (pprint {:pid pid, :wcls wcls, :ucls ucls :leader leader})
        (pprint wordcount-leadership-scores)
        (pprint utterancecount-leadership-scores)))
    (or leader :med)))

(defn get-leadership-score
  [person]
  ;; (println "wordcount-leadership-scores=" wordcount-leadership-scores)
  ;; (println "utterancecount-leadership-scores=" utterancecount-leadership-scores)
  (let [pid (convert-role-pid person)
        rawscore (+ (first  (get (into {} wordcount-leadership-scores) pid [0 0]))
                    (second (get (into {} utterancecount-leadership-scores) pid [0 0])))]
    ;; (println "get-leadership-score rawscore=" rawscore)
    (* rawscore 10)))                     ; +++ Replace coefficient with a reasoned value

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Intervention Selection

;;; Variables used by interventions that are bound at each cycle.

(def ^:dynamic ELAPSED-MS 0)                      ; Current elapsed milliseconds
(def ^:dynamic EPOCH      0)                      ; Epoch 0 is the planning period (2 minutes) remaining 5 epochs are 3 minutes each
(def ^:dynamic FBO      nil)                      ; bound to the selected recipient according to the rules
(def ^:dynamic VIA      nil)                      ; bound to the selected intermediary according to the rules
(def ^:dynamic HAS-SEEN nil)                      ; True if the recipient (VIA) has already seen the intervention
(def ^:dynamic IID      nil)                      ; ID of the intervention in question.
(def ^:dynamic GUARDRES nil)                      ; The result returned from the guard function, can be used to pass information from the guard to the body, such as an event.
(def ^:dynamic TRIAL      0)

(defn new-event-id
  []
  (let [idnum (seglob/get-event-id-number)
        idkeyword (keyword (str "event-" idnum))]
    (seglob/set-event-id-number (+ idnum 1))
    idkeyword))

(defn register-event
  [event]
  (seglob/set-event-log-events (conj (seglob/get-event-log-events) (merge event {:event-id (new-event-id)})))
  nil)

(defn register-event-as-handled
  [event em]                            ; Which event was handled and when
  (let [eventid (:event-id event)]
    (seglob/set-events-handled (conj (seglob/get-events-handled) {eventid em}))
    nil))

;;; return a list of unhandled events
(defn find-recent-unhandled-events
  [eventkey recency-ms em]
  (let [candidates (filter #(and (< (- em (:em %)) recency-ms)                          ; Within the specified recency-ms
                                 (= eventkey (:event-key %))                            ; The correct type of event
                                 (not (get (seglob/get-events-handled) (:event-id %)))) ; Not already handled
                           (seglob/get-event-log-events))
        ;;_ (if (not (empty? candidates)) (println "In find-recent-unhandled-events: candidates=" (into () candidates)))
        order-of-recency (into () (sort #(> (:em %1) (:em %2)) candidates))]
    #_(if (and (= eventkey :suboptimal-evacuation) (not (empty? candidates)))
      (println "In find-recent-unhandled-events: order-of-recency=" (into () order-of-recency)))
    (if (empty? order-of-recency) () order-of-recency)))


;;; Return a list of possibilities given the rules

(defn get-fbo-candidates
  [guidance]
  (let [fbo-candidates
        (into []
              (filter #(case guidance
                         :any true
                         :tran (= % :tran)
                         :med (= % :med)
                         :eng (= % :eng)
                         :me (or (= % :med) (= % :eng))
                         :mt (or (= % :med) (= % :tran))
                         :et (or (= % :eng) (= % :tran))
                         :em (or (= % :med) (= % :eng))
                         :tm (or (= % :med) (= % :tran))
                         :te (or (= % :eng) (= % :tran))
                         [])
                      [:med :eng :tran]))]
    ;; (println "fbo candidates given guidance of" guidance " are " fbo-candidates)
    fbo-candidates))

(defn get-via-candidates-given
  [fbo guidance]
  (let [via-candidates
        (into []
              (filter #(case guidance
                         :direct (= % fbo)
                         :lead   (and (not= % fbo) (considered-leader %))
                         :tran   (and (not= % fbo) (= % :tran))
                         :med    (and (not= % fbo) (= % :med))
                         :eng    (and (not= % fbo) (= % :eng))
                         :me     (and (not= % fbo) (or (= % :med) (= % :eng)))
                         :mt     (and (not= % fbo) (or (= % :med) (= % :tran)))
                         :et     (and (not= % fbo) (or (= % :eng) (= % :tran)))
                         :em     (and (not= % fbo) (or (= % :med) (= % :eng)))
                         :tm     (and (not= % fbo) (or (= % :med) (= % :tran)))
                         :te     (and (not= % fbo) (or (= % :eng) (= % :tran))))
                      [:med :eng :tran]))]
    ;;(when (empty? via-candidates) (println "****** failed to find any candidates for fbo=" fbo "guidance=" guidance "*******"))
    ;; (println "via candidates given fbo" fbo "and guidance of" guidance " are " via-candidates)
    (if (empty? via-candidates) (list fbo) via-candidates)))

(def ^:dynamic *has-seen* {}) ; Needs to go somewhere that will survive a restart +++

(defn reset-has-seen
  []
  (def ^:dynamic *has-seen* {}))

(defn has-seen?
  [who what]
  (some #{what} (get *has-seen* (seglob/strfromsymbol who) #{})))

(defn seen-by
  [whom what]
  (if (not (some #{what} (get *has-seen* (seglob/strfromsymbol whom))))
    (def ^:dynamic *has-seen* (merge *has-seen* {(seglob/strfromsymbol whom) (conj (get *has-seen* (seglob/strfromsymbol whom) #{}) what)})))
  (println "*has-seen* =" *has-seen*))

(defn epoch-in-range
  [epoch [from to]]
  (and (>= epoch from) (<= epoch to)))  ; Note that the epochs ar einclusive [3 5] means epochs 3, 4, and 5.

(defn ok-for-this-trial?
  [constraint]
  (let [tn (seglob/get-trial-number)]
    (when (and (dplev :all :error) (or (not constraint) (not (number? tn)) (< tn 1) (> tn 2)))
      (println "ERROR: in ok-for-this-trial? with tn=" tn))
    (or (not constraint)                ; No constraint means unconstrained
        (case constraint
          :both     true
          :any      true
          :first    (== tn 1)
          :second   (== tn 2)
                    false))))

(defn update-leadership-values ; ++++ Move
  [em]
  (let [current-epoch (ras/compute-epoch-from-ms em)
        trial (seglob/get-trial-number)
        team-members (seglob/get-team-members)
        data-by-prior-epochs (into {}
                                  (map (fn [tm]
                                         {tm (map (fn [epoch]
                                                (let [db (seglob/get-player-strength-data)]
                                                  [(ts/get-ps-value db tm trial epoch :spoken-words)
                                                   (ts/get-ps-value db tm trial epoch :spoken-utterances)]))
                                              (range 0 current-epoch))})
                                       team-members))
        ;; _ (if (> current-epoch 0) (println "Leadership raw values"))
        ;; _ (if (> current-epoch 0) (pprint data-by-prior-epochs))
        ;; Just do the most recent epoch for now decide what to do with te others later.
        most-recent-data (into {} (map (fn [[person data]] {person (last data)}) data-by-prior-epochs))
        ;; _ (if (> current-epoch 0) (println "most recent data only"))
        ;; _ (if (> current-epoch 0) (pprint most-recent-data))
        total-words (reduce + (map #(if (empty? %) 0 (first %)) (vals most-recent-data)))
        total-utterances (reduce + (map #(if (empty? %) 0 (second %)) (vals most-recent-data)))
        ;; _ (if (> current-epoch 0) (println "total-words=" total-words))
        ;; _ (if (> current-epoch 0) (println "total-utterances=" total-utterances))
        _ (seglob/debug-save most-recent-data)
        scaled-data (into [] (map (fn [[person data]]
                                    (if (or (== total-utterances 0)
                                            (== total-words 0))
                                      [person [0 0]]
                                      (if (empty? data)
                                        [person [0 0]]
                                        [person [(/ (first data) total-words)
                                                 (/ (second data) total-utterances)]])))
                                  most-recent-data))
        ;; _ (if (> current-epoch 0) (println "Scaled data"))
        ;; _ (if (> current-epoch 0) (pprint scaled-data))
        sorted-words-data (sort (fn [x y] (> (first (second x))) (first (second y))) scaled-data)
        ;; _ (if (> current-epoch 0) (println "sorted-words-data"))
        ;; _ (if (> current-epoch 0) (pprint sorted-words-data))
        sorted-utterances-data (sort (fn [x y] (> (second (second x))) (second (second y))) scaled-data)
        ;; _ (if (> current-epoch 0) (println "sorted-utterances-data"))
        ;; _ (if (> current-epoch 0) (pprint sorted-utterances-data))
        ]
    #_(if (dplev :all :debug-interventions)
      (println "leadership estimates data-by-prior-epochs is as follows:")
      (pprint data-by-prior-epochs))
    (def wordcount-leadership-scores sorted-words-data)
    (def utterancecount-leadership-scores sorted-utterances-data)
    [sorted-words-data sorted-utterances-data]))

(defn convert-role-to-intervention-form
  [role]
  (let [internal-role-name (get {"Medical_Specialist" :med,
                                 "Engineering_Specialist" :eng,
                                 "Transport_Specialist" :tran} role)]
     (cond
        (nil? internal-role-name)
        (do
          (if (dplev :error :all)
             (println "ERROR: Failed to find internal name for the role" role "defaulting to :med"))
          :med)

        :otherwise
        internal-role-name)))

(defn convert-role-callsign
  [ivrole]
  (case ivrole
    :tran "Green"
    :med  "Red"
    :eng  "Blue"
    (do
      (println "ERROR: Unhandled ivrole" ivrole "in convert-role-callsign")
      "Red")))

;;; Return a list of tripples that are possible, without considering parameters.
(defn get-list-of-candidates
  [int-table em]
  (let [current-epoch (ras/compute-epoch-from-ms em)
        _ (update-leadership-values em)
        trial (seglob/get-trial-number)
        raw-candidates
        (into ()
              (apply
               concat
               (into ()
                     (map (fn [ivs]           ; Map over the entries in the IVS table.
                            (if (and
                                 (ok-for-this-trial? (get ivs :trial :both))
                                 (epoch-in-range current-epoch (get ivs :epochs [0 5])))
                              (let [fbo-cands
                                    (apply concat
                                           (map (fn [fbo] ; Map over possible targets of the interventions
                                                  (let [via-cands
                                                        (map (fn [via] ; Map over the possible delovery mechanisms
                                                               (binding [VIA (convert-role-pid via)
                                                                         FBO (convert-role-callsign fbo)
                                                                         ELAPSED-MS em
                                                                         IID (get ivs :ID)
                                                                         EPOCH current-epoch
                                                                         TRIAL trial
                                                                         HAS-SEEN (has-seen? (convert-role-pid via) (get ivs :ID))]
                                                                 (let [guardres ((get ivs :guard))]
                                                                   (when (dplev :all)
                                                                     (println "Guard for" (:ID ivs) "result=" guardres))
                                                                   (if guardres [ivs via fbo guardres])))) ; Collect the ones whose guards returned True.
                                                             (get-via-candidates-given fbo (get ivs :via)))]
                                                    (remove empty? via-cands)))
                                                (get-fbo-candidates (get ivs :fbo))))]
                                (remove empty? fbo-cands))
                              ()))      ; Ignore epochs that our out of range.
                          int-table))))]
    #_(when (not (empty? raw-candidates))
      (println "Here are the raw intervention candidates:")
      (pprint raw-candidates))
    raw-candidates))


(defn last-time-addressed
  [person]
  (get (seglob/get-last-time-addressed) person (- (* 120 1000)))) ;+++ constant

(defn record-last-time-addressed
  [people em]
  (let [newly-addressed (into {} (map (fn [x] {x em}) people))]
    (seglob/set-last-time-addressed (merge (seglob/get-last-time-addressed) newly-addressed))))

(defn time-since-last-addressed
  [person em]
  (- em (get (seglob/get-last-time-addressed) person)))

;;; For now, consider only the time since the last intervention of the fbo and the via.  Consider adding value to the table
;;; and making the value consider time since last of its kind (supportive, relationship building,corrective)

;; 1. Prefer fbo and via not be the same person
;; 2. Prefer where the vip hasn't heard from un in a long time
;; 3. Rank leaders on the currently perceived leadership skills.
;; 4. What else?  What formula for the recency rule?

(defn evaluate-intervention
  [[ivs via fbo] em]
  (let [lta-via (- em (last-time-addressed via))
        lta-fbo (- em (last-time-addressed fbo))
        leadership (get-leadership-score via)
        importance (get ivs :value 0)
        score (cond (< lta-via (get *intervention-parameters* :minimum-time-between-interventions)) 0
                    (< lta-fbo (get *intervention-parameters* :minimum-time-between-interventions)) 0
                    :otherwise (+ lta-via lta-fbo leadership importance))]
    (when (== score 0)
      (when (dplev :all)
        (println "score=0 because, lta-via=" lta-via "lta-fbo=" lta-fbo "leadership=" leadership "importance=" importance "em=" em)))
    score))

;;; Score the candidates, discqrding any that have zero value.
(defn compute-score-of-candidates
  [interventions em]
  (let [scored (map #(let [score (evaluate-intervention % em)] [score %]) interventions)
        positive-value (filter #(> (first %) 0) scored)]
    positive-value))

(defn select-interventions              ; +++ write this.  take first, then exclude others that refer to the same vie or fbo, repeat
  [sorted-list]
  ;; (map second sorted-list) ; Select all of them
  (if (not (empty? sorted-list))
    (remove nil? [(second (first sorted-list))]) ; Select first of them for now
    []))                                      ; Do nothing

(defn consider-interventions
  [pub id pid int-table int-expls em]
  (if (< em 2000)
    pub
    (let [trial (seglob/get-trial-number)
          interventions (get-list-of-candidates int-table em)           ; list of triples [ivs via fbo]
          rated (compute-score-of-candidates interventions em) ; makes a sequence of [score intervention]
          sorted (sort #(> (first %1) (first %2)) rated)
          selected (select-interventions sorted)]
      (when (dplev :all)
        (if (not (empty? interventions)) (println "Candidate interventions are" interventions))
        (if (not (empty? selected)) (println "Selected intervention candidates are" selected)))
      ;; Now do them
      (doseq [anintervention selected]
        (let [[ivs via fbo guardres] anintervention
              intervention-function (get ivs :fun)]
          (binding [VIA (convert-role-pid via)
                    FBO (convert-role-callsign fbo)
                    ELAPSED-MS em
                    IID (get ivs :ID)
                    TRIAL trial
                    EPOCH (ras/compute-epoch-from-ms em)
                    GUARDRES guardres
                    HAS-SEEN (has-seen? (convert-role-pid via) (get ivs :ID))]
            (println "intervention-function=" intervention-function "VIA=" VIA "FBO=" FBO "IID=" IID "HAS-SEEN=" HAS-SEEN)
            (cond (not intervention-function)
                  (do
                    (when (dplev :error :all :warn)
                      (println "A missing intervention function has been encountered ivs=" ivs)))
                  :otherwise
                  (do
                    ;; record the fact of having run this intervention
                    (seen-by VIA (get ivs :ID))           ; Register use of this case
                    (record-last-time-addressed [via fbo] em) ; remember who we addressed and when
                    (intervention-function FBO VIA em (get int-expls IID))
                    (seglob/inc-interventions-given))))))
      pub)))

;;; Fin
