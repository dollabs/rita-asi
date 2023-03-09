;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.players
  "Observations about changes to players states."
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
            ;;[rita.state-estimation.rlbotif :as rlbotif]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.teamstrength :as ts]
            [rita.state-estimation.ras :as ras]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.rita-se-core :as rsc :refer :all] ; back off from refer all +++
            [rita.state-estimation.cognitiveload :as cogload]
            [rita.state-estimation.interventions :as intervene]
            [rita.state-estimation.interventionengine :as ie]
            [rita.state-estimation.victims :as victims]
            [rita.state-estimation.study2 :as study2]
            [rita.state-estimation.study3 :as study3]
            [rita.state-estimation.predictions :as predict]
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

#_(in-ns 'rita.state-estimation.players)


;;; "Event:PlayerFrozenStateChange"

;;; sub_type: Event:PlayerFrozenStateChange
;;; tbm=
;;; {:data {:mission_timer 1 : 44,
;;;         :elapsed_milliseconds 799847.0,
;;;         :playername Player724,
;;;         :player_x -2172.0,
;;;         :player_y 60.0,
;;;         :player_z 39.0,
;;;         :state_changed_to UNFROZEN, ; or FROZEN
;;;         :medic_playername Player818},
;;;  :header {:timestamp 2021-06-29T21:10:51.500Z,
;;;           :message_type event,
;;;           :version 0.6},
;;;  :msg {:experiment_id bc50aea2-b913-4889-9fbd-f4ba2a72763c,
;;;        :trial_id 87da6996-7642-49bb-b114-8e428c87b5de,
;;;        :timestamp 2021-06-29T21:10:51.500Z,
;;;        :source simulator,
;;;        :sub_type Event:PlayerFrozenStateChange,
;;;        :version 1.0}}

(defn rita-handle-PlayerFrozenStateChange-message
  [tbm tb-version]
  (let [data (:data tbm)
        header (:header tbm)
        msg (:msg tbm)
        {em :elapsed_milliseconds
         player_x :player_x
         player_y :player_y
         player_z :player_z
         new-state :state_changed_to
         medic_player_name :medic_player_name} data
        pid (seglob/get-participant-id-from-data data)
        mid (if medic_player_name (seglob/get-participant-id-from-player-name medic_player_name))]
    (if em (seglob/update-last-ms-time em))
    (when (dplev :all) (println "Participant " pid new-state "medic-participant" mid "at x=" player_x "y=" player_y "z" player_z))
    (cond (= new-state "FROZEN")
          (let [pubs (predict/predict-story nil "agentBeliefState.rita" :frozen pid (- em 100))
                ppubs (predict/predict-coms pubs "agentBeliefState.rita" :help pid (+ em 100))
                pppubs (predict/predict-coms ppubs "agentBeliefState.rita" :helper-response pid (+ em 300))
                ppppubs (predict/predict-story-event pppubs "agentBeliefState.rita" :helper-gives-care pid (+ em 500))
                ]
            ppppubs)

          (= new-state "UNFROZEN")
          (do
            (predict/pop-story :frozen)
            (predict/match-prediction "agentBeliefState.rita" :PM2-story-event-prediction (predict/translate-sid :frozen))
            nil))))

;; message_type= status
;; sub_type= Status:PlayerName
;; tbm= {:data
;;        {:playername Player342},
;;       :header {:timestamp 2022-03-22T15:31:53.865Z,
;;                :message_type status,
;;                :version 1.1},
;;       :msg {:experiment_id 00000000-0000-0000-0000-000000000000,
;;             :trial_id 00000000-0000-0000-0000-000000000000,
;;             :timestamp 2022-03-22T15:31:53.865Z,
;;             :source simulator,
;;             :sub_type Status:PlayerName,
;;             :version 0.5}}

(defn rita-handle-PlayerName-message
  [tbm tb-version]
  nil)

;;;      "Event:PlayerSwinging"
;; sub_type: Event:PlayerSwinging tbm= {:data {:mission_timer 10 : 3,
;;                                             :playername ASIST5,
;;                                             :swinging true},
;;                                      :header {:timestamp 2020-08-19T23:14:36.627Z,
;;                                               :message_type event,
;;                                               :version 1.0},
;;                                      :msg {:experiment_id e0b770fa-d726-4c7e-8406-728dcfaf8b98,
;;                                            :trial_id b66aa7e5-a16c-4abc-a2cb-5ae3bdc675e5,
;;                                            :timestamp 2020-08-19T23:14:36.628Z,
;;                                            :source simulator,
;;                                            :sub_type Event:PlayerSwinging,
;;                                            :version 0.5}}
(defn rita-handle-player-swinging-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {mission_timer :mission_timer
         name :name ; Player396
         playername :playername
         swinging :swinging} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))
        id (rsc/get-player-id-from-participant-id (keyword pid))]
    (set-field-value! id 'activity (if swinging :swinging :stationary))
    (when (dplev :action :all) (println "Participant" pid (if swinging "swinging" "not swinging")))
    nil))

(defn rita-handle-event-item-used-message
  [tbm tb-version]
  nil) ;+++ where did this go ?
;;; {"msg":{"trial_id":"3cf9680a-7033-46b8-9846-220aade68e7f",
;;;         "data":{"equippeditemname":"minecraft:lead",
;;;                 "playername":"Player148"},
;;;         "sub_type":"Event:ItemEquipped",
;;;         "source":"simulator",
;;;         "version":"0.4"},
;;;  "header":{"message_type":"event",
;;;            "version":"0.4",
;;;            "timestamp":"2020-06-04T23:45:24.767Z"}}

;; {"msg":{"trial_id":"3a5266e9-f9ea-4020-86e3-0f295cc11f85",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:ItemEquipped",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-19T12:25:39.449Z"},
;;  "data":{"equippeditemname":"minecraft:potion",
;;          "playername":"Player96"},
;;  "header":{"message_type":"event","version":"0.5","timestamp":"2020-06-19T12:25:39.449Z"}}

(defn pretty-tool-name
  [ugly-tool-name]
  (case ugly-tool-name
    "minecraft:lead"         "Dog Lead"
    "minecraft:glass_bottle" "Glass Bottle"
    "minecraft:potion"       "First Aid Box" ; "potion"
    "minecraft:air"          "Nothing"       ; "air"
                             ugly-tool-name))

(defn rita-handle-ItemEquipped-message
  [tbm tbversion]
  (let [message (if (= tbversion :tb4) (:data (:msg tbm)) (:data tbm))
        {playername :playername         ; "Player148"
         equippeditemname :equippeditemname} message
        pid (seglob/get-participant-id-from-data message)
        player (get-player-from-name pid)]
    (when (dplev :tool :all) (println "Player" pid "has selected the tool:"
             (pretty-tool-name equippeditemname)))
    (register-selected-tool pid equippeditemname)
    (ritamsg/add-bs-change-message
     []
     {:subject pid
      :changed :action
      :values {:subject pid
               :action :select-tool
               :object (pretty-tool-name equippeditemname)}
      :agent-belief 1.0
      })))


;;;       "Event:ItemUsed"
;; sub_type: Event:ItemUsed tbm= {:data {:mission_timer 8 : 59,
;;                                       :playername ASIST5,
;;                                       :itemname asistmod:item_unpause_cookie,
;;                                       :item_x -2055.0,
;;                                       :item_y 60.0,
;;                                       :item_z 152.0},
;;                                :header {:timestamp 2020-08-19T23:17:19.857Z,
;;                                         :message_type event,
;;                                         :version 1.0},
;;                                :msg {:experiment_id e0b770fa-d726-4c7e-8406-728dcfaf8b98,
;;                                      :trial_id b66aa7e5-a16c-4abc-a2cb-5ae3bdc675e5,
;;                                      :timestamp 2020-08-19T23:17:19.858Z,
;;                                      :source simulator,
;;                                      :sub_type Event:ItemUsed,
;;                                      :version 0.5}}

(defn rita-handle-event-item-used-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {mission-timer :mission_timer
         playername :playername
         itemname :itemname
         x :item_x
         y :item_y
         z :item_z} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))
        time-remaining (ras/parse-mission-time mission-timer)]
    (when (dplev :tool :all) (println "Used item: " itemname "at [" x y z "] mission timer=" mission-timer))
    ;; Do nothing with this data for now
    nil))

(defn all-participants-assigned-initial-roles?
  []
  (when (>= (count (keys (seglob/get-roles-assigned))) 3)
    (seglob/set-team-members! (into [] (keys (seglob/get-roles-assigned))))
    (println "Team members: " (seglob/get-team-members)  "All roles selected: " (seglob/get-roles-assigned))
    true))

;;; Known roles:
;;;   :new_role Search_Specialist
;;;   :new_role Medical_Specialist
;;;   :new_role Hazardous_Material_Specialist

;;;                 "Event:RoleSelected"
;; {:data {:mission_timer 10 : 0,
;;         :playername WoodenHorse9773,
;;         :new_role Search_Specialist,
;;         :elapsed_milliseconds 303849.0,
;;         :prev_role Hazardous_Material_Specialist},
;;  :header {:version 1.1,
;;           :message_type event,
;;           :timestamp 2021-03-13T00:38:18.727Z},
;;  :msg {:experiment_id 3c80b33a-330b-4912-8b4c-7fe078292352,
;;        :trial_id a13c7181-4966-4ecb-81a4-784ea4b66008,
;;        :source simulator,
;;        :timestamp 2021-03-13T00:38:18.727Z,
;;        :sub_type Event:RoleSelected,
;;        :version 0.5}}

(defn rita-handle-RoleSelected-message
  [tbm tb-version]
  (let [old-num-roles (seglob/num-roles)
        {trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {playername :playername,
         name :name,
         elapsed_milliseconds :elapsed_milliseconds,
         new_role :new_role,
         prev_role :prev_role} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))
        ;;id (rsc/get-player-id-from-participant-id (keyword playername))
        ]
    (if elapsed_milliseconds (seglob/update-last-ms-time elapsed_milliseconds))
    (when (and new_role true) ;(not (seglob/get-assigned-role pid)))
      (seglob/assign-role! pid new_role)) ; Capture only the initial binding.

    (rsc/set-current-role! pid new_role elapsed_milliseconds) ; Record latest role if role changes occur during trial

    (when (dplev :io :all) (println "Player" pid "Selected role" new_role "was" prev_role))
    (if (and (= (seglob/num-roles) 3) (< old-num-roles 3))
      ;; We have just reached full roll assignment
      (do (study2/establish-strategy (rsc/establish-role-profile))
          (let [pubs (predict/predict-final-score nil "agentBeliefState.rita" (/ elapsed_milliseconds 1000.0) elapsed_milliseconds)
                story (if (all-participants-assigned-initial-roles?) (apply slearn/role-strategy (vals (seglob/get-roles-assigned))) nil)
                _ (when (dplev :story :all) (println "story id=" story))
                ppubs (if story (predict/predict-story pubs "agentBeliefState.rita" story (- elapsed_milliseconds 100) pubs))]
            ppubs)))))

;; {"msg":{"trial_id":"a35aea6b-49aa-4d6e-9c0c-3f1e650e560a",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:PlayerJumped",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-12T00:04:28.895Z"},
;;  "data":{"item_z":185,
;;          "item_y":52,
;;          "item_x":-2134,
;;          "playername":"Player487"},
;;  "header":{"message_type":"event","version":"0.5","timestamp":"2020-06-12T00:04:28.893Z"}}

(defn rita-handle-player-jumped-message
  [tbm tbversion]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {item-x :item_x
         item-y :item_y
         item-z :item_z
         name :name ; Player396
         playername :playername} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))
        id (rsc/get-player-id-from-participant-id (keyword pid))]
    ;;(set-field-value! id 'activity :jumping)
    (when (dplev :all) (println "Player" pid "jumped  at [" item-x item-y item-z "]"))))

;; {"msg":{"trial_id":"3a5266e9-f9ea-4020-86e3-0f295cc11f85",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:PlayerSprinting",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-19T12:24:37.380Z"},
;;  "data":{"sprinting":true,
;;          "playername":"Player96"},
;;  "header":{"message_type":"event",
;;            "version":"0.5",
;;            "timestamp":"2020-06-19T12:24:37.379Z"}}

(defn rita-handle-player-sprinting-message
  [tbm tbversion]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {sprinting :sprinting
         name :name ; Player396
         playername :playername} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))
        id (rsc/get-player-id-from-participant-id (keyword playername))]
    (set-field-value! id 'activity (if sprinting :sprinting :walking))
    (when (dplev :all) (println "Player" pid (if sprinting "is" "is not") "sprinting"))))

;;; {"msg":{"data":{"action":"Victim Rescued",  !!! TB4 has action, not TB5, tb4 has data in msg
;;;                 "playername":"Player757",
;;;                 "victim_x":-2148,
;;;                 "victim_y":52,
;;;                 "victim_z":160},
;;;         "sub_type":"Event:Triage",
;;;         "source":"simulator",
;;;         "version":"0.4",
;;;         "timestamp":"2020-06-10T23:39:12.193Z"},
;;;  "header":{"message_type":"event",
;;;            "version":"0.4",
;;;            "timestamp":"2020-06-10T23:39:12.193Z"}}

;;; {"msg":{"trial_id":"3cf9680a-7033-46b8-9846-220aade68e7f",
;;;         "data":{"action":"Triage In Progress",
;;;                 "playername":"Player148",
;;;                 "victim_x":-2148,
;;;                 "victim_y":52,
;;;                 "victim_z":160},
;;;         "sub_type":"Event:Triage",
;;;         "source":"simulator",
;;;         "version":"0.4"},
;;;  "header":{"message_type":"event",
;;;            "version":"0.4",
;;;            "timestamp":"2020-06-04T23:45:52.207Z"}}

;; {"msg":{"trial_id":"3a5266e9-f9ea-4020-86e3-0f295cc11f85",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:Triage",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-19T12:25:39.895Z"},
;;  "data":{"color":"Yellow",                       !!! not always present
;;          "playername":"Player96",
;;          "victim_x":-2029,
;;          "victim_y":60,
;;          "triage_state":"IN_PROGRESS",           !!! TB5 replaces action with triage_state
;;          "victim_z":176},
;;  "header":{"message_type":"event","version":"0.5","timestamp":"2020-06-19T12:25:39.895Z"}}

;; {"msg":{"trial_id":"a35aea6b-49aa-4d6e-9c0c-3f1e650e560a",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:Triage",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-12T00:03:29.447Z"},
;;  "data":{"playername":"Player487",               !!! Notice, no "color" even in TB5
;;          "victim_x":-2148,
;;          "victim_y":52,
;;          "triage_state":"IN_PROGRESS",
;;          "victim_z":180},
;;  "header":{"message_type":"event","version":"0.5","timestamp":"2020-06-12T00:03:29.445Z"}}

;; {"msg":{"trial_id":"a35aea6b-49aa-4d6e-9c0c-3f1e650e560a",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:Triage",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-12T00:03:44.442Z"},
;;  "data":{"playername":"Player487",
;;          "victim_x":-2148,
;;          "victim_y":52,
;;          "triage_state":"SUCCESSFUL",
;;          "victim_z":180},
;;  "header":{"message_type":"event","version":"0.5","timestamp":"2020-06-12T00:03:44.442Z"}}


(defn rita-handle-Triage-message
  [tbm tbversion]
  (let [message (if (= tbversion :tb4) (:data (:msg tbm)) (:data tbm))
        {action :action                 ; tb4: "Triage In Progress", "Victim Rescued"
         triage-state :triage_state     ; tb5: "IN_PROGRESS", "SUCCESSFUL"
         name :name ; Player396
         playername :playername         ; "Player148"
         elapsed-milliseconds :elapsed_milliseconds
         type :type
         victim-x :victim_x
         victim-y :victim_y
         victim-z :victim_z} message
        pid (seglob/get-participant-id-from-data message)
        id (rsc/get-player-id-from-participant-id (keyword pid))
        victim-status (case tbversion
                        :tb4 action
                        :tb5 triage-state
                        triage-state)
        player (get-player-from-name pid)
        ;;+++ if victim not found, add one
        victim (get-victim-from-coordinates victim-x victim-z victim-y)
        color (victims/color-of-victim victim)]
    (if elapsed-milliseconds (seglob/update-last-ms-time elapsed-milliseconds))
    (when (dplev :triage :all)
      (println "Triage pid:" pid
               "status=" victim-status
               "action=" action
               "victim=" (and victim (global/RTobject-variable victim))))
    (cond
      (or (= victim-status "Triage In Progress") ; TB4 version
          (= victim-status "IN_PROGRESS"))       ; TB5 version
      (do
        (when (dplev :triage :all) (println "Player" pid "triaging a" color
                 "victim @[" victim-x victim-y victim-z "]"))
        (if victim
          (do
            (register-victim-triage victim pid :triage-started)
            (predict/match-prediction id :triage-victim (global/RTobject-variable victim)))))

      (or (= victim-status "Victim Rescued") ; TB4 version
          (= victim-status "SUCCESSFUL"))    ; TB5 version
      (do
        (if (or (= type "victim_c") (= type "victim_saved_c"))
          (do
            (ts/increment-strength-data-for pid (seglob/get-trial-number) elapsed-milliseconds :triaged-critical-victims)
            (seglob/inc-victims-triaged))
          (do
            (ts/increment-strength-data-for pid (seglob/get-trial-number) elapsed-milliseconds :triaged-victims)
            (seglob/inc-critical-victims-triaged)))
        (when (dplev :triage :all) (println "Player" pid "rescued a" color
                 "victim @[" victim-x victim-y victim-z "]"))
        (if victim
          (let []
            (register-victim-triage victim pid :victim-rescued)
            (predict/match-prediction id :triage-victim (global/RTobject-variable victim))
            (predict/player-triaged id color)
            (when (dplev :triage :all) (println "Player triaged a" (or color type) "victim (" (global/RTobject-variable victim) ")"))
            (ritamsg/add-bs-change-message
             []
             {:subject pid
              :changed :action
              :values {:subject pid
                       :action :triage-victim
                       :object (get-object-vname victim)}
              :agent-belief 1.0
              }))))

      (= victim-status "UNSUCCESSFUL")
      (ie/register-event {:event-key :triage-unsuccessful :em elapsed-milliseconds :pid pid :victim victim}) ; maybe-offer-a-triage-tip

      :otherwise
      (when (dplev :triage :all :unhandled) (println "Unhandled Event:Triage: " victim-status)))))

;;; {"msg":{"trial_id":"3cf9680a-7033-46b8-9846-220aade68e7f",
;;;         "data":{"powered":true,
;;;                 "playername":"Player148",
;;;                 "lever_x":-2151,
;;;                 "lever_y":53,
;;;                 "lever_z":177},
;;;         "sub_type":"Event:Lever",
;;;         "source":"simulator",
;;;         "version":"0.4"},
;;;  "header":{"message_type":"event",
;;;            "version":"0.4",
;;;            "timestamp":"2020-06-04T23:45:36.811Z"}}

;; {"msg":{"trial_id":"a35aea6b-49aa-4d6e-9c0c-3f1e650e560a",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:Lever",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-12T00:03:16.795Z"},
;;  "data":{"powered":true,
;;          "playername":"Player487",
;;          "lever_x":-2151,
;;          "lever_y":53,
;;          "lever_z":174},
;;  "header":{"message_type":"event","version":"0.5","timestamp":"2020-06-12T00:03:16.794Z"}}

(defn rita-handle-Lever-message
  [tbm tbversion]
  (let [message (if (= tbversion :tb4) (:data (:msg tbm)) (:data tbm))
        {name :name ; Player396
         playername :playername         ; "Player148"
         powered :powered
         lever-x :lever_x
         lever-y :lever_y
         lever-z :lever_z} message
        pid (seglob/get-participant-id-from-data message)
        id (rsc/get-player-id-from-participant-id (keyword pid))
        player (get-player-from-name pid)
        lever (get-lever-from-coordinates lever-x lever-z lever-y)]
    (when (dplev :all) (println "Player" pid "turned" (if powered "on" "off")
             "lever @[" lever-x lever-y lever-z "]"))
    (if lever
      (do
        (register-lever-state lever pid powered)
        (predict/match-prediction id :set-switch-state (get-object-vname lever))))))

;;; {"msg":{"trial_id":"3cf9680a-7033-46b8-9846-220aade68e7f",
;;;         "data":{"door_x":-2153,
;;;                 "playername":"Player148",
;;;                 "door_y":53,
;;;                 "door_z":175,
;;;                 "open":true},
;;;         "sub_type":"Event:Door",
;;;         "source":"simulator",
;;;         "version":"0.4"},
;;;  "header":{"message_type":"event",
;;;            "version":"0.4",
;;;            "timestamp":"2020-06-04T23:45:34.605Z"}}

;; {"msg":{"trial_id":"3a5266e9-f9ea-4020-86e3-0f295cc11f85",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:Door",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-19T12:24:48.398Z"},
;;  "data":{"door_x":-2054,
;;          "playername":"Player96",
;;          "door_y":61,
;;          "door_z":183,
;;          "open":true},
;;  "header":{"message_type":"event","version":"0.5","timestamp":"2020-06-19T12:24:48.398Z"}}

(defn rita-handle-Door-message
  [tbm tbversion]
  (let [message (if (= tbversion :tb4) (:data (:msg tbm)) (:data tbm))
        {name :name ; Player396
         playername :playername         ; "Player148"
         open :open
         door-x :door_x
         door-y :door_y
         door-z :door_z} message
        pid (seglob/get-participant-id-from-data message)
        id (rsc/get-player-id-from-participant-id (keyword pid))
        player (get-player-from-name pid)
        door (get-door-from-coordinates door-x door-z door-y)]
    (when (dplev :all) (println "Player" pid (if open "opened" "closed")
             "door @[" door-x door-y door-z "]=" (if door (get-object-vname door))))
    (if door                            ; in case door wasn't found
      (do
        (register-door-state door pid open)
        (predict/match-prediction id :open (get-object-vname door))
        (ritamsg/add-bs-change-message
         []
         {:subject pid
          :changed :action
          :values {:subject pid
                   :action (if open 'open 'close)
                   :object (get-object-vname door)}
          :agent-belief 1.0
          })))))

;; {"msg":{"trial_id":"3a5266e9-f9ea-4020-86e3-0f295cc11f85",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:ItemDrop",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-19T12:21:48.512Z"},
;;  "data":{"item_z":147,"item_y":61,"item_x":-2150,
;;          "itemname":"minecraft:potion",
;;          "playername":"Player96"},
;;  "header":{"message_type":"event",
;;            "version":"0.5",
;;            "timestamp":"2020-06-19T12:21:48.511Z"}}

(defn rita-handle-ItemDropped-message
  [tbm tbversion]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {item-x :item_x
         item-y :item_y
         item-z :item_z
         itemname :itemname
         playername :playername} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))]
    (when (dplev :action :all) (println "Player" pid "dropped" itemname "at [" item-x item-y item-z "]"))))

;;; testbed message sub_type: Event:ToolUsed tbm=
;; {:data {:target_block_type asistmod:block_victim_1,
;;         :tool_type MEDKIT,
;;         :target_block_z -7.0,
;;         :target_block_x -2222.0,
;;         :target_block_y 60.0,
;;         :elapsed_milliseconds 194947.0,
;;         :mission_timer 11 : 49,
;;         :durability 20.0,
;;         :count 1.0,
;;         :playername ASIST4},
;;  :header {:version 1.1,
;;           :message_type event,
;;           :timestamp 2021-03-13T00:36:29.825Z}, :msg
;;  {:experiment_id 3c80b33a-330b-4912-8b4c-7fe078292352,
;;   :trial_id a13c7181-4966-4ecb-81a4-784ea4b66008,
;;   :source simulator,
;;   :timestamp 2021-03-13T00:36:29.825Z,
;;   :sub_type Event:ToolUsed,
;;   :version 1.0}}

;;  :tool_type MEDKIT
;;  :tool_type HAMMER

;; "Event:ToolUsed"

(defn rita-handle-ToolUsed-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {playername :playername
         target_block_type :target_block_type
         tool_type :tool_type} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))]
    (when (dplev :action :all) (println "Player" pid "has used" tool_type "on" target_block_type))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Ground truth

;; sub_type= Mission:RoleText
;; tbm= {:data
;;        {:missionName Saturn_A_Blackout,
;;         :medical_specialist_text [Some test information],
;;         :engineering_specialist_text [Some test information],
;;         :transport_specialist_text [Some test information]},
;;       :header {:timestamp 2022-03-22T15:44:21.256Z,
;;                :message_type groundtruth,
;;                :version 2.0},
;;       :msg {:experiment_id 4b3dc21b-bf17-4368-b1f7-ef63fa8f17aa,
;;             :trial_id a3896ac5-a01f-4874-8bf8-f221140469ba,
;;             :timestamp 2022-03-22T15:44:21.256Z,
;;             :source simulator,
;;             :sub_type Mission:RoleText,
;;             :version 2.1}}

(defn rita-handle-RoleText-message
  [tbm tb-version]
  nil)


;;; Fin
