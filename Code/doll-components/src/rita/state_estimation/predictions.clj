;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.predictions
  "predictions"
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
            [rita.state-estimation.testbed :as tb]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.interventions :as intervene]
            [rita.state-estimation.ras :as ras]
            [rita.state-estimation.spacepredicates :as spreads]
            [rita.state-estimation.victims :as victims]
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

(declare add-prediction-message)

(defn d-get-field-value
  [id field & [proc]]
  (let [result (get-field-value id field)]
    (if result
      result
      (do
        (when (dplev :all :error :warn))
          (println "ERROR: When looking up id=" id "field=" field "in function" proc)
          (println "While processing the following message:")
          (pprint (seglob/get-last-received-message))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Prediction management

(defn update-player-beep-use
  [id strategy uses-beeps secs beeps-ignored beeps-followed]
  (let []
    (when (dplev :beeps :all) (println "UPBU: BF=" beeps-followed "BI=" beeps-ignored
             "Tstrategy=" strategy "Bresponse=" uses-beeps
             "sec=" secs))
    (cond
      (and (> beeps-followed 0) (= beeps-ignored 0))
      (do (set-field-value! id 'beep-response :always)
          (if (not (= uses-beeps :always))
            (when (dplev :beeps :all)
              (println "Player beep-use changing from" uses-beeps "to :always -c0"))))

      (and (> beeps-ignored 0) (= beeps-followed 0))
      (do (set-field-value! id 'beep-response :never)
          (if (not (= uses-beeps :never))
            (when (dplev :beeps :all)
              (println "Player beep-use changing from" uses-beeps "to :never -c1"))))

      (and (> beeps-ignored 0) (> (/ beeps-ignored 3.0) beeps-followed))
      (do (set-field-value! id 'beep-response :sometimes)
          (if (not (= uses-beeps :sometimes))
            (when (dplev :beeps :all)
              (println "Player beep-use changing from" uses-beeps "to :sometimes -c2"))))

      (and (> beeps-followed 0) (> (/ beeps-followed 3.0) beeps-ignored))
      (do (set-field-value! id 'beep-response :usually)
          (if (not (= uses-beeps :usually))
            (when (dplev :beeps :all)
              (println "Player beep-use changing from" uses-beeps "to :usually -c4")))))))

(defn player-ignored-beeps
  [id]
  (let [beeps-ignored (+ (or (d-get-field-value id 'beeps-ignored "player-ignored-beeps") -1) 1)
        beeps-followed (or (d-get-field-value id 'beeps-followed "player-ignored-beeps") -1)
        uses-beeps (d-get-field-value id 'beep-response "player-ignored-beeps")
        strategy (d-get-field-value id 'triage-strategy "player-ignored-beeps")
        secs  (d-get-field-value id 'seconds-remaining "player-ignored-beeps")]
    (set-field-value! id 'beeps-ignored beeps-ignored)
    (update-player-beep-use id strategy uses-beeps secs beeps-ignored beeps-followed)))

(defn player-followed-beeps
  [id]
  (let [beeps-ignored (d-get-field-value id 'beeps-ignored "player-followed-beeps")
        beeps-followed (or (d-get-field-value id 'beeps-followed "player-followed-beeps") -1)
        _ (if (and (= beeps-followed -1) (dplev :error :all))
            (println "ERROR: Failed to get field value of" id "beeps-followed in player-followed-beeps"))
        beeps-followed (+ beeps-followed 1)
        uses-beeps (d-get-field-value id 'beep-response "player-followed-beeps")
        strategy (d-get-field-value id 'triage-strategy "player-followed-beeps")
        secs  (d-get-field-value id 'seconds-remaining "player-followed-beeps")]
    (set-field-value! id 'beeps-followed beeps-followed)
    (update-player-beep-use id strategy uses-beeps secs beeps-ignored beeps-followed)))


;;; Belief-state update for strategy +++ move to an aging approach
(defn update-player-triage-strategy
  [id strategy bresp secs gold-victims-triaged green-victims-triaged left-gold-victims left-green-victims]
  (when (dplev :all) (println "UPTS: YT=" gold-victims-triaged "GT=" green-victims-triaged
           "YS=" left-gold-victims "GS=" left-green-victims
           "Tstrategy=" strategy "Bresponse=" bresp
           "sec=" secs))
  (let []
    (cond
      (< secs 285)                      ; All players know when the gold victims have died.
      (do (set-field-value! id 'triage-strategy :green-victims-only)
          (if (not (= strategy :green-victims-only))
            (do
              (when (dplev :strategy :all) (println "Player strategy changing from" strategy "to :green-victims-only -c0"))
              ;; C0: publish a belief state update message +++
              )))

      (and (> gold-victims-triaged 1) (= green-victims-triaged 0)
           (= left-gold-victims 0) (= left-green-victims 0))
      ;; Green victims can be avoided so they don't show up as skipped (for now)
      (do (set-field-value! id 'triage-strategy :gold-victims-only)
          (if (not (= strategy :gold-victims-only))
            (do
              (when (dplev :strategy :all) (println "Player strategy changing from" strategy "to :gold-victims-only -c1"))
              ;; C1: publish a belief state update message +++
              )))

      ;; Perfect adherence to the rule
      (and (= left-gold-victims 0) (> left-green-victims 0))
      (do (set-field-value! id 'triage-strategy :gold-victims-only)
          (if (not (= strategy :gold-victims-only))
            (do
              (when (dplev :strategy :all) (println "Player strategy changing from" strategy "to :gold-victims-only -c1"))
              ;; C1: publish a belief state update message +++
              )))

      (and (= left-green-victims 0) (> left-gold-victims 0))
      (do (set-field-value! id 'triage-strategy :green-victims-only)
          (if (not (= strategy :green-victims-only))
            (do
              (when (dplev :strategy :all) (println "Player strategy changing from" strategy "to :green-victims-only -c2"))
              ;; C2: publish a belief state update message +++
              )))

      (and (= left-green-victims 0) (= left-gold-victims 0)
           (> green-victims-triaged 0) (> gold-victims-triaged 0))
      (do (set-field-value! id 'triage-strategy :all-victims)
          (if (not (= strategy :all-victims))
            (do
              (when (dplev :strategy :all) (println "Player strategy changing from" strategy "to :all-victims -c3"))
              ;; publish a belief state update message +++
              )))

      ;; Sufficiently following rule
      (and (> left-gold-victims 0) (> left-green-victims 0)
           (> gold-victims-triaged 0) (> green-victims-triaged 0)
           (> (/ (/ gold-victims-triaged left-gold-victims) 2.5)
              (/ green-victims-triaged left-green-victims)))
      (do (set-field-value! id 'triage-strategy :gold-victims-only)
          (if (not (= strategy :gold-victims-only))
            (do
              (when (dplev :strategy :all) (println "Player strategy changing from" strategy "to :gold-victims-only -c4"))
              ;; publish a belief state update message +++
              )))

      (and (> left-green-victims 0) (> left-gold-victims 0)
           (> green-victims-triaged 0) (> gold-victims-triaged 0)
           (> (/ (/ green-victims-triaged left-green-victims) 2.5)
              (/ gold-victims-triaged left-gold-victims)))
      (do (set-field-value! id 'triage-strategy :green-victims-only)
          (if (not (= strategy :green-victims-only))
            (do
              (when (dplev :strategy :all) (println "Player strategy changing from" strategy "to :green-victims-only -c5"))
              ;; publish a belief state update message +++
              )))

      (and (> left-green-victims 0) (> left-gold-victims 0)
           (> green-victims-triaged 0) (> gold-victims-triaged 0)
           (> (/ green-victims-triaged 3.0) left-green-victims)
           (> (/ gold-victims-triaged 3.0) left-gold-victims))
      (do (set-field-value! id 'triage-strategy :all-victims)
          (if (not (= strategy :all-victims))
            (do
              (when (dplev :strategy :all) (println "Player strategy changing from" strategy "to :all-victims -c6"))
              ;; publish a belief state update message +++
              ))))))

(defn player-triaged
  [id color]
  (let [gold-victims-triaged (+ (d-get-field-value id 'gold-victims-triaged "player-triaged") (if (= color :gold) 1 0))
        green-victims-triaged (+ (d-get-field-value id 'green-victims-triaged "player-triaged") (if (= color :green) 1 0))
        left-gold-victims (d-get-field-value id 'left-gold-victims "player-triaged")
        left-green-victims (d-get-field-value id 'left-green-victims "player-triaged")
        strategy (d-get-field-value id 'triage-strategy "player-triaged")
        bresp  (d-get-field-value id 'beep-response "player-triaged")
        secs  (d-get-field-value id 'seconds-remaining "player-triaged")]
    (set-field-value! id (if (= color :gold) 'gold-victims-triaged 'green-victims-triaged)
                      (if (= color :gold) gold-victims-triaged green-victims-triaged))
    (update-player-triage-strategy id strategy bresp secs gold-victims-triaged green-victims-triaged left-gold-victims left-green-victims)))

;;; Statistict update for strategy
(defn player-skipped
  [id color]
  (let [gold-victims-triaged (d-get-field-value id 'gold-victims-triaged "player-skipped")
        green-victims-triaged (d-get-field-value id 'green-victims-triaged "player-skipped")
        left-gold-victims (+ (d-get-field-value id 'left-gold-victims "player-skipped") (if (= color :gold) 1 0))
        left-green-victims (+ (d-get-field-value id 'left-green-victims "player-skipped") (if (= color :green) 1 0))
        strategy (d-get-field-value id 'triage-strategy "player-skipped")
        bresp  (d-get-field-value id 'beep-response "player-skipped")
        secs  (d-get-field-value id 'seconds-remaining "player-skipped")]
    (set-field-value! id (if (= color :gold) 'left-gold-victims 'left-green-victims)
                      (if (= color :gold) left-gold-victims left-green-victims))
    (update-player-triage-strategy id strategy bresp secs gold-victims-triaged green-victims-triaged left-gold-victims left-green-victims)))


(defn maybe-handle-expired-prediction
  [id aprediction]
  (let [{subj :subject
         obj  :object
         act  :action
         use  :using
         why  :reason
         hid  :hypothesis-id
         rank :hypothesis-rank} aprediction
        strategy (if id (d-get-field-value id 'triage-strategy "maybe-handle-successful-prediction"))] ; "agentBeliefState.participant1"
    (if (not (number? rank)) (when (dplev :error :all) (println "ERROR: hypothesis rank missing." aprediction)))
    (if (= rank 0) (when (dplev :all) (println "Prediction expiring:" act obj)))
    (if id (case act
             :triage-victim
             (if (= hid "opportunistic-triage")
               (let [color (victims/color-of-victim obj)]
                 (if (= rank 1) (player-skipped id color))))

             :enter-room
             (if (and (= hid "opportunistic-entering") (= rank 1))
               (case why
                 0 ; If the beeps=0 followed
                 (player-followed-beeps id)

                 1 ; If the beeps=1, if player strategy = green-only or all, ignored, followed if only-gold
                 (if (or (= strategy :green-victims-only) (= strategy  :all-victims))
                   (player-ignored-beeps id)
                   (player-followed-beeps id))

                 2 ; If the beeps=2
                 (player-ignored-beeps id)

                 nil)
               nil)
             nil))))                            ; Nothing to do with other cases, yet

(defn new-prediction-statistic
  [pred-type]
  (let [data (seglob/get-se-predictions)
        ;;_ (if (dplev :all) (println "data=" data))
        preds-of-type (get data pred-type 0)
        ;;_ (if (dplev :all) (println "preds-of-type=" preds-of-type))
        newmap (conj data {pred-type (+ preds-of-type 1)})]
    (seglob/set-se-predictions newmap)))

(defn new-successful-prediction-statistic
  [pred-type]
  (let [data (seglob/get-se-successful-predictions)
        ;;_ (if (dplev :all) (println "data=" data))
        preds-of-type (get data pred-type 0)
        ;;_ (if (dplev :all) (println "preds-of-type=" preds-of-type))
        newmap (conj data {pred-type (+ preds-of-type 1)})]
    (seglob/set-se-successful-predictions newmap)))

(defn report-short-statistics
  []
  (when (dplev :io :all)
    (println "*STATS* Predictions published:" (apply + (vals (seglob/get-se-predictions)))
             "successful:" (apply + (vals (seglob/get-se-successful-predictions)))
             "p(success):" (/ (apply + (vals (seglob/get-se-successful-predictions)))
                              (float (apply + (vals (seglob/get-se-predictions)))))
             "semap:" (seglob/get-se-predictions)
             "okmap:" (seglob/get-se-successful-predictions))
    (println "Interventions given:" (seglob/get-interventions-given))))

(defn report-detailed-statistics
  []
  (let [skeys (keys (seglob/get-se-predictions))]
    (when (dplev :io :all)
      (println "type  predictions successful p(correct)")
      (doseq [akey skeys]
        (println akey (get (seglob/get-se-predictions) akey 0) (get (seglob/get-se-successful-predictions) akey 0)
                 (/ (get (seglob/get-se-successful-predictions) akey 0)
                    (float (get (seglob/get-se-predictions) akey 0))))))))

(defn maybe-handle-successful-prediction
  [id aprediction]
  (let [{subj :subject
         obj  :object
         act  :action
         use  :using
         why  :reason
         hid  :hypothesis-id
         rank :hypothesis-rank} aprediction
        strategy  (if id (d-get-field-value id 'triage-strategy "maybe-handle-successful-prediction"))] ;"agentBeliefState.participant1"
    (if (not (number? rank)) (when (dplev :error :all) (println "ERROR: hypothesis rank missing." aprediction)))
    (if (= rank 0) (new-successful-prediction-statistic act))
    (report-short-statistics)
    (if id (case act
             :enter-room
             (let [strategy (if id (d-get-field-value id 'triage-strategy "maybe-handle-successful-prediction"))]
               (if (= hid "opportunistic-entering")
                 (case why
                   0 ; If the beeps=0 and player entered ignored
                   (if (= rank 1) (player-ignored-beeps id))

                   1 ; If the beeps=1 and player strategy = green-only followed, otherwise ignored
                   (if (or (= strategy :green-victims-only) (= strategy  :all-victims))
                     (if (= rank 1) (player-followed-beeps id))
                     (if (= rank 1) (player-ignored-beeps id)))

                   2 ; If the beeps=2 followed
                   (if (= rank 1) (player-followed-beeps id))

                   nil)
                 nil))
             nil))))

(def duplicate-action-exclusions
  [;; Exclude mandatory metric predictions M1-M7 from duplicate removal
   :final-score :m7_will_not_enter_room :m7_will_enter_room :m6 :m3
   ;; Exclude RITA metric predictions
   :PM1-story-selection :PM2-story-event-prediction :PM4-story-communication-prediction])

(declare add-to-predictions)

(defn add-prediction-message
  [existingmsgs id newmsg]
  (let [{act :action
         obj :object
         subj :subject
         use  :using
         why  :reason
         rank :hypothesis-rank} newmsg]
    (if (or
         (some (partial = act) duplicate-action-exclusions)
         (not (some (fn [[tb pred]] (and (= act  (:action pred))
                                         (= obj  (:object pred))
                                         (= use  (:using pred))
                                         (= why  (:reason pred))
                                         (= subj (:subject pred))
                                         (= rank (:hypothesis-rank pred)))) (seglob/get-predictions))))
      ;; Avoid duplicate predictions
      (let [msg (conj newmsg {:uid (seglob/uidgen) :state "unknown"})
            rank (get msg :hypothesis-rank)
            act (get msg :action)]
        (when (dplev :prediction :all) (println "!Prediction! " msg))
        (if (= rank 0) (new-prediction-statistic act))
        (add-to-predictions id msg)
        (ritamsg/add-message existingmsgs "predictions" :predictions msg))
      (do
        (when (dplev :prediction :all) (println "DUPLICATE PREDICTION:" newmsg))
        existingmsgs))))

(defn get-unexpired-predictions
  [id]
  (let [tim (seglob/rita-ms-time)
        expired (remove nil? (map (fn [p] (if (>= tim (first p)) p)) (seglob/get-predictions)))]
    (doseq [anexpired expired]
      (maybe-handle-expired-prediction id (second anexpired)))
     (into [] (remove nil? (map (fn [p] (if (< tim (first p)) p)) (seglob/get-predictions))))))

(defn add-to-predictions
  [id pred]
  (let [tim (seglob/rita-ms-time)              ; (System/currentTimeMillis)
        upperbound (second (:bounds pred))
        ;;_ (when (dplev :all) (println "timeout=" timeout))
        timeout (+ (* 1000 upperbound) tim) ; convert to milliseconds
        _ (when (dplev :all) (println "timeout=(" upperbound  ") seconds, timeout=" timeout))]
    (seglob/set-predictions (conj (get-unexpired-predictions id) [timeout pred]))))

(declare match-prediction)


(defn translate-sid
  [sid]
  (case sid
    :sss "Search_First_Delayed_Reward_Move_Victims_For_Faster_Processing"
    :hhh "Expose_First_Reveal_Blocked_Victims_And_Find_Critical_Victims"
    :mmm "Low_Hanging_Fruit_give_care_to_all_easy_cases_first"
    :mmh "Handle_Non_Critical_victims_With_One_Hazardous_Materials_Specialist_For_Unreachable_victims"
    :ssh "Move_Victims_First_With_One_Hazardous_Materials_Specialist_For_Unreachable_victims"
    :hhs "Emphasize_Rubble_Removal_With_One_Medic_To_Give_Care"
    :mms "Two Medics_And_A_Search_Specialist"
    :ssm "Emphasize_Moving_Normal_Victims_With_One_Medic_To_Give_Care"
    :hhm "Emphasize_Search_And_Care_Of_Critical_Victims"
    :hsm "One_Of_Each_For_clean_sweep_of_building"
    :frozen "Injured_Participant_needs_Care"
    :critical "Handle_Critical_Victim"
    :help "Call_For_Help"
    :helper-response "Team_Member_Coming_To_Give_Care"
    "No matching story found"))


(defn push-story
  [new-sid]
  (seglob/set-instantiated-stories (conj (seglob/get-instantiated-stories) (translate-sid new-sid))))

(defn pop-story
  [sid]
  (if (= (last (seglob/get-instantiated-stories)) (translate-sid sid))
    (seglob/set-instantiated-stories (pop (seglob/get-instantiated-stories)))))


(defn predict-story
  [pubs id story pid em]
  (let [pub1 (add-prediction-message
              pubs
              id
              {:action :PM1-story-selection
               :story (translate-sid story)
               :subject (translate-sid story)
               :object pid
               :active-stories (if (not (empty? (seglob/get-instantiated-stories))) (seglob/get-instantiated-stories))
               :hypothesis-id   "hyp0001-pending"
               :elapsed_milliseconds em
               :hypothesis-rank 0
               :bounds [0 120]
               :agent-belief 0.95})]
    (push-story story)
    (match-prediction id :PM1-story-selection (translate-sid story))
    pub1))

(defn predict-coms
  [pubs id story pid em]
  (let [pub1 (add-prediction-message
              pubs
              id
              {:action :PM4-story-communication-prediction
               :story (translate-sid story)
               :subject pid
               :active-stories (if (not (empty? (seglob/get-instantiated-stories))) (seglob/get-instantiated-stories))
               :hypothesis-id   "hyp0001-pending"
               :elapsed_milliseconds em
               :hypothesis-rank 0
               :bounds [0 30]
               :agent-belief 0.95})]
    (match-prediction id :PM4-story-selection (translate-sid story))
    pub1))

(defn predict-story-event
  [pubs id story pid em]
  (let [pub1 (add-prediction-message
              pubs
              id
              {:action :PM2-story-event-prediction
               :story (translate-sid :frozen)
               :subject pid
               :active-stories (if (not (empty? (seglob/get-instantiated-stories))) (seglob/get-instantiated-stories))
               :hypothesis-id   "hyp0001-pending"
               :elapsed_milliseconds em
               :hypothesis-rank 0
               :bounds [0 75]
               :agent-belief 0.95})]
    ;(match-prediction id :PM2-story-event-prediction (translate-sid :frozen))
    pub1))

(def trial-1-mean 533.33)
(def trial-2-mean 686.00)

(def trial-1-sd   533.33)
(def trial-2-sd   686.00)

(defn predict-final-score
  [pubs id elapsedseconds elapsed_milliseconds]
  (let [teamquality (ts/current-team-strength)
        lpm (seglob/get-learned-model)
        strategy (seglob/get-role-strategy)
        [currentscore tmsecs] (seglob/get-current-score-at)
        [mean sd meanfs sdfs] (slearn/get-score-moments-at lpm strategy elapsed_milliseconds)
        noscoreyet (and (== mean 0.0) (== currentscore 0.0))
        sd (cond (not (== sd 0.0)) sd
                 (and (== sd 0.0) (> sdfs 0.0)) sdfs
                 (and (== sd 0.0) (== sdfs 0.0) (> mean 0)) mean
                 (== elapsed_milliseconds 0) (max 1.0 mean)
                 (> sdfs 0.0) (* sdfs (/ elapsed_milliseconds (* 17 60 1000.0))) ; Updated for Study3
                 :otherwise 1)
        difference (- currentscore mean) ; Is the current score more or less that what the model predicts
        diff-in-sds (/ difference sd)    ; Normalize the difference by standard deviations
        teamquality (if noscoreyet 0.0 (ts/update-team-strength diff-in-sds))
        msecs-remaining (- (* 17 60 1000.0) elapsed_milliseconds) ;+++ updated for study 3, was 15 minutes for study2
        projected-score (* 10                                 ; scores are divisible by 10 don't predict
                           (int                               ; impossible scores. Maybe should round?
                            (/ (+ meanfs
                                  (* teamquality sdfs))       ; prorated adjustment based on team strength
                               10)))]
    (when (and (> sd 0.0) (> sdfs 00.0))
      (when (dplev :io :all)
        (println "difference=" difference "diff-in-sds=" diff-in-sds "mean=" mean "sd=" sd "meanfs=" meanfs "sdfs=" sdfs "secs remaining=" (/ msecs-remaining 1000)))
      (when (dplev :io :all)
        (println "New projected score=" projected-score "New team-quality=" teamquality))
      (add-prediction-message
       pubs
       id
       {:action :final-score
        :object (str projected-score)
        :hypothesis-id   "hyp0001-pending"
        :elapsed_milliseconds elapsed_milliseconds
        :hypothesis-rank 0
        :bounds [(int (min 120 (/ msecs-remaining 1000.0))) (int (min 120 (/ msecs-remaining 1000.0)))]
        :agent-belief (+ 0.5 (* 0.5 (/ elapsedseconds (* 17.0 60))))})))) ; +++ updated for study 3

(defn successful-prediction-message
  [msg]
  {:mtopic "predictions" :mtype :predictions :message msg})

(defn match-prediction
  [id action object]
  (let [preds (get-unexpired-predictions id)
        attime (seglob/rita-ms-time)
        ;;_ (when (dplev :all) (println "match-prediction" action object "preds=" preds))
        ;; Find all matching predictions (could be more than 1 because of multiple hypotheses
        successes (filter (fn [x] (and (= action (:action (second x)))
                                       (= object (:object (second x)))
                                       x))
                          preds)
        ;; (filter (fn [x] (not (some #{x} '(1 2 3)))) [5 3 1 3 2 6 8 2 1])
        remaining-preds (filter (fn [x] (not (some #{x} successes))) preds)]
    ;;(when (dplev :all) (println "successes=" successes))
    ;; Process each of the successful matches
    (doseq [success successes]
      (if (and success (<= attime (first success))) ; must be true!
        (let [prediction (merge (second success) {:state true})]
          (maybe-handle-successful-prediction id prediction)
          (when (dplev :prediction :all) (println "!Prediction correct: " prediction "at" attime "with"
                   (/ (- (first success) attime) 1000) "seconds to spare!"))
          (seglob/set-successful-predictions
            (conj (seglob/get-successful-predictions) prediction)))))
    (seglob/set-predictions remaining-preds)
    nil))


(defn will-player-triage-victim?
  [id color]
  (let [strategy (d-get-field-value id 'triage-strategy "will-player-triage-victim?")
        secs (d-get-field-value id 'seconds-remaining "will-player-triage-victim?")
        time-remaining (d-get-field-value id 'seconds-remaining "will-player-triage-victim?")]
    ;;(when (dplev :all) (println "In will-player-triage-victim? strategy=" strategy "time-remaining=" time-remaining "color=" color))
    (if (and time-remaining (< time-remaining 315))
      (= color :green)
      (case strategy
        :gold-victims-only (and (= color :gold) (> secs 315))
        :green-victims-only (= color :green)
        :all-victims (or (> secs 315) (= color :green))
        false))))


(defn get-time-remaining
  [id]
  (let [id "agentBeliefState.participant1"] ;+++ for now participant1 is the keeper of remaining time
    (d-get-field-value id 'seconds-remaining "get-time-remaining")))

(defn player-attends-to-beeps?
  [id]
  (let [uses-beeps (d-get-field-value id 'beep-response "player-attends-to-beeps?")]
    uses-beeps))

(defn wants-to-enter-room?
  [id aroom]
  (let [visitedp (seglob/room-entered? aroom) ;+++ (room-already-visited? aroom)
        numbeeps (tb/beeps-for-room aroom)
        willgreen (will-player-triage-victim? id :green)
        time-remaining (get-time-remaining id)
        beeperp (player-attends-to-beeps? id)]
    (when (dplev :all) (println "In wants-to-enter-room, visitedp=" visitedp
             "numbeeps=" numbeeps
             "willgreen=" willgreen
             "time=" time-remaining
             "beeperp=" beeperp))
    (cond (not (a-room? aroom))
          ;; Let's not predict wanting to enter an object that we don't know about!
          false

          (and (< time-remaining 320) ; In second phase
               (or (and (> numbeeps 0)
                        (or (not visitedp) ; Everyone wants to go into an unvisited beeping room in phase 2!
                            (= beeperp :always) (= beeperp :usually))) ; too unpredictable if a non-beep user if visited
                   (and (not visitedp)
                        (= beeperp :never)))) ; a non-beep user will enter a non beeped unvisited room.
          true

          (and (>= time-remaining 320) (= numbeeps 2)) ; In first phase
          true

          (and (>= time-remaining 320) (= numbeeps 1) willgreen) ; In first phase
          true

          (and (>= time-remaining 320) (= beeperp :never))
          true

          :otherwise
          false)))

(defn maybe-predict-portal-use [pub id playername pname whereIam near-portal em]
  ;; stand-in for the model-based prediction
  (cond (spreads/in-a-room? whereIam)
        (let [otherside (get-other-side-of near-portal whereIam)
              doorname (and near-portal (get-object-vname near-portal))
              roomname (and otherside (get-object-vname otherside))
              myroomname (get-object-vname whereIam)]
          ;; If we are near a closed door, predict that we are goint to open it
          (add-prediction-message
           pub
           id
           ;; {:action :exit-room-through
           ;;  :object (get-object-vname near-portal)
           ;;  :from-room roomname
           ;;                 "Unknown")
           ;;  :bounds 5
           ;; :probability 0.6}
           {:subject playername
            :action :exit-room
            :object (or myroomname "missing room name")
            :using doorname
            :hypothesis-id   "hyp0001-pending"
            :hypothesis-rank 0
            :reason [:and (if doorname
                            {:subject playername
                             :is-moving-towards doorname
                             :agent-belief 0.9})
                     ]
            :bounds [0 5]
            :agent-belief 0.6}))

        (spreads/in-a-corridor? whereIam)
        (let [portal-type (global/RTobject-type near-portal)
              portal-var (global/RTobject-variable near-portal)
              bel-closed-door (bs/get-belief-in-variable portal-var :closed)
              otherside (get-other-side-of near-portal whereIam)
              doorname (get-object-vname near-portal)
              roomname (or (and otherside (get-object-vname otherside)) "Unknown rooom")
              _ (if (not otherside)
                  (when (dplev :warn :all) (println "Can't find the other side of portal" doorname "(" portal-var ")")))
              _ (when (dplev :all) (println "Portal" doorname
                         "type=" portal-type "believe closed=" bel-closed-door))
              ppub (if (and false
                            (not (spreads/door-open? near-portal)) ; was(and (= portal-type 'Door) (> bel-closed-door 0.6))
                            otherside
                            (vol/a-room? otherside)
                            (wants-to-enter-room? id otherside))
                     (add-prediction-message
                      pub
                      id
                      {:subject playername
                       :action :open
                       :object doorname
                       :using nil
                       :hypothesis-id   "hyp0001-pending"
                       :hypothesis-rank 0
                       :reason [:and (if doorname
                                       {:subject playername
                                        :is-moving-towards doorname
                                        :agent-belief 0.9})
                                {:subject playername
                                 :wants-to-enter
                                 roomname
                                 :agent-belief 0.9}
                                {:subject doorname
                                 :is "closed"
                                 :agent-belief bel-closed-door}]
                       :bounds [0 5]
                       :agent-belief 0.6})
                     pub)
              pppub (if (and otherside
                             (vol/a-room? otherside)
                             (wants-to-enter-room? id otherside))
                      (add-prediction-message
                       ppub
                       id
                       ;; {:action :enter-room-through
                       ;;  :object (get-object-vname near-portal)
                       ;;  :to-room (or (and otherside (get-object-vname otherside))
                       ;;               "Unknown")
                       ;;  :timeout 5
                       ;;  :probability 0.9}
                       {:subject playername
                        :action :enter-room
                        :hypothesis-id   "hyp0001-pending"
                        :hypothesis-rank 0
                        :object roomname
                        :using doorname
                        :reason [:and (if otherside
                                        {:subject playername
                                         :is-moving-towards doorname
                                         :agent-belief 0.9})
                                 {:subject playername
                                  :has-not-visited roomname
                                  :agent-belief 1.0}
                                 {:subject playername
                                  :wants-to-find
                                  "victim"
                                  :agent-belief 0.9}
                                 {:subject playername
                                  :wants-to-triage "victim"
                                  :agent-belief 1.0}]
                        :bounds [0 5]
                        :agent-belief 0.6})
                      ppub)
              ppppub (if (and otherside (vol/a-room? otherside))
                       (add-prediction-message
                        pppub
                        id
                        {:subject playername
                         :action :enter-room
                         :hypothesis-id   "opportunistic-entering"
                         :hypothesis-rank 1
                         :object roomname
                         :using doorname
                         :reason (tb/beeps-for-room otherside) ;(num-beeps-for-room otherside)
                         :bounds [0 5]
                         :agent-belief 0.2})
                       pppub)

              ]
          ppppub)))

(defn maybe-predict-victim-triage [pub id playername whereIam near-victim]
  ;; stand-in for the model-based prediction
  (if (not (seglob/has-role? id "Medical_Specialist"))
    pub
    (let [pub1 (add-prediction-message
                pub
                id
                {:subject playername
                 :action :triage-victim
                 :hypothesis-id   "opportunistic-triage"
                 :hypothesis-rank 1
                 :object (global/RTobject-variable near-victim)
                 :using "first-aid-kit"
                 :reason [:and
                          {:subject playername
                           :is-close-to (get-object-vname near-victim)
                           :agent-belief 1.0}
                          {:subject (get-object-vname near-victim)
                           :is-a "victim"
                           :agent-belief 1.0}
                          {:subject playername
                           :wants-to-triage "victim"
                           :agent-belief 1.0}
                          {:subject playername
                           :has-strategy
                           :all-players}]
                 :bounds [0 21]
                 :agent-belief 0.2})]
      (cond (and (will-player-triage-victim? id (victims/color-of-victim near-victim))
                 (>= (bs/get-belief-in-variable (global/RTobject-variable near-victim) :awaiting-triage) 0.8))

            (add-prediction-message
             pub1
             id
             ;; {:action :triage-victim
             ;;  :object (get-object-vname near-victim)
             ;;  :timeout 5 :probability 0.9}
             {:subject playername
              :action :triage-victim
              :hypothesis-id   "hyp0001-pending"
              :hypothesis-rank 0
              :object (global/RTobject-variable near-victim)
              :using "first-aid-kit"
              :reason [:and
                       {:subject playername
                        :is-close-to (get-object-vname near-victim)
                        :agent-belief 1.0}
                       {:subject (get-object-vname near-victim)
                        :is-a "victim"
                        :agent-belief 1.0}
                       {:subject playername
                        :wants-to-triage "victim"
                        :agent-belief 1.0}]
              :bounds [0 5]
              :agent-belief 0.6}   )

            :otherwise pub1))))


(defn maybe-predict-switch-use [pub id playername whereIam near-switch]
  ;; stand-in for the model-based prediction
  (cond (not (= (seglob/get-loaded-model-name) "Falcon"))
        ;; +++ need state of illumination to learn whether ipower is on or off +++
        (add-prediction-message
         pub
         id
         ;; {:action :turn-on-switch
         ;;  :object (get-object-vname near-switch)
         ;;  :timeout 5 :probability 0.9}
         {:subject playername
          :action :set-switch-state
          :hypothesis-id   "hyp0001-pending"
          :hypothesis-rank 0
          :object (get-object-vname near-switch)
          :reason [:and
                     {:subject playername
                      :is-close-to (get-object-vname near-switch)
                      :agent-belief 1.0}
                     {:object (get-object-vname near-switch)
                      :has-state :off
                      :agent-belief 0.5}
                     {:subject playername
                      :wants-to-find "victim"
                      :agent-belief 1.0}]
            :bounds [0 5]
            :agent-belief 0.6}
         )))


(defn rebalance
  [pdf]
  (let [total (apply + (map second pdf))]
    (into {} (map (fn [[thing prob]] [thing (/ prob (float total))]) pdf))))


(defn room-unvisited
  [roomname]
  (<= (bs/get-belief-in-variable roomname :visited) 0.2))

;;; If multiple contenders for first place are very close, return all of them for crosreferencing
;;; with other support.
(defn get-best-matches
  [matchmap]
  (let [maxmatch (apply max-key val matchmap)
        matches (filter #(< (- (second maxmatch) (second %)) 0.05) matchmap)]
    matches))

(defn best-supporting-bi-tri-matches
  [best-bis best-tris]
  (let [numbis (count best-bis)
        numtris (count best-tris)
        result (cond (and (= numtris 0) (= numbis 1))
                     [(first best-bis) nil]

                     (and (= numtris 1) (= numbis 0)) ; This case should be impossible
                     [nil (first best-tris)]

                     (and (= numbis 1) (= numtris 1))
                     [(first best-bis) (first best-tris)]

                     (and (> numbis 1) (= numtris 1))
                     [[(first (first best-tris)) (get (into {} best-bis) (first (first best-tris)))] (first best-tris)]

                     (and (> numbis 1) (> numtris 1)) ; +++ could do marginally better here (select best from near)
                     (let [trikeys (into #{} (keys best-tris))
                           bikeys  (into #{} (keys best-bis))
                           shared-keys (clojure.set/intersection trikeys bikeys)]
                       (if (empty? shared-keys)
                         [(apply max-key val (into {} best-bis)) (apply max-key val (into {} best-tris))]
                         [[(first shared-keys) (get (into {} best-bis) (first shared-keys))]
                          [(first shared-keys) (get (into {} best-tris) (first shared-keys))]]))

                     :otherwise [(first best-bis) (first best-tris)])]
    result))

;;; (best-supporting-bi-tri-matches nil [[:A 0.5]])
;;; (best-supporting-bi-tri-matches [[:A 0.5]] nil)
;;; (best-supporting-bi-tri-matches [[:A 0.5]] [[:B 0.5]])
;;; (best-supporting-bi-tri-matches [[:B 0.5] [:A 0.5]] [[:A 0.5]])
;;; (best-supporting-bi-tri-matches {:A 0.5, :B 0.5} {:A 0.5, :C 0.5})

(defn maybe-predict-next-room ; plus prediction into message chain
  [pub id playername vecprevtworooms]
  (let [emission (seglob/get-experiment-mission)
        role (seglob/get-role playername)
        loadedlm (seglob/get-learned-model)
        [bigram trigram] (get loadedlm :room-visit-order) ; first=bigram second=trigram
        bigrammodel (get bigram  [role emission])
        bigrammatch (get bigrammodel [(last vecprevtworooms)])
        filtered-bigram (rebalance (filter #(room-unvisited (first %)) bigrammatch))
        trigrammodel (get trigram  [role emission])
        trigrammatch (get trigrammodel vecprevtworooms)
        filtered-trigram (rebalance (filter #(room-unvisited (first %)) trigrammatch))
        best-bigram-matches (if (not (empty? filtered-bigram)) (get-best-matches filtered-bigram))
        best-trigram-matches (if (not (empty? filtered-trigram)) (get-best-matches filtered-trigram))
        [best-bigram-match best-trigram-match] (best-supporting-bi-tri-matches best-bigram-matches best-trigram-matches)]
    ;; Trigram is more compelling but may lack evidence.
    ;;(when (dplev :all) (println "best-bigram-match=" best-bigram-match "best-trigram-match=" best-trigram-match))
    ;;(when (dplev :all) (println "vecprevtworooms=" vecprevtworooms "trigrammatch=" trigrammatch))
    (let [conclusion
          (cond (and best-trigram-match best-bigram-match ; a trigram match will always have bigram match
                     (= (first best-trigram-match) (first best-bigram-match)))
                [(first best-trigram-match) (max (second best-trigram-match) (second best-bigram-match))]

                (and best-trigram-match best-bigram-match ; a trigram match will always have bigram match
                     (not (= (first best-trigram-match) (first best-bigram-match))))
                [(first best-trigram-match) (- (second best-trigram-match) (second best-bigram-match))]

                best-bigram-match
                best-bigram-match

                best-trigram-match      ; In theory this is impossible
                best-trigram-match)]
      (if (and conclusion (> (second conclusion) 0.6)) ;+++ arbitrary threshold for achieving a descent rate of success.
        (add-prediction-message
         pub
         id
         ;; {:action :turn-on-switch
         ;;  :object (get-object-vname near-switch)
         ;;  :timeout 5 :probability 0.9}
         {:subject playername
          :action :next-room-to-visit
          :hypothesis-id "hyp0001-pending"
          :hypothesis-rank 0
          :object (first conclusion)
          :reason [:and
                   {:subject playername
                    :wants-to-visit "all-rooms"
                    :agent-belief 1.0}]
          :bounds [0 500] ; calculate these bounds - we have the data (problem with 5 minute pause.)
          :agent-belief (second conclusion)}
         )
        pub))))
