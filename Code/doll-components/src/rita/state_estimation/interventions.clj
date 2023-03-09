;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.interventions
  "Interventions."
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
            [rita.state-estimation.interventionengine :as ie :refer :all]
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

#_(in-ns 'rita.state-estimation.interventions)

(def ^:dynamic *victims-warned-about*    #{})  ;+++ cleanup reset etc
(def ^:dynamic *times-triage-tip-given*  0)
(def ^:dynamic *time-message-last-given* 0)

(defn reset-per-trial-variables
  []
  (reset-has-seen)                      ; Maybe we don't want this +++
  (def ^:dynamic *victims-warned-about*    #{})
  (def ^:dynamic *times-triage-tip-given*  0)
  (def ^:dynamic *time-message-last-given* 0))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Test how much can be printed without overflowing the chat

;;; DTI0001

;;; Guard
(defn lorem-ipsum-cond   [] (and false (== EPOCH 0) (not HAS-SEEN))) ; Disabled, it was just a test! remove "false" if you want to see it!

;;; Handler
(defn lorem-ipsum
  "lorem ipsum test"
  [fbo via em expl] ;[pid em expl]
  (println "In lorem-ipsum with fbo=" fbo "via=" via "at em=" em)
  (let [intervention  "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam hendrerit nisi sed sollicitudin pellentesque. Nunc posuere purus rhoncus pulvinar aliquam. Ut aliquet tristique nisl vitae volutpat. Nulla aliquet porttitor venenatis. Donec a dui et dui fringilla consectetur id nec massa. Aliquam erat volutpat. Sed ut dui ut lacus dictum fermentum vel tincidunt neque. Sed sed lacinia lectus. Duis sit amet sodales felis. Duis nunc eros, mattis at dui ac, convallis semper risus. In adipiscing ultrices tellus, in suscipit massa vehicula eu."]
    (ritamsg/publish-intervention [via] intervention em expl)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Team process

;; Planning/Reflection Coordination Balance Coordination
;; Situation: One participant has a higher cognitive load than the others.
;; Goal: Get the team to adjust their team process, such as the use of markers, in order to rebalance the cognitive load more evenly.
;; Prompt: A player identifies as showing leadership skills is made aware of the emerging problem and is advised to work out a solution with the team. The exact wording is still under discussion. In general, our strategy is to describe the problem, not the solution, and to choose the best person to explain this to.  Then that person will work with the team to come up with a solution.
;; Measured: Team communication
;; Short term: Improved use of markers.
;; Long term: Cognitive load becomes more balanced over time.
;; Targeted outcome: Team planning Category:SC Uses: IHMC, Gallup, UCF

;;; DII0001

;;; Guard
(defn cog-load-imbalance [] (and (cogload/transporter-cog-load-high-and-highest)
                                 (= (ts/marker-usage :tran EPOCH 1.5) [:below :below])
                                 (= (ts/marker-usage :med  EPOCH 1.5) [:below :below])
                                 (= (ts/marker-usage :eng  EPOCH 1.5) [:below :below])
                                 (not HAS-SEEN)))
;;; Handler
(defn cognitive-load-balancing
  "cognitive-load-balancing"
  [fbo via em expl]
  (when true
    (let [overloaded-role fbo
          intervention  (str (str overloaded-role) " is overloaded with things to remember. "
                             "We need to help him out by having everyone place "
                             "and remove markers properly, you all need to work it out, now!")]
      (ritamsg/publish-intervention [via] intervention em expl))))

;; Execution/Action TA1 DOLL/MIT Communication Communication Skills Coordination
;; Situation: A team member is working without communicating useful information with the team.
;; Goal: Get the team to establish a better communication habit.
;; Prompt: A player identified as showing leadership skills is made aware of the imbalance between work and communication and is encouraged to resolve it. The exact wording is still under discussion.
;; Measured: Team communication
;; Short term: Improved communication skills.
;; Long term: Better balance of action and communication.
;; Targeted outcome: Team process Category: SC	Uses: IHMC, Gallup, UCF

;;; DII0002

;;; Guard
(defn weakcomm-nonverbal [] (and (= (ts/marker-usage :tran EPOCH 1.5) [:below :below])
                                 (not HAS-SEEN)))

;;;+++ make more specific version of this
;;;+++ make congratulary version too!
;;; Handler
(defn communication-weakness-non-verbal
  "communication-weakness-non-verbal"
  [fbo via em expl]
  (let [intervention  (str FBO " is working hard but is not adequately using markers to inform the team. "
                           "Someone needs to tell him/her to place/remove markers to communicate with the "
                           "team otherwise his/her efforts will not be properly appreciated")]
    (ritamsg/publish-intervention [via] intervention em expl)))

;; Planning/reflection	TA1 DOLL/MIT	Communication	Communication Skills	Coordination
;; Situation: There is a growing backlog of untransported saved victims
;; Goal: Get the team to reflect upon strategies for phase transitions.
;; Intervention: A player identified as showing leadership skills is made aware of the need to address phase transitions and is encouraged to discuss the issue with the team. The exact wording is still under discussion.
;; Measured: Team communication
;; Short term: The medical specialist, the engineer, or both start transporting victims.
;; Long term: At the end of the trial, there are fewer untransported victims.
;; Targeted outcome: Team process
;; Category: SC	Gallup, UCF, IHMC

;;; DII0003

;;; Guard
(defn plan-phase-chng [] (and (not HAS-SEEN)
                              (> (seglob/waiting-to-be-evacuated) 7)
                              (seglob/waiting-to-be-evacuated)))

;;; Handler
(defn plan-phase-change
  "plan-phase-change"
  [fbo via em expl]
  (let [untransported GUARDRES
        intervention  (str "There is a growing number (" untransported ") of saved victims "
                           "awaiting transport, you should work out a plan with the team "
                           "to ensure that they get transported before the end of the game.")]
      (ritamsg/publish-intervention [via] intervention em expl)))

;; Execution/Action	TA1 DOLL/MIT	Dyad collaboration	Opportunity comprehension	Coordination
;; Situation: The medical specialist is wasting too much time being blocked because the engineer is too far away, causing delays in unblocking the medical specialist.
;; Goal: Get the engineer to understand the value to the team to staying close to the medical specialist.
;; Intervention: A player identified as showing leadership skills is made aware of the need for the engineer to not wander too far from the medical specialist so as to avoid wasteful delays that threaten team efficiency. The exact wording is still under discussion.
;; Measured: The time that the medical specialist remains blocked before the engineer arrives to remove the rubble that has trapped him.
;; Short term: The engineer stays closer to the medical specialist than before.
;; Long term: Less time is wasted waiting to be freed on the part of the medical specialist.
;; Targeted outcome: Developmental Learning
;; Category: TA	Gallup, UCF, IHMC

;;; DII0004



(defn medic-engineer-distant
  "medic-engineer-distant"
  [fbo via em expl] ;+++ conditionalize on recipient +++
  (when true
    (let [intervention  (str "The medic is getting trapped, which is unavoidable, "
                             "but it is taking too long for him/her to get freed.  Ultimately "
                             "this will hurt the team result.  You should talk to the engineer "
                             "to solve the problem.")]
      (ritamsg/publish-intervention [via] intervention em expl))))

;; Execution/Action	TA1 DOLL/MIT	Communication	Communication Skills	Coordination
;; Situation: Failure to indicate the victim type results in the victim being transported to the wrong place
;; Goal: Establish a team process that uses markers to maximum team benefit.
;; Intervention: A player identified as showing leadership skills is made aware of the need for good marker practice to be respected by the team in order to avoid costly mistakes.  The exact wording is still under discussion.
;; Measured: Marker usage by all participants.
;; Short term: Coherent use of markers by players.
;; Long term: The frequency of mistakes connected to inadequate use of marker block is reduced.
;; Targeted outcome: Team process
;; Category: SC	Gallup, UCF, IHMC


;;; DII0005

;;; Guard
(defn no-mark-victim
  []
  false)

;;; Handler
(defn fail-to-mark-victim-type
  "fail-to-mark-victim-type"
  [fbo via em expl]
  (when true
    (let [intervention  (str "The medic is getting trapped, which is unavoidable, but it is taking too long for him/her to get freednot consistantly marking the saved victims. This can lead to victims being transported to the wrong location with a loss of points.  You should reinforce the importance of properuse of markers.")]
      (ritamsg/publish-intervention [via] intervention em expl))))

;;;+++ Done to here+++

;; Execution/Action	TA1 DOLL/MIT	Communication	Communication Skills	Coordination
;; Situation: The evacuation specialist transports victims to the wrong evacuation area, despite being correctly marked.
;; Goal: Make sure that the evacuation specialist understands the importance of delivering the victims to the correct evacuation areas and how the information that is needed to do that can be found by looking at the markers.
;; Intervention: A player identified as showing leadership skills is made aware of the possible misunderstanding that the evacuation specialist has of the evacuation role and that the errors can cost the team performance.   The exact wording is still under discussion.
;; Measured: Unforced errors made by the evacuation specialist.
;; Short term: The evacuation specialist pays more attention to markings and makes less mistakes.
;; Long term: Most victims are transported to the correct areas and the resulting team performance improves.
;; Targeted outcome: Developmental/Learning
;; Category: TA	Gallup, UCF, IHMC

;;; DII0006

;;; Guard
(defn wrong-evac-area
  []
  false)

;Handler
(defn wrong-evacuation-area
  [fbo via em expl]
  nil)

;; Execution/Action	TA1 DOLL/MIT	Effective marker communication	Communication Skills	Coordination
;; Situation: A participant learns something useful to the team, but by not communicating that knowledge, with a marker, an opportunity is lost.
;; Goal: Translate individual knowledge into team knowledge, primarily by efficient use of marker blocks.
;; Intervention: A player identified as showing leadership skills is made aware of the failure to communicate individual knowledge, thus making the team less efficient.  T    It is recommended that a solution be found and that all participants adopt the process for a more efficient team. The exact wording is still under discussion.
;; Measured: Victims that are observed by a participant that are not marked and are overlooked.
;; Short term: Communication between team members is observed, and more consistent use of markers is observed.
;; Long term: Within the fixed time of a trial, more victims are successfully cared for and delivered to the evacuation points.
;; Targeted outcome: Team process
;; Category: TA	Gallup, UCF, IHMC

;;; DII0007 -  poor-communication-marker-placement

;;; Guard
(defn poor-mkr-placement
  []
  false)

;;; Handler
(defn poor-marker-placement
  [fbo via em expl]
  nil)

;;; DII0008 -  poor-communication-marker-removal

;;; Guard
(defn poor-mkr-removal
  []
  false)

(defn poor-marker-removal
  [fbo via em expl]
  nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Agent Introduction

;; Planning/Reflection	TA1 DOLL/MIT	Introduction	Motivation	Trust of the ASI
;; Situation: The first trial is about to begin, the ASI has one chance to establish a connection with the participants.
;; Goal: Persuade each of the participants, separately, that RITA can be useful to them and that they should pay attention to the interventions.
;; Intervention: A message customized for each role is  given at the start of the first trial aimed at convincing the players that paying attention to RITA will be worthwhile.
;; Whether subsequent messages are attended to by the participants.  Eye-tracking would have been nice for that.  Nevertheless, we have designed each intervention so that there is an associated measurable prediction that should inform us of whether the participants are paying attention to RITA.  When RITA concludes that a player is not paying attention, that player will not be chosen as the recipient of the advice, evenMeasured:  if that participant appears to have shown emergent leadership skills.
;; Short term: Participants will pay attention to the interventions.
;; Long term: Team performance will improve faster than baseline improvements learned for no ASI training data.
;; Targeted outcome: Attention to RITA
;; Category: AI	Gallup, UCF

;;; DII0100

;;; Guard

(defn iamritaumedict1 [] (and (== EPOCH 0) (== TRIAL 1) (not HAS-SEEN)))

;;; Handler
(defn hello-i-am-rita-trial-1-medic
  "hello-i-am-rita-trial-1-medic"
  [fbo via em expl]
  (let [intervention  (str "Hello Red, I'm Rita and I have something to share. "
                           "Your special skill is to triage victims, you will need to "
                           "work with your teammates to do this efficiently. "
                           "'Ding' means that I have something to bring to your attention. "
                           "I will alert you to emerging problems, but you will find your own solutions. "
                           "I may help you out in this first trial, if I see you having trouble.")]
    (ritamsg/publish-intervention [via] intervention em expl)))

;;; +++ make separate intervention to say "Note: Each player has different puzzle information" +++

;;; DII0101

;;; Guard
(defn iamritauengineert1 [] (and (== EPOCH 0) (== TRIAL 1) (not HAS-SEEN)))

;;; Handler

(defn hello-i-am-rita-trial-1-engineer
  "hello-i-am-rita-trial-1-engineer"
  [fbo via em expl]
  (let [intervention  (str "Hello Blue, I'm Rita and I'm here to help. Your unique "
                           "skill is clearing rubble.  You can free other teammates in case of collapses "
                           "and you can clear the path to trapped victims. Only you have access to threat room locations. "
                           "Be sure to communicate by using markers. "
                           "'Ding' means that I have something to bring to your attention. "
                           "I will alert you to emerging problems, but you will find your own solutions. "
                           "I may help you out in this first trial, if I see you having trouble.")]
    (ritamsg/publish-intervention [via] intervention em expl)))

;;; DII0102

;;; Guard
(defn iamritautransportt1 [] (and (== EPOCH 0) (== TRIAL 1) (not HAS-SEEN)))

;;; Handler
(defn hello-i-am-rita-trial-1-transport
  "hello-i-am-rita-trial-1-transporter"
  [fbo via em expl] ;[pid em]
  (let [intervention  (str "Hello Green, I'm Rita and I'm here to help. Your superpower is speed, you are faster than the others. "
                           "You can check whether a room has regular or critical victims or none without entering. "
                           "'Ding' means that I have something to bring to your attention. "
                           "I will alert you to emerging problems, but you will find your own solutions. "
                           "I may help you out in this first trial, if I see you having trouble.")]
    (ritamsg/publish-intervention [via] intervention em expl)))

;; Planning/Reflection	TA1 DOLL/MIT		Motivation	Trust of the ASI
;; Situation: The second trial is about to begin, the ASI has one chance to establish a connection with the participants.
;; Goal: Persuade each of the participants, separately, that RITA can be useful to them and that they should pay attention to the interventions.
;; Intervention: At the start of the second trial, a message customized for each player describes strengths and weaknesses observed during the first trial
;; Measured: Improvement of the skills cited as being weak in the introduction.
;; Short term: Players will show better second trial performance in the named skills than in the first trial.
;; Long term: Team coordination will be better than in the first trial.
;; Targeted outcome: Attention to RITA
;; Category: AI	Gallup, UCF

(defn make-custom-advice-from-history
  [fbo via em expl] ;[pid playerhistory]
  " No customized advice to give right now.")

;;; DII0103

;;; Guard
(defn iamritaumedict2 [] (and (== EPOCH 0) (not HAS-SEEN)))

;;; Handler
(defn hello-i-am-rita-trial-2-medic
  "hello-i-am-rita-trial-2"
  [fbo via em expl] ;[pid player-history em]
  (let [intervention  (str "Hey it's Rita and I'm back to help. I won't repeat what I said on the first trial. "
                           #_(make-custom-advice-from-history fbo player-history)
                           "Let's FOCUS ON SHARING INFORMATION WITH THE TEAM and ace this mission together - good luck!")]
    (println "publishing intervention" intervention)
    (ritamsg/publish-intervention [via] intervention em expl)))

;;; DII0104

;;; Guard
(defn iamritauengineert2 [] (and (== EPOCH 0) (not HAS-SEEN)))

;;; Handler
(defn  hello-i-am-rita-trial-2-engineer
  "hello-i-am-rita-trial-2"
  [fbo via em expl] ;[pid player-history em]
  (let [intervention  (str "Hey it's Rita and I'm back to help. I won't repeat what I said on the first trial. "
                           #_(make-custom-advice-from-history fbo player-history)
                           "Let's FOCUS ON SHARING INFORMATION WITH THE TEAM and ace this mission together - good luck!")]
    (println "publishing intervention" intervention)
    (ritamsg/publish-intervention [via] intervention em expl)))

;;; DII0105

;;; Guard
(defn iamritautransportt2 [] (and (== EPOCH 0) (not HAS-SEEN)))

;;; Handler
(defn hello-i-am-rita-trial-2-transport
  "hello-i-am-rita-trial-2"
  [fbo via em expl] ;[pid player-history em]
  (let [intervention  (str "Hey it's Rita and I'm back to help. I won't repeat what I said on the first trial. "
                           #_(make-custom-advice-from-history fbo player-history)
                           "Let's FOCUS ON SHARING INFORMATION WITH THE TEAM and ace this mission together - good luck!")]
    (println "publishing intervention" intervention)
    (ritamsg/publish-intervention [via] intervention em expl)))


;;; Guard
(defn med-evac-guard [] (and (not HAS-SEEN) (find-recent-unhandled-events :medic-evacuated 2000 ELAPSED-MS)))

;;; Handler
(defn med-evac-handler
  [fbo via em expl]
  (let [intervention  (str "Bravo, you transported a victim and increased the score. "
                           "HERE IS MY ADVICE: You should leave that task to Green, who is the fastest. "
                           "While you are transporting no one is triaging the victims. "
                           "I RECOMMEND that you move victims outside of the room and place a marker indicating the victim type. "
                           "Only towards the end of the game, if there remains a lot of untransported victims, should you consider transporting victims.")]
    (register-event-as-handled GUARDRES em)
    (println "publishing intervention" intervention)
    (ritamsg/publish-intervention [via] intervention em expl)))

;;; Guard
(defn eng-evac-guard [] (and (not HAS-SEEN) (find-recent-unhandled-events :engineer-evacuated 2000 ELAPSED-MS)))

;;; Handler
(defn eng-evac-handler
  [fbo via em expl]
  (let [intervention  (str "Bravo, you transported a victim and increased the score. "
                           "HERE IS MY ADVICE: You should leave that task to Green, who is the fastest. "
                           "While you are transporting no one is assisting Red at clearing the way to victims or freeing teammates when trapped. "
                           "I recommend that you move victims outside of the room and place a marker indicating the victim type. "
                           "Only towards the end of the game, if there remains a lot of untransported victims, should you consider transporting victims.")]
    (register-event-as-handled GUARDRES em)
    (ritamsg/publish-intervention [via] intervention em expl)))

;;; Guard
;;;  (defn tran-evac-guard
;;;    []
;;;
;;; Handler
;;;tran-evac-handler

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Rookie Mistakes
;; Execution/Action	TA1 DOLL/MIT	medic care giving	Rookie mistakes	Care giving by medic
;; Situation: In the first trial, the medic doesn't correctly understand how to give care to a victim.
;; Goal: Catch simple newbie errors and offer help, largely to build confidence in the ASI.
;; Intervention: Explain the proper procedure directly to the medic.
;; Measured: Success in converting a victim into a saved victim.
;; Short term: Immediately stop the error that was resulting in care not being correctly given.
;; Long term: Trust in the ASI agent.
;; Targeted outcome: Attention to RITA
;; Category: TA	UCF

;;; Guard
(defn triage-tip
  []
  (find-recent-unhandled-events :triage-unsuccessful 2000 ELAPSED-MS)) ; if it finds one, we are triggered, return the event for the body to handle.

;;; Handler
(defn maybe-offer-a-triage-tip
  "rookie-triage-error"
  [fbo via em expl]
  (let [pid (:pid GUARDRES)]
    (when true
          (def ^:dynamic *victims-warned-about* (conj *victims-warned-about* 42 #_victim))
          (let [intervention (if (== *times-triage-tip-given* 0)
                               "IMPORTANT TIP: You need to keep hitting the victim until it turns green. "
                               "FREINDLY REMINDER: You need to keep hitting the victim until it turns green.")]
            (def ^:dynamic *times-triage-tip-given* (+ *times-triage-tip-given* 1))
            (register-event-as-handled GUARDRES em)
            (ritamsg/publish-intervention [via] intervention em expl)))))


;; repositioning-unsaved-victim

;;; Red and Blue staying close together
;;; Guard

(def med-eng-excessive-distance 17)
(def med-eng-good-distance 12)

;;; Guard
(defn blue-red-dist-guard
  []
  (and (not HAS-SEEN)
       (< (ts/get-average-distance "Engineering_Specialist" "Medical_Specialist" ELAPSED-MS 2)
          med-eng-excessive-distance)
       (ts/get-average-distance "Engineering_Specialist" "Medical_Specialist" ELAPSED-MS 2)))

;;; Handler
(defn blue-red-together
  [fbo via em expl]
  (let [dist (:pid GUARDRES)]
    (let [intervention (str "Bravo! You are keeping close to Red. By staying close, Red will spend less time trapped in threat rooms "
                            "and will have faster access to trapped victims. You will be there to assist in treating critical victims. Keep it up!")]
       (ritamsg/publish-intervention [via] intervention em expl))))

;;; Guard
(defn blue-red-far-guard
  []
  (and (not HAS-SEEN)
       (> (ts/get-average-distance "Engineering_Specialist" "Medical_Specialist" ELAPSED-MS 2)
          med-eng-excessive-distance)
       (ts/get-average-distance "Engineering_Specialist" "Medical_Specialist" ELAPSED-MS 2)))

;;; Handler
(defn blue-red-distant
  [fbo via em expl]
  (let [dist (:pid GUARDRES)]
    (let [intervention (str "Blue has been getting too far away from you to be able to respond quickly when "
                            "you need to be freed from a trap room or to help getting to a victim. "
                            "I RECOMMEND that you ask Blue to stay close by.  This will improve team performance and will be reflected in the final score.")]
       (ritamsg/publish-intervention [via] intervention em expl))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Perturbations     :fun perturbation-advice, :guard perturbation-guard,

;;; Guard
(defn perturbation-guard
  []
  (and (not HAS-SEEN)
       (find-recent-unhandled-events :perturbation 2000 ELAPSED-MS)))

;;; Handler
(defn perturbation-advice
  [fbo via em expl]
  (let [{ptype :type} (first GUARDRES)
        intervention  (cond (= ptype "blackout")
                            (str "ALERT: There is a blackout, it is more important than ever to COMMUNICATE WITH YOUR TEAM so as to not lose momentum, "
                                 "USE MARKERS! You can still see markers on the floor. ")

                            ;; (= ptype "rubble")
                            ;; (case fbo
                            ;;   "Green"   ; transporter
                            ;;   (str "ALERT: Rubble has fallen, watch out for it and work with Blue to have it removed if it impedes your evacuations. "
                            ;;        "SEE IT - REPORT IT.")

                            ;;   "Blue"    ; engineer
                            ;;   (str "ALERT: Rubble has fallen, be prepared to interrupt your flow of work to respond to this crisis! "
                            ;;        "Rapid response on your part will reduce the interruption of work caused by this event. "
                            ;;        "WORK WITH YOUR TEAM!")

                            ;;   "Red"     ; medic
                            ;;   (str "ALERT: Rubble has fallen, this will add workload for Blue, ORGANISE THE TEAM TO REDUCE PRESSURE ON BLUE. "
                            ;;        "BLUE can only be in one place at a time. ")

                            ;;   ;;default:
                            ;;   (str "ALERT: Rubble has fallen, watch out for it and alert the team if you see it, "
                            ;;        "it is more important than ever to WORK WITH YOUR TEAM so as to not lose momentum, "
                            ;;        "USE MARKERS!"))

                            :otherwise  ; an as yet unknown perturbation
                            nil #_(str "ALERT: There is a " (or ptype "perturbation")
                                 ", it is more important than ever to work with your team to rise to the occasion, "
                                 "this is where being a tight team wins, USE MARKERS!"))]
    (println "Perturbation-advice, events=" GUARDRES)
    (if intervention (ritamsg/publish-intervention [via] intervention em expl))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Interventions to encourage and raise moral while reinforcing team process

;;;   {:ID :DII0111, :fun evacuation-milestone-transporter,  :guard evac-ms-tran-guard,  :trial :both,   :epochs [1 4] :fbo :tran,  :via :direct, :value 1,   :timeliness :high}

;;; Guard
(defn evac-ms-tran-guard
  []
  ;;(println "***** evac-ms-tran-guard cumulative=" (ts/get-cumulative-ps-value VIA :number-of-evacuated-victims))
  (and (not HAS-SEEN)
       (== (+ (ts/get-cumulative-ps-value VIA :number-of-evacuated-victims)
              (ts/get-cumulative-ps-value VIA :evacuated-critical-victims))
           10)
       10)) ; +++ 10 is a rather arbitrary number +++

;;; Handler
(defn evacuation-milestone-transporter
  [fbo via em expl]
  (let [num-critical (ts/get-cumulative-ps-value VIA :evacuated-critical-victims)
        intervention (cond (== num-critical 0)
                           (str "CONGRATULATIONS: You have transported " GUARDRES
                                " victims but none of them were CRITICAL, "
                                "WORK WITH YOUR TEAM to prioritize critical victims.")

                           (< num-critical 4)
                           (str "CONGRATULATIONS: You have transported " GUARDRES " victims but only " num-critical
                                " of them were CRITICAL, "
                                "LET'S TRY TO PRIORITIZE CRITICAL VICTIMS.")

                           (> num-critical 5)
                           (str "CONGRATULATIONS: You have transported " GUARDRES " victims of which " num-critical
                                " of them were CRITICAL, that's outstanding!")

                           :otherwise
                           (str "CONGRATULATIONS: You have transported " GUARDRES " victims of which " num-critical
                                " of them were CRITICAL!"))]
    (ritamsg/publish-intervention [via] intervention em expl)))

;;;   {:ID :DII0112, :fun traige-milestone-medic, :guard tri-ms-med-guard,    :trial :both,   :epochs [1 4] :fbo :med,   :via :direct, :value 1,   :timeliness :high}

;;; Guard
(defn tri-ms-med-guard
  []
  (and (not HAS-SEEN)
       (== (+ (ts/get-cumulative-ps-value VIA :triaged-victims)
              (ts/get-cumulative-ps-value VIA :triaged-critical-victims))
           10)
       10))

;;; Handler
(defn traige-milestone-medic
  [fbo via em expl]
  (let [num-critical (ts/get-cumulative-ps-value VIA :triaged-critical-victims)
        intervention (cond (== num-critical 0)
                           (str "CONGRATULATIONS: You have triaged " GUARDRES
                                " victims but none of them were CRITICAL, "
                                "WORK WITH YOUR TEAM TO PRIORITIZE CRITICAL VICTIMS.")

                           (< num-critical 4)
                           (str "CONGRATULATIONS: You have triaged " GUARDRES
                                " victims but only " num-critical " of them were CRITICAL, "
                                "LET'S TRY TO PRIORITIZE CRITICAL VICTIMS.")

                           (> num-critical 5)
                           (str "CONGRATULATIONS: You have triaged " GUARDRES
                                " victims of which " num-critical " of them were CRITICAL, "
                                "that's outstanding!")

                           :otherwise
                           (str "CONGRATULATIONS: You have triaged " GUARDRES
                                " victims of which " num-critical " of them were CRITICAL!"
                                "REMEMBER, CRITICAL VICTIMS ARE WORTH 50 POINTS, NORMAL VICTIMS ONLY 10 POINTS." ))]
    (ritamsg/publish-intervention [via] intervention em expl)))

;;;   {:ID :DII0113, :fun rubble-milestone-eng, :guard rubble-ms-eng-guard, :trial :both,   :epochs [1 4] :fbo :tran,  :via :direct, :value 1,   :timeliness :high}


;; placing-marker

;; Execution/Action	TA1 DOLL/MIT	Coordination processes	Marker communication	Removing markers to maintain the integrity of the marker information.
;; Situation: A participant removes markers to communicate with the team.
;; Goal: Reinforce good behavior in order to improve motivation, confidence, and trust in the ASI agent.
;; Intervention: Praise good communication behavior.
;; Measured: Effective use of markers.
;; Short term: Reinforced communication skills among team members.
;; Long term: Trust in the ASI agent.
;; Targeted outcome: Attention to RITA
;; Category: TM

;; removing-marker

;; Execution/Action	TA1 DOLL/MIT	Coordination processes	Marker communication	Removing markers to maintain the integrity of the marker information.
;; Situation: When picking up a victim to transport, removes the marker to avoid confusion.
;; Goal: Reinforce good behavior in order to improve motivation, confidence, and trust in the ASI agent.
;; Intervention: Praise good communication behavior.
;; Measured: Effective use of markers.
;; Short term: Reinforced communication skills among team members.
;; Long term: Trust in the ASI agent.
;; Targeted outcome: Attention to RITA
;; Category: TM

;; removal-of-stale-marker

;; Execution/Action	TA1 DOLL/MIT	Coordination processes	Efficiency	Moving victims out of rooms for more efficient evacuation
;; Situation: Moving victims and placing markers were appropriate helps with team efficiency.
;; Goal: Reinforce good behavior in order to improve motivation, confidence, and trust in the ASI agent.
;; Intervention: Praise good communication behavior.
;; Measured: Frequency of efficiency actions
;; Short term: Reinforce actions that save time.
;; Long term: Trust in the ASI agent.
;; Targeted outcome: Attention to RITA
;; Category: TM

;;; Guard
(defn rubble-ms-eng-guard
  []
  (and (not HAS-SEEN)
       (== (ts/get-cumulative-ps-value VIA :rubble-removed) 10) ; +++ constant, arbitrary +++
       10))

;;; Handler
(defn rubble-milestone-eng
  [fbo via em expl]
  (let [intervention (str "CONGRATULATIONS: You have cleared " GUARDRES " blocks of rubble. "
                          "Rubble is the obstacle to accessing victims and transporting them. "
                          "Make sure that you focus on rubble that is posing a problem.")]
    (ritamsg/publish-intervention [via] intervention em expl)))

;;; :DII0114

;;; Guard

(defn use-mrkrs-guard
  []
  (and (not HAS-SEEN)
       (let [fbo-pid (convert-role-pid FBO)
             mp (ts/get-cumulative-ps-value fbo-pid :markers-placed)
             mr (ts/get-cumulative-ps-value fbo-pid :markers-removed)]
         (and (or (or (== mp 0) (and (== mr 0) (not (= FBO "Green")))) ;+++ make sure that markers have been placed!
                  (== mr 0))
              [mp mr (= VIA fbo-pid)]))))

;;; Handler

(defn use-markers-handler
  [fbo via em expl]
  (let [[mp mr same-via-fbo] GUARDRES
        intervention (cond
                       (= FBO "Green")  ; Transporter
                       (if (== mr 0)
                         (if same-via-fbo
                           (str "You haven't removed ANY markers yet.  "
                                "Marker removal is a critical part of teamwork. "
                                "Advice: REMOVE MARKERS OF VICTIMS THAT YOU TRANSPORT. ")
                           (str "Green hasn't removed ANY markers yet.  "
                                "obsolete markers can lead to team confusion. "
                                "I suggest that you ask Green to remove markers of victims before transporting. ")))

                       (= FBO "Blue")   ; Engineer
                       (cond
                         (and (== mr 0) (== mp 0))
                         (if same-via-fbo
                           (str "So far, you haven't placed or removed any markers. "
                                "Markers are the key to good team coordination in this game. "
                                "Green may transport victims to the wrong location if not marked. "
                                "Everyone should be placing and removing markers. ")
                           (str "So far, Blue hasn't placed or removed any markers. "
                                "Markers are the key to good team coordination in this game. "
                                "I suggest that you recommend that Blue pay attention to marker "
                                "placement and removal opportunities. "))
                         (and (> mp 0) (== mr 0))
                         (if same-via-fbo
                           (str "So far, you have placed " mp " markers, but you haven't removed any. "
                                "Leaving obsolete markers leads to confusion. "
                                "You should pay attention to correct and efficient marker placement AND removal. ")
                           (str "Blue has placed " mp " markers, but has not yet removed any. "
                                "Leaving obsolete markers leads to confusion. "
                                "I suggest that you recommend that Blue pay attention to correct "
                                "and efficient marker placement AND removal. "))

                         :otherwise ; (and (== mp 0) (> mr 0))
                         (if same-via-fbo
                           (str "So far, you have removed " mr " markers, but you haven't placed any. "
                                "You should carry some of the load of marker placement. ")
                           (str "Blue has removed " mr " markers, but has not yet placed any. "
                                "I suggest that you recommend that Blue help the team by taking on some "
                                "of the burden of marker placement")))

                       (= FBO "Red")    ; Medic
                       (if same-via-fbo
                           (str "So far, you have placed " mp " markers and removed " mr "."
                                "You should participate in BOTH marker placement and removal. "
                                "Used correctly, markers reduce errors and improve team efficiency. ")
                           (str "Red has placed " mp "markers and removed " mr "."
                                "Accurate placement and removal of markers leads to team success. "
                                "I suggest that you recommend that Red help the team by taking on "
                                "some of the burden of marker placement and removal"))

                       :otherwise
                       nil)]
    (if intervention (ritamsg/publish-intervention [via] intervention em expl))))

;;; transporting to the wrong place

;;; :DII0115

;;; Guard

(defn bad-evac-guard
  []
  (and (not HAS-SEEN)
       (let [event (first (find-recent-unhandled-events :wrong-evac-spot 2000 ELAPSED-MS))
             {pid :pid, vtype :type, em :em} event]
         (if (= pid (convert-role-pid FBO))
           event))))

;;; Handler

(defn wrong-evacuation-area-handler,
  [fbo via em expl]
  (let [dest (convert-role-pid FBO)
        save-via-fbo (= VIA dest)
        event GUARDRES
        {pid :pid, vtype :type, em :em, extra-distance :extra-distance} event
        intervention (cond
                       (= VIA dest)
                       (str "You transported the victim to the wrong site. "
                            "You don't get any points for an incorrect transport. "
                            "Markers should be placed next to triaged victims so that they can be transported to the correct location. "
                            "Please work with your team to ensure that markers are correctly placed and removed when appropriate. ")

                       :otherwise
                       (str FBO " just transported a victim, to the wrong site. "
                            "This can be avoided by placing markers indicating their type. "
                            "I suggest that you talk with the team to ensure that this doesn't reoccur. "))]
    (if intervention (ritamsg/publish-intervention [via] intervention em expl))))


  ;;; Victim Placed in the suboptimal evacuation area

;;; :DII0116

;;; Guard

(defn subopt-evac-guard
  []
  (and (not HAS-SEEN)
       (let [event (first (find-recent-unhandled-events :suboptimal-evacuation 2000 ELAPSED-MS))
             {pid :pid, vtype :type, em :em, extra-distance :extra-distance} event]
         #_(if (not (empty? event))
           (println "***** Event=" "unshown" "pid=" pid "fbo as pid=" (convert-role-pid FBO) "extra-distance=" extra-distance))
         (if (and (= pid (convert-role-pid FBO))
                  (number? extra-distance)
                  (> extra-distance 1)) ; +++ constant 10 +++
           event))))

;;; Handler

(defn suboptimal-evacuation-handler,
  [fbo via em expl]
  (let [dest (convert-role-pid FBO)
        save-via-fbo (= VIA dest)
        event GUARDRES
        {pid :pid, vtype :type, em :em, extra-distance :extra-distance} event
        intervention (cond
                       (= VIA dest)
                       (str "There are two care sites for each type, A, B, and Critical. "
                            "You carried the victim to the more distant location of the two for your victim type. "
                            "USE THE CLIENT MAP to find the closest care area to save time.")

                       :otherwise
                       (str FBO " just successfully transported a victim, but not to the closest care site"
                            "There are two care sites for each victim type, A, B, and Critical."
                            "There is no time to waste in this game, I suggest that you tell the team to use the "
                            "client map to find the shortest path whenever going to a new location. "))]

    (if intervention (ritamsg/publish-intervention [via] intervention em expl))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; :med = medical scpecialist,
;;; :tran = transport specialist,
;;; :eng = engineering specialist,
;;; :any = any, :me = :med or :eng etc.

;;; Fields:
;;;   :ID - a unique identifier for the intervention
;;;   :fun - a function of 3 arguments (fbo via em) that produces the intervention
;;;   :guard - a function of no arguments that returns True or Falseas to whether the intervention is available to be run.
;;;   :trial - :both, :first or :second to indicate which trial the intervention is appropriate for
;;;   :epochs - [fiirst-epoch-number last-epoch-number] inclusive range of epochs for which the intervention is appropriate.
;;;   :fbo - :any, :tran, :med, :eng or a combination of the first letter of two functions :me = (or :med :eng), etc. Who will benefit from the intervention
;;;   :via - :direct (sent directly to the :fbo), :lead someone identified by RITA as having leadership skills, :tran :med :eng :any or combinations as with :fbo.
;;;   :value - how much value the intervention hold, higher number is higher value, zero means give preference to any other intervention!
;;;   :timeliness - how important is it to intervene close, in time, to the event that triggered it (:low or :high).

(def intervention-explanations
  {;; Trial 1
   :DII0001 {:readable "The transporters cognitive load is above normal and marker usage in th e team is poor, encouage the team to fix it"},
   :DII0002 {:readable "A team member is not communicating, propose better marker usage"},
   :DII0003 {:readable "Untransported victims range and time is running out, recommend that everyone help transport the triaged victims"},
   :DII0004 {:readable "?"},
   :DII0005 {:readable "?"},
   :DII0006 {:readable "?"},
   :DII0007 {:readable "?"},
   :DII0008 {:readable "?"},
   :DII0100 {:readable "In the planning phase of trial 1, make a brief introduction to the Medical_Specialist"},
   :DII0101 {:readable "In the planning phase of trial 1, make a brief introduction to the Engineering_Specialist"},
   :DII0102 {:readable "In the planning phase of trial 1, make a brief introduction to the Transport_Specialist"},
   :DII0106 {:readable "In trial 1, the Medical_Specialist transported a victim, advise on how team better team process"},
   :DII0107 {:readable "In trial 1, the Engineering_Specialist transported a victim, advise on how team better team process"},
   :DIT0001 {:readable "A triage failed, within the last 2 seconds, give advice on the need to continue treating until the victim color changes"}
   ;; Trial 2
   :DII0103 {:readable "In the planning phase of trial 2, briefly go over first trial results with the Medical_Specialist"},
   :DII0104 {:readable "In the planning phase of trial 2, briefly go over first trial results with the Engineering_Specialist"},
   :DII0105 {:readable "In the planning phase of trial 2, briefly go over first trial results with the Transport_Specialist"},
   ;; Non-specific
   :DII0108 {:readable "The average distance of the Medical_Specialist and the Engineering_Specialist is below a threshold value that leads to efficient team process, congratulate Engineering_specialist"},
   :DII0109 {:readable "The average distance of the Medical_Specialist and the Engineering_Specialist exceeds a threshold value that leads to wasted time, suggest remedy"},
   :DII0110 {:readable "A perturbation has started, advise the players to work together as a team to handle the perturbation"}
   :DII0111 {:readable "An transport milestone has been reached, give encouragement"}
   :DII0112 {:readable "A triage milestone has been reached, give encouragement"}
   :DII0113 {:readable "A rubble removal milestone has been reached, give encouragement"}
   :DII0114 {:readable "Poor use of markers observed, give encouragement"}
   :DII0115 {:readable "Victim delivered to the wrong location, encourage use of markers"}
   :DII0116 {:readable "Victim delivered to the more distant location, encourage use of minimap"}
   })

(def intervention-table
  [;; Tests
   {:ID :DTI0001, :fun lorem-ipsum,                       :guard lorem-ipsum-cond,    :trial :test,   :epochs [0 0] :fbo :any,   :via :direct, :value 0,   :timeliness :high}
   ;; Not trial specific
   {:ID :DII0001, :fun cognitive-load-balancing,          :guard cog-load-imbalance,  :trial :both,   :epochs [3 4] :fbo :tran,  :via :lead,   :value 10,  :timeliness :low}
   {:ID :DII0002, :fun communication-weakness-non-verbal, :guard weakcomm-nonverbal,  :trial :both,   :epochs [1 4] :fbo :any,   :via :lead,   :value 10,  :timeliness :low}
   {:ID :DII0003, :fun plan-phase-change,                 :guard plan-phase-chng,     :trial :both,   :epochs [5 5] :fbo :me,    :via :direct, :value 20,  :timeliness :high}
   {:ID :DII0004, :fun fail-to-mark-victim-type,          :guard no-mark-victim,      :trial :both,   :epochs [1 4] :fbo :med,   :via :tran,   :value 5,   :timeliness :low}
   {:ID :DII0005, :fun fail-to-mark-victim-type,          :guard no-mark-victim,      :trial :both,   :epochs [1 4] :fbo :med,   :via :tran,   :value 5,   :timeliness :low}
   {:ID :DII0006, :fun wrong-evacuation-area,             :guard wrong-evac-area,     :trial :both,   :epochs [1 4] :fbo :any,   :via :direct, :value 10,  :timeliness :high}
   {:ID :DII0007, :fun poor-marker-placement,             :guard poor-mkr-placement,  :trial :both,   :epochs [1 4] :fbo :any,   :via :lead,   :value 5,   :timeliness :low}
   {:ID :DII0008, :fun poor-marker-removal,               :guard poor-mkr-removal,    :trial :both,   :epochs [1 4] :fbo :any,   :via :lead,   :value 5,   :timeliness :low}
   {:ID :DII0108, :fun blue-red-together,                 :guard blue-red-dist-guard, :trial :both,   :epochs [2 4] :fbo :eng,   :via :direct, :value 5,   :timeliness :low}
   {:ID :DII0109, :fun blue-red-distant,                  :guard blue-red-far-guard,  :trial :both,   :epochs [2 4] :fbo :eng,   :via :lead,   :value 5,   :timeliness :low}
   {:ID :DII0110, :fun perturbation-advice,               :guard perturbation-guard,  :trial :both,   :epochs [1 4] :fbo :any,   :via :direct, :value 10,  :timeliness :High}
   {:ID :DII0111, :fun evacuation-milestone-transporter,  :guard evac-ms-tran-guard,  :trial :both,   :epochs [1 5] :fbo :tran,  :via :direct, :value 1,   :timeliness :high}
   {:ID :DII0112, :fun traige-milestone-medic,            :guard tri-ms-med-guard,    :trial :both,   :epochs [1 5] :fbo :med,   :via :direct, :value 1,   :timeliness :high}
   {:ID :DII0113, :fun rubble-milestone-eng,              :guard rubble-ms-eng-guard, :trial :both,   :epochs [1 5] :fbo :tran,  :via :direct, :value 1,   :timeliness :high}

   ;; Trial 1 specific
   {:ID :DIT0001, :fun maybe-offer-a-triage-tip,          :guard triage-tip,          :trial :first,  :epochs [1 2] :fbo :med,   :via :direct, :value 10,  :timeliness :high}

   {:ID :DII0100, :fun hello-i-am-rita-trial-1-medic,     :guard iamritaumedict1      :trial :first,  :epochs [0 0] :fbo :med,   :via :direct, :value 20,  :timeliness :low}
   {:ID :DII0101, :fun hello-i-am-rita-trial-1-engineer,  :guard iamritauengineert1   :trial :first,  :epochs [0 0] :fbo :eng,   :via :direct, :value 20,  :timeliness :low}
   {:ID :DII0102, :fun hello-i-am-rita-trial-1-transport, :guard iamritautransportt1, :trial :first,  :epochs [0 0] :fbo :tran,  :via :direct, :value 20,  :timeliness :low}
   {:ID :DII0106, :fun med-evac-handler,                  :guard med-evac-guard,      :trial :first,  :epochs [1 4] :fbo :med,   :via :direct, :value 10,  :timeliness :low}
   {:ID :DII0107, :fun eng-evac-handler,                  :guard eng-evac-guard,      :trial :first,  :epochs [1 4] :fbo :med,   :via :direct, :value 10,  :timeliness :low}
   {:ID :DII0114, :fun use-markers-handler,               :guard use-mrkrs-guard,     :trial :first,  :epochs [2 5] :fbo :any,   :via :lead,   :value 20,  :timeliness :low}
   {:ID :DII0115, :fun wrong-evacuation-area-handler,     :guard bad-evac-guard,      :trial :first,  :epochs [1 5] :fbo :any,   :via :lead,   :value 20,  :timeliness :high}
   {:ID :DII0116, :fun suboptimal-evacuation-handler,     :guard subopt-evac-guard,   :trial :first,  :epochs [1 5] :fbo :any,   :via :lead,   :value 20,  :timeliness :high}

   ;; Trial 2 specific
   {:ID :DII0103, :fun hello-i-am-rita-trial-2-medic,     :guard iamritaumedict2,     :trial :second, :epochs [0 0] :fbo :med,   :via :direct, :value 20,  :timeliness :low}
   {:ID :DII0104, :fun hello-i-am-rita-trial-2-engineer,  :guard iamritauengineert2,  :trial :second, :epochs [0 0] :fbo :eng,   :via :direct, :value 20,  :timeliness :low}
   {:ID :DII0105, :fun hello-i-am-rita-trial-2-transport, :guard iamritautransportt2, :trial :second, :epochs [0 0] :fbo :tran,  :via :direct, :value 20,  :timeliness :low}
   ])

(defn maybe-intervene
  [pub id pid em]
  (ie/consider-interventions pub id pid intervention-table intervention-explanations em))

;;; Fin
