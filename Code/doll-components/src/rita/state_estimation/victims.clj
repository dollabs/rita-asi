;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.victims
  "Observations about changes to victims placement and states."
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
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            [rita.state-estimation.ras :as ras]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.teamstrength :as ts]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.cognitiveload :as cogload]
            [rita.state-estimation.interventions :as intervene]
            [rita.state-estimation.interventionengine :as ie]
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

#_(in-ns 'rita.state-estimation.victims)

;;; Takes either a victim object or it's variable name.

(defn color-of-victim
  [avictim]
  ;;(when (dplev :all) (println "In color-of-victim with avictim=" avictim))
  (if (nil? avictim)
    (do (when (dplev :warn :all) (println "color-of-victim called with NULL"))
        :null)
    (let [victim-obj (if (global/RTobject? avictim)
                       avictim
                       (first (eval/find-objects-of-name avictim)))
          victim-type (global/RTobject-type victim-obj)
          ;;_ (when (dplev :all) (println "Color of victim type=" victim-type "avictim=" avictim))
          res (case victim-type
                Minecraft-object-prismarine :green
                Minecraft-object-gold_block :gold
                block_victim_1 :green
                block_victim_2 :gold
                (when (dplev :error :all)
                  (println "Error unknown victim type:" victim-type)))]
      ;;(when (dplev :all) (println "color=" res))
      res)))

(defn victims-in-room
  [aroom]
  (let [in-play-victims
        (into []
              (map (fn [victim]
                     (if (not (or (>= (bs/get-belief-in-variable
                                       (global/RTobject-variable victim) :dead) 0.8)
                                  (>= (bs/get-belief-in-variable
                                       (global/RTobject-variable victim) :saved) 0.8)))
                       victim))
                   (get (seglob/get-victims-in-occupiable-spaces) aroom)))]
    ;;(when (dplev :all) (println "In play victims in room" (global/RTobject-variable aroom) "=" in-play-victims))
    in-play-victims))

(defn green-victims-in-room
  [aroom]
  (let [in-play-green-victims
        (into []
              (map (fn [victim]
                     (if (and
                          ;; Victim awaiting care
                          (not (or (>= (bs/get-belief-in-variable
                                        (global/RTobject-variable victim) :dead) 0.8)
                                   (>= (bs/get-belief-in-variable
                                        (global/RTobject-variable victim) :saved) 0.8)))
                          ;; Normal (non critical) victim
                          (= (color-of-victim victim) :green))
                       victim))
                   (get (seglob/get-victims-in-occupiable-spaces) aroom)))]
    ;;(when (dplev :all) (println "In play victims in room" (global/RTobject-variable aroom) "=" in-play-victims))
    in-play-green-victims))


;;; testbed message_type= event sub_type= Event:VictimEvacuated
;;; tbm= {:data
;;;        {:victim_y 60.0,
;;;         :participant_id x202203021,
;;;         :victim_id 24.0,
;;;         :elapsed_milliseconds 150591.0,
;;;         :type victim_saved_a,  ; or victim_saved_c or victim_saved_b
;;;         :mission_timer 12 : 33,
;;;         :success true,
;;;         :playername Aptiminer1,
;;;         :victim_x -2116.0,
;;;         :victim_z 62.0},
;;;       :header {
;;;         :version 1.1,
;;;         :message_type event,
;;;         :timestamp 2022-03-02T18:37:53.847Z},
;;;       :msg {
;;;         :trial_id 55cd3a31-548a-4d5b-9376-6f78a93545d6,
;;;         :timestamp 2022-03-02T18:37:53.847Z,
;;;         :version 2.0,
;;;         :experiment_id f97a5942-790d-4b85-b5cb-fc648bba3cc1,
;;;         :sub_type Event:VictimEvacuated,
;;;         :source simulator}}

(defn rita-handle-victim-evacuated-message
  [tbm tb-version]
  (let [data (:data tbm)
        header (:header tbm)
        msg (:msg tbm)
        {em :elapsed_milliseconds
         victim_x :victim_x
         victim_y :victim_y
         victim_z :victim_z
         type :type
         victim_id :victim_id} data
        pid (seglob/get-participant-id-from-data data)
        callsign (seglob/pid2callsign pid)
        victim-info (get (seglob/get-victim-map) victim_id)
        {vtype :vtype
         [primary-evac-spot secondary-evac-spot] :evacuation-locations
         distance-difference :distance-difference} victim-info]

    (when (dplev :action :all :io)
      (println "******,PID=" pid "(" callsign ") evacuated a victim of type" type
               "with VID=" victim_id "at em=" em)
      (pprint (seglob/get-pid-to-callsign)))

    (if em (seglob/update-last-ms-time em))

    (case callsign
      "Red"
      (ie/register-event {:event-key :medic-evacuated :em em :pid pid :victim-type type})

      "Blue"
      (ie/register-event {:event-key :engineer-evacuated :em em :pid pid :victim-type type})

      "Green"
      (ie/register-event {:event-key :transporter-evacuated :em em :pid pid :victim-type type}))

    (if (= type "victim_saved_c")
      (do
        (ts/increment-strength-data-for pid (seglob/get-trial-number) em :evacuated-critical-victims)
        (seglob/inc-critical-victims-evacuated))
      (do
        (ts/increment-strength-data-for pid (seglob/get-trial-number) em :number-of-evacuated-victims)
        (seglob/inc-victims-evacuated)))

    ;; Maybe generate an event
    (if (nil? victim-info)
      (if (dplev :all :error)
        (println "ERROR: Failed to find victim info for victim_id=" victim_id))
      (cond
        (ras/coordinates-in-volume victim_x victim_z victim_y primary-evac-spot)
        (println "***** Well placed") ; well done!

        (ras/coordinates-in-volume victim_x victim_z victim_y secondary-evac-spot)
        (do (println "***** Suboptimally placed")  ;nil; ; suboptimal, make an event here
            (ie/register-event {:event-key :suboptimal-evacuation,
                                :pid pid,
                                :x victim_x,
                                :z victim_y,
                                :y victim_z,
                                :evac-at secondary-evac-spot,
                                :better-would-be primary-evac-spot,
                                :extra-distance distance-difference,
                                :type vtype,
                                :em em}))

        :otherwise
        (do (println "***** Incorrectly placed")
          (ie/register-event {:event-key :wrong-evac-spot,
                                :pid pid,
                                :x victim_x,
                                :z victim_y,
                                :y victim_z,
                                :best-ecav primary-evac-spot,
                                :second-choice secondary-evac-spot,
                                :type vtype,
                                :em em})
          (when (dplev :all :warn)
            (println "Warn: Evacuation info didn't line up:")
            (println "location placed=[" victim_x victim_y victim_z "]")
            (println "primary location is here:")
            (pprint primary-evac-spot)
            (println "secondary location is here:")
            (pprint secondary-evac-spot)))))

    (when (dplev :action :all)
      (println "Participant " pid "evacuated victim_id=" victim_id "of type=" type "at x=" victim_x "y=" victim_y "z" victim_z))
    nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Ground truth

;;;      "Mission:VictimList"
;; sub_type: Mission:VictimList tbm= {:data {:mission_timer 10 : 3,
;;                                           :mission Falcon_Easy,
;;                                           :mission_victim_list [{:x -2077.0, :y 60.0, :z 145.0, :block_type block_victim_1, :room_name Open Break Area}
;;                                                                 {:x -2082.0, :y 60.0, :z 147.0, :block_type block_victim_1, :room_name Open Break Area}
;;                                                                 ...
;;                                                                 {:x -2047.0, :y 60.0, :z 173.0, :block_type block_victim_2, :room_name Herbalife Conference Room}]},
;;                                    :header {:timestamp 2020-08-19T23:14:36.572Z,
;;                                             :message_type groundtruth,
;;                                             :version 1.0},
;;                                    :msg {:experiment_id e0b770fa-d726-4c7e-8406-728dcfaf8b98,
;;                                          :trial_id b66aa7e5-a16c-4abc-a2cb-5ae3bdc675e5,
;;                                          :timestamp 2020-08-19T23:14:36.573Z,
;;                                          :source simulator,
;;                                          :sub_type Mission:VictimList,
;;                                          :version 0.5}}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Ground truth



;;;       "Event:VictimsExpired"
;; sub_type: Event:VictimsExpired tbm= {:data {:mission_timer 5 : 0,
;;                                             :expired_message All remaining yellow victims have succumbed to their injuries.
;;                                             },
;;                                      :header {:timestamp 2020-08-19T23:23:05.446Z,
;;                                               :message_type groundtruth,
;;                                               :version 1.0},
;;                                      :msg {:experiment_id e0b770fa-d726-4c7e-8406-728dcfaf8b98,
;;                                            :trial_id b66aa7e5-a16c-4abc-a2cb-5ae3bdc675e5,
;;                                            :timestamp 2020-08-19T23:23:05.446Z,
;;                                            :source simulator,
;;                                            :sub_type Event:VictimsExpired,
;;                                            :version 0.5}}

(defn rita-handle-event-victims-expired-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {mission-timer :mission_timer
         msg :expired_message} (:data tbm)
        time-remaining (ras/parse-mission-time mission-timer)]
    (when (dplev :all) (println "Victims have expired -  mission timer=" mission-timer msg))
    ;; +++ map through all victims setting the mode of the yellow victims to :dead 100% belief +++
    ;; Do nothing with this data for now
    nil))

;; Unhandled testbed message
;; sub_type: Event:VictimPickedUp tbm=
;; {:data {:mission_timer 0 : 26,
;;         :playername WoodenHorse9773,
;;         :victim_x -2099.0,
;;         :victim_y 60.0,
;;         :color GREEN,
;;         :victim_z 36.0,
;;         :elapsed_milliseconds 877748.0},
;;  :header {:version 1.1,
;;           :message_type event,
;;           :timestamp 2021-03-13T00:47:52.626Z},
;;  :msg {:experiment_id 3c80b33a-330b-4912-8b4c-7fe078292352,
;;        :trial_id a13c7181-4966-4ecb-81a4-784ea4b66008,
;;        :source simulator,
;;        :timestamp 2021-03-13T00:47:52.626Z,
;;        :sub_type Event:VictimPickedUp,
;;        :version 1.0}}

;;; "Event:VictimPickedUp"

(defn rita-handle-VictimPickedUp-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {playername :playername
         victim_x :victim_x
         victim_y :victim_y
         victim_z :victim_z
         em :elapsed_milliseconds
         color :color
         type :type
         target_block_type :target_block_type} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))]
    (if em (seglob/update-last-ms-time em))
    (when (dplev :action :all)
      (println "Player" pid "has picked up a" (or type color) "victim at" [victim_x victim_y victim_z]))))

;; Unhandled testbed message sub_type: Event:VictimPlaced tbm=
;; {:data {:mission_timer 0 : 19,
;;         :playername WoodenHorse9773,
;;         :victim_x -2102.0,
;;         :victim_y 60.0,
;;         :color GREEN,
;;         :victim_z 21.0,
;;         :elapsed_milliseconds 884097.0},
;;  :header {:version 1.1,
;;           :message_type event,
;;           :timestamp 2021-03-13T00:47:58.975Z},
;;  :msg {:experiment_id 3c80b33a-330b-4912-8b4c-7fe078292352,
;;        :trial_id a13c7181-4966-4ecb-81a4-784ea4b66008,
;;        :source simulator,
;;        :timestamp 2021-03-13T00:47:58.975Z,
;;        :sub_type Event:VictimPlaced,
;;        :version 1.0}}

;;                  "VictimPlaced"


;;; "Event:VictimPlaced"
;;; sub_type: Event:VictimPlaced
;;; tbm=
;;; {:data {:mission_timer 10 : 18,
;;;         :elapsed_milliseconds 285248.0,
;;;         :playername Player724,
;;;         :type REGULAR,
;;;         :victim_x -2098.0,
;;;         :victim_y 60.0,
;;;         :victim_z 57.0,
;;;         :victim_id 31.0},
;;;  :header {:timestamp 2021-06-29T21:02:16.901Z,
;;;           :message_type event,
;;;           :version 0.6},
;;;  :msg {:experiment_id bc50aea2-b913-4889-9fbd-f4ba2a72763c,
;;;        :trial_id 87da6996-7642-49bb-b114-8e428c87b5de,
;;;        :timestamp 2021-06-29T21:02:16.901Z,
;;;        :source simulator,
;;;        :sub_type Event:VictimPlaced,
;;;        :version 1.2}}

;; {"app-id":"TestbedBusInterface",
;;  "mission-id":"a30f2873-6c9e-4306-bea4-5b5af130c930",
;;  "routing-key":"testbed-message",
;;  "testbed-message":{"data":{"victim_y":60.0,
;;                             "participant_id":"E000665",
;;                             "victim_id":-1.0,
;;                             "elapsed_milliseconds":114539.0,
;;                             "type":"victim_a", or :victim_saved_a (or b or c)
;;                             "mission_timer":"18 : 9",
;;                             "playername":"BLUE_ASIST1",
;;                             "victim_x":-2190.0,
;;                             "victim_z":81.0},
;;                     "header":{"message_type":"event",
;;                               "version":"1.1",
;;                               "timestamp":"2022-03-31T00:55:45.049Z"},
;;                     "msg":{"experiment_id":"208224d3-bb80-45d5-a984-b4e533039161",
;;                            "version":"2.1",
;;                            "sub_type":"Event:VictimPlaced",
;;                            "trial_id":"a30f2873-6c9e-4306-bea4-5b5af130c930",
;;                            "source":"simulator",
;;                            "timestamp":"2022-03-31T00:55:45.050Z",
;;                            "replay_id":"f0a09562-189a-4c80-851c-58fd3039c2b1",
;;                            "replay_parent_type":"TRIAL"}},
;;  "timestamp":1649176371063,
;;  "received-routing-key":"testbed-message",
;;  "exchange":"rita"}

(defn rita-handle-VictimPlaced-message
  [tbm tb-version]
    (let [data (:data tbm)
        header (:header tbm)
        msg (:msg tbm)
        {em :elapsed_milliseconds
         victim_x :victim_x
         victim_y :victim_y
         victim_z :victim_z
         type :type
         victim_id :victim_id} data
          pid (seglob/get-participant-id-from-data data)]
      (if em (seglob/update-last-ms-time em))
      (slearn/add-moved! pid (/ em 1000.0) [victim_x victim_y victim_z] victim_id)
      (ts/increment-strength-data-for pid (seglob/get-trial-number) em :number-of-relocated-victims)
      (when (dplev :action :all)
        (println "Participant " pid "placed victim_id=" victim_id "of type=" type "at x=" victim_x "y=" victim_y "z" victim_z))
      nil))

;;; This is the old version
  #_(defn rita-handle-VictimPlaced-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {playername :playername
         elapsed_milliseconds :elapsed_milliseconds
         victim_x :victim_x
         victim_y :victim_y
         victim_z :victim_z
         color :color
         target_block_type :target_block_type} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))
        vid nil]
    (slearn/add-moved! pid (/ elapsed_milliseconds 1000.0) [victim_x victim_y victim_z] vid)
    (when (dplev :action :all) (println "Player" pid "has placed a" color "victim at" [victim_x victim_y victim_z]))))

;; Unhandled testbed message sub_type: Status:UserSpeech tbm=
;; {:data {:text  104 over here,
;;         :playername intermonk},
;;  :header {:version 0.1,
;;           :message_type status,
;;           :timestamp 2021-03-13T00:38:51.3817Z},
;;  :msg {:experiment_id 3c80b33a-330b-4912-8b4c-7fe078292352,
;;        :trial_id a13c7181-4966-4ecb-81a4-784ea4b66008,
;;        :source asistdataingester,
;;        :timestamp 2021-03-13T00:38:51.3817Z,
;;        :sub_type Status:UserSpeech,
;;        :version 0.4}}

;;                  "Status:UserSpeech"



;;; Fin
