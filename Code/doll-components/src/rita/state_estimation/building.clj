;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.building
  "Observations about changes to the building states."
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
            [rita.state-estimation.volumes :as vol :refer :all]
            ;[rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            ;;[rita.state-estimation.rlbotif :as rlbotif]
            [rita.state-estimation.interventionengine :as ie]
            [rita.state-estimation.teamstrength :as ts]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.rita-se-core :as rsc :refer :all] ; back off from refer all +++
            [rita.state-estimation.cognitiveload :as cogload]
            [rita.state-estimation.interventions :as intervene]
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

#_(in-ns 'rita.state-estimation.building)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Rubble and Blockages

;;; Blockages

;;; message_type= event sub_type= Event:RubbleCollapse
;;; tbm= {:data
;;;        {:participant_id x202203021,
;;;         :toBlock_y 60.0,
;;;         :elapsed_milliseconds 104741.0,
;;;         :triggerLocation_y 59.0,
;;;         :mission_timer 13 : 19,
;;;         :triggerLocation_z 42.0,
;;;         :fromBlock_x -2137.0,
;;;         :toBlock_x -2137.0,
;;;         :triggerLocation_x -2138.0,
;;;         :playername Aptiminer1,
;;;         :fromBlock_y 60.0,
;;;         :toBlock_z 47.0,
;;;         :fromBlock_z 47.0},
;;;       :header {
;;;         :version 1.1,
;;;         :message_type event,
;;;         :timestamp 2022-03-02T18:37:07.997Z},
;;;       :msg {
;;;         :trial_id 55cd3a31-548a-4d5b-9376-6f78a93545d6,
;;;         :timestamp 2022-03-02T18:37:07.997Z,
;;;         :version 2.0,
;;;         :experiment_id f97a5942-790d-4b85-b5cb-fc648bba3cc1,
;;;         :sub_type Event:RubbleCollapse,
;;;         :source simulator}}

(defn rita-handle-rubble-collapse-message
  [tbm tb-version]
  ;+++ do something here
  　nil)

;;; sub_type: Event:RubbleDestroyed tbm=
;; {:data {:mission_timer 10 : 22,
;;         :playername intermonk,
;;         :rubble_z 5.0,
;;         :rubble_x -2186.0,
;;         :elapsed_milliseconds 281897.0,
;;         :rubble_y 60.0},
;;  :header {:version 1.1,
;;           :message_type event,
;;           :timestamp 2021-03-13T00:37:56.775Z},
;;  :msg {:experiment_id 3c80b33a-330b-4912-8b4c-7fe078292352,
;;        :trial_id a13c7181-4966-4ecb-81a4-784ea4b66008,
;;        :source simulator,
;;        :timestamp 2021-03-13T00:37:56.775Z,
;;        :sub_type Event:RubbleDestroyed,
;;        :version 0.5}}

;; "Event:RubbleDestroyed"

(defn rita-handle-RubbleDestroyed-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {name :name
         playername :playername
         elapsed_milliseconds :elapsed_milliseconds
         rubble_z :rubble_z
         rubble_x :rubble_x
         rubble_y :rubble_y} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))
        id (rsc/get-player-id-from-participant-id (keyword pid))]
    (ts/increment-strength-data-for pid (seglob/get-trial-number) elapsed_milliseconds :rubble-removed)
    (slearn/add-cleared! pid (/ elapsed_milliseconds 1000.0) [rubble_x rubble_y rubble_z])
    (when (dplev :action :all) (println "Player" pid "Cleared rubble at" [rubble_x rubble_y rubble_z]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Perturbations

;;; message_type= event sub_type= Event:Perturbation
;;; tbm= {:data
;;;        {:elapsed_milliseconds 543006.0,
;;;         :mission_state start,
;;;         :mission_timer 6 : 0,
;;;         :type blackout},
;;;       :header {
;;;         :version 1.1,
;;;         :message_type event,
;;;         :timestamp 2022-03-02T18:44:26.260Z},
;;;       :msg {
;;;         :trial_id 55cd3a31-548a-4d5b-9376-6f78a93545d6,
;;;         :timestamp 2022-03-02T18:44:26.261Z,
;;;         :version 2.2,
;;;         :experiment_id f97a5942-790d-4b85-b5cb-fc648bba3cc1,
;;;         :sub_type Event:Perturbation,
;;;         :source simulator}}

(defn rita-handle-perturbation-message
  [tbm tb-version]
  (let [{em :elapsed_milliseconds
         ptype :type} (:data tbm)]
    (when ptype
      (when (dplev :all :io)
        (println "Perturbation of type " ptype " occurred at" em))
      (ie/register-event {:event-key :perturbation :type ptype :em em}))
  　nil))


;;; Comms and signals

;;; testbed message_type= event sub_type= Event:Signal
;;; tbm= {:data
;;;        {:y 59.0,
;;;         :participant_id x202203023,
;;;         :elapsed_milliseconds 100192.0,
;;;         :roomname F4,
;;;         :mission_timer 13 : 23,
;;;         :z 12.0,
;;;         :x -2164.0,
;;;         :playername Player539,
;;;         :message No Victim Detected}, ;  or Regular Victim Detected or Critical Victim Detected
;;;       :header {
;;;         :version 1.1,
;;;         :message_type event,
;;;         :timestamp 2022-03-02T18:37:03.448Z},
;;;       :msg {
;;;         :trial_id 55cd3a31-548a-4d5b-9376-6f78a93545d6,
;;;         :timestamp 2022-03-02T18:37:03.448Z,
;;;         :version 2.0,
;;;         :experiment_id f97a5942-790d-4b85-b5cb-fc648bba3cc1,
;;;         :sub_type Event:Signal,
;;;       :source simulator}}

(defn rita-handle-signal-message
  [tbm tb-version]
  ;+++ do something here
  　nil)


;; {"msg":{"trial_id":"3a5266e9-f9ea-4020-86e3-0f295cc11f85",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:Beep",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-19T12:22:23.952Z"},
;;  "data":{"beep_z":152,
;;          "beep_y":60,
;;          "message":"Beep Beep",
;;          "beep_x":-2080,
;;          "source_entity":"Victim Detection Device (Player)"},
;;  "header":{"message_type":"event",
;;            "version":"0.5",
;;            "timestamp":"2020-06-19T12:22:23.951Z"}}

(defn rita-handle-beep-message
  [tbm tbversion]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {beep-x :beep_x
         beep-y :beep_y
         beep-z :beep_z
         message :message
         source-entity :source_entity} (:data tbm)
        neartoportal (vol/close-to-a-portal beep-x beep-z beep-y 5)
        {playername :playername
         id :id} (find-player-nearest beep-x beep-y beep-z)
        whereIam (where-am-I beep-x beep-z beep-y)
        vname (and neartoportal (global/RTobject-variable neartoportal)) ; variable name
        allothersides (and neartoportal (get-rooms-from-portal neartoportal))
        othersides (and neartoportal (remove (fn [r] (= r whereIam)) allothersides))
        proomnames (and neartoportal (map (fn [r] (get-object-vname r)) othersides))
        vroomnames (and neartoportal (map global/RTobject-variable othersides))]
    (when (dplev :beeper :all) (println source-entity "says" message "at [" beep-x beep-z beep-y "] to room" proomnames))

    (let [strategy (player-triage-strategy id)
          beep-response (player-beep-response id)
          time-remaining (seconds-remaining id)]
      (when (dplev :strategy :all) (println "Strategy=" strategy "beep-response=" beep-response "time-remaining=" time-remaining))
      (case message
        "Beep"
        (do (when (dplev :beeper :all) (println "One beep heard"))
            (seglob/set-last-beeped-room [neartoportal 1 (seglob/rita-ms-time)])
            (if neartoportal (predict/maybe-predict-portal-use [] id playername nil whereIam neartoportal)))

        "Beep Beep"
        (do (when (dplev :beeper :all) (println "Two beeps heard"))
            (seglob/set-last-beeped-room [neartoportal 2 (seglob/rita-ms-time)])
            (if neartoportal (predict/maybe-predict-portal-use [] id "playername" nil whereIam neartoportal)))

        (when (dplev :beeper :warn :all) (println "unrecognised beep message:" message))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Tools

;;;testbed message sub_type: Event:ToolDepleted tbm=
;; {:data {:mission_timer 10 : 30,
;;         :playername WoodenHorse9773,
;;         :tool_type HAMMER,
;;         :elapsed_milliseconds 273652.0},
;;  :header {:version 1.1,
;;           :message_type event,
;;           :timestamp 2021-03-13T00:37:48.528Z},
;;  :msg {:experiment_id 3c80b33a-330b-4912-8b4c-7fe078292352,
;;        :trial_id a13c7181-4966-4ecb-81a4-784ea4b66008,
;;        :source simulator,
;;        :timestamp 2021-03-13T00:37:48.529Z,
;;        :sub_type Event:ToolDepleted,
;;        :version 1.0}}

;;                  "Event:ToolDepleted"

(defn rita-handle-ToolDepleted-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {playername :playername
         tool_type :tool_type} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))]

    (when (dplev :all) (println "Player" pid "has a depleted" tool_type))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Ground truth

(defn rita-handle-PerturbationRubbleLocations-message
  [tbm tb-version]
  nil) ;+++ implement me.  It is essential that out agents can see the fallen rubble

;;;      "Mission:BlockageList"
;; sub_type: Mission:BlockageList tbm= {:data {:mission Falcon_Easy,
;;                                             :mission_blockage_list [{:x -2073.0, :y 61.0, :z 190.0, :block_type air, :room_name Room 107/Room 108, :feature_type Opening - Passable}
;;                                                                     {:x -2073.0, :y 62.0, :z 190.0, :block_type air, :room_name Room 107/Room 108, :feature_type Opening - Passable}
;;                                                                     ...
;;                                                                     {:x -2079.0, :y 62.0, :z 182.0, :block_type air, :room_name Right Hallway, :feature_type Blockage}]},
;;                                      :header {:timestamp 2020-08-19T23:14:36.576Z,
;;                                               :message_type groundtruth,
;;                                               :version 1.0},
;;                                      :msg {:experiment_id e0b770fa-d726-4c7e-8406-728dcfaf8b98,
;;                                            :trial_id b66aa7e5-a16c-4abc-a2cb-5ae3bdc675e5,
;;                                            :timestamp 2020-08-19T23:14:36.577Z,
;;                                            :source simulator,
;;                                            :sub_type Mission:BlockageList,
;;                                            :version 0.5}}
(defn rita-handle-blockage-list-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {mission :mission
         mission_blockage_vec :mission_blockage_list} (:data tbm)]
    (when (dplev :all) (println "Received blockage list, mission=" mission "with" (count mission_blockage_vec) "blockages"))
  nil))

;; message_type= observation sub_type= Mission:FreezeBlockList
;; tbm= {:data {:mission_freezeblock_list [],
;;              :mission Saturn_A},
;;       :header {:version 1.1,
;;                :message_type groundtruth,
;;                :timestamp 2022-01-19T22:35:50.226Z},
;;       :msg {:sub_type Mission:FreezeBlockList,
;;             :trial_id add1aeef-a1c0-4621-b7d6-51efe129c99c,
;;             :source simulator,
;;             :version 0.1,
;;             :experiment_id 934c548a-54ef-4e1e-bdbb-613bd395764b,
;;             :timestamp 2022-01-19T22:35:50.227Z}}

(defn rita-handle-FreezeBlockList
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {mission :mission
         mission_freezeblock_list :mission_freezeblock_list} (:data tbm)]
    (when (dplev :all)
      (println "Received freeze block list, mission=" mission "with" (count mission_freezeblock_list) "freeze blocks")) ; +++ do something with them
  nil))

;;; Fin
