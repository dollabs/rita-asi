;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.observations
  "Observations form the testbed."
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
            [rita.state-estimation.ras :as ras]
            ;;[rita.state-estimation.rlbotif :as rlbotif]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.rita-se-core :as rsc :refer :all] ; back off from refer all +++
            [rita.state-estimation.spacepredicates :as spreads]
            [rita.state-estimation.cognitiveload :as cogload]
            [rita.state-estimation.interventions :as intervene]
            [rita.state-estimation.predictions :as predict]
            [rita.state-estimation.measures :as measures]
            [rita.state-estimation.asrandchat :as lang]
            [rita.state-estimation.victims :as victims]
            [rita.state-estimation.players :as players]
            [rita.state-estimation.markers :as markers]
            [rita.state-estimation.building :as building]
            [rita.state-estimation.achandling :as acs]
            [rita.state-estimation.groundtruth :as gt]
            [rita.state-estimation.anomalies :as anomaly]
            [rita.state-estimation.study2 :as study2]
            [rita.state-estimation.study3 :as study3]
            [rita.state-estimation.teamstrength :as ts]
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

#_(in-ns 'rita.state-estimation.observations)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Observations come in to observations, where they are dispatched to         ;;;
;;; their handlers according to their message type and subtype, where the      ;;;
;;; Work of interpreting the observations is performed.                        ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; +++ fix temporary solution below +++
(def ^:dynamic *last-named-occupiable-space* nil) ; Handle brief dissapearances in testbed

;;; +++ not clear that this belongs here +++ MOVE IT

(defn maybe-report-occupancy-change
  [pub id playername x y z whereIam oldw nearportal em]
  (or (if (and (not (= playername "Ed"))
               (not (= oldw whereIam))) ; If I'm not where I was before
        ;(when (dplev :all) (println "Note: occupancy change, was in" oldw "now in" whereIam))
        (let [previously (or oldw *last-named-occupiable-space*)
              oldspace (and previously (get-object-vname previously))
              newspace (and whereIam (get-object-vname whereIam))
              pub (markers/maybe-learn-marker-semantics pub id playername x y z nearportal whereIam em)]
          (if (not (nil? whereIam)) (def ^:dynamic *last-named-occupiable-space* whereIam))
          (if (and (not (nil? previously)) (not (nil? whereIam)))
            (do
              (if (> *debug-verbosity* 0)
                (when (dplev :occupancy :all) (println playername "has left" oldspace  "victims=" (prop/prop-readable-form (victims/victims-in-room previously)))))
              (seglob/set-last-room! playername previously)
              (predict/match-prediction id :exit-room oldspace)))
          (if (and (not (nil? previously)) (not (nil? whereIam)))
            (do (if (> *debug-verbosity* 0)
                  (when (dplev :occupancy :all) (println playername "has entered" newspace "victims=" (prop/prop-readable-form (victims/victims-in-room whereIam)))))
                (predict/match-prediction id :enter-room newspace)
                (if (and (vol/a-room? newspace) (not (seglob/room-entered? newspace)))
                  (seglob/register-room-entered newspace))
                (bs/add-binary-proposition ; +++ need to add a timestamp
                 :was-in
                 (global/get-object-from-id id) ; perhaps no longer needed given :visited
                 (global/RTobject-variable whereIam))
                (let [npub (ritamsg/add-bs-change-message
                            pub
                            {:subject playername
                             :changed :location
                             :values newspace
                             :agent-belief 1.0})
                      nnpub (if nearportal
                              (ritamsg/add-bs-change-message
                               npub
                               {:subject playername
                                :changed :action
                                :values {:subject playername
                                         :action :open
                                         :object (if nearportal (get-object-vname nearportal))}
                                :agent-belief 0.8})
                              npub)]
                  (if (and nearportal (= (global/RTobject-type nearportal) 'Door))
                    (let [nnnpub (ritamsg/add-bs-change-message
                                  nnpub
                                  {:subject playername
                                   :changed :action
                                   :values {:subject playername
                                            :action (if (a-room? whereIam) 'enter-room 'enter-corridor)
                                            :object (get-object-vname whereIam)}
                                   :agent-belief 0.9
                                   })
                          nnnnpub (ritamsg/add-bs-change-message
                                  nnnpub
                                  {:subject (get-object-vname nearportal)
                                   :changed :mode
                                   :values {:open 0.8 :closed 0.1 :locked 0.1}
                                   :agent-belief 0.8
                                   })]
                      nnnnpub)
                    nnpub))))))
      pub))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Handle and interpret "state" messages. This probably belongs elsewhere such as in testbed.

;; {"msg":{"trial_id":"3a5266e9-f9ea-4020-86e3-0f295cc11f85",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"state",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-19T12:21:51.230Z"},
;;  "data":{"y":60.0,
;;          "motion_y":0.0,
;;          "entity_type":"human",
;;          "total_time":3432756,
;;          "name":"Player96",
;;          "yaw":-217.34996,
;;          "pitch":21.000008,
;;          "world_time":10000,
;;          "life":20.0,
;;          "z":147.55394746331615,
;;          "id":"2ce4ef37-1586-3eaf-ba6f-4b93d1e38829",
;;          "x":-2149.300000011921,
;;          "timestamp":"2020-06-19T12:21:51.230Z",
;;          "observation_number":172,
;;          "motion_z":0.0,
;;          "motion_x":0.0},
;;  "header":{"message_type":"observation",
;;            "version":"0.5",
;;            "timestamp":"2020-06-19T12:21:51.230Z"}}

(def messagecounter 0)

(defn condense-evidence
  [acname evidence]
  (map (fn [[tm data]]
               [tm (if data (ras/average (map first data)))])
             evidence))

  ;; (cond (empty? evidence)
  ;;       []

  ;;       (== (count evidence) 1)
  ;;       evidence ; (first evidence)

  ;;       (> (count evidence) 1)
  ;;       evidence
  ;;       ;;[(ras/average (map first evidence)) (ras/average (map second evidence))]))
;;       ))

(defn csv-flatten
  [vecof-tm-data-pairs]
  (let [data (map second vecof-tm-data-pairs)]
    (str  (or (nth data 0) " ")  ", " (or (nth data 1) " ")", " (or (nth data 2) " "))))

(defn safe-trim
  [x]
  (clojure.string/trim (str x)))

(defn report-epoch-statistics
  [epoch & [tofilep]]
  (let [knowledge (seglob/get-leadership-votes)
        db (seglob/get-player-strength-data)
        trial (seglob/get-trial-number)
        us-participants (map safe-trim (seglob/get-team-members))
        pcallnames (map seglob/pid2callsign us-participants)
        pcmap (into {} (map (fn [cn pid] {cn pid}) pcallnames us-participants))
        participants [(get pcmap "Red") (get pcmap "Green") (get pcmap "Blue")]

        ;; _ (println "###### epoch statistics ######")
        ;; _ (println "pid2callsignmap=" (seglob/get-pid-to-callsign))
        ;; _ (println "us-participants=" us-participants)
        ;; _ (println "pcallnames=" pcallnames)
        ;; _ (println "pcmap=" pcmap)
        ;; _ (println "participants=" participants)
        ucfknowledge (into {}
                           (map (fn [tm]
                                  {tm (get knowledge ["ac_ucf_ta2_playerprofiler" epoch tm])})
                                participants))
        gelpknowledge (into {}
                           (map (fn [tm] {tm (get knowledge ["ac_gallup_ta2_gelp" epoch tm])})
                                participants))
        dmutterknow  (map (fn [tm]
                            [tm (ts/get-ps-value db tm trial epoch :spoken-utterances)])
                          participants)
        dmwordsknow  (map (fn [tm]
                            [tm (ts/get-ps-value db tm trial epoch :spoken-words)])
                          participants)
    ;; (println "Knowledge=")
    ;; (pprint knowledge)
        jsonout (with-out-str
                  (println (if tofilep "" "LEADERSHIP-STATISTICS-JSON") "{\"epoch\"" epoch ", \"trial\"" (seglob/get-trialID) ", \"trial-num\"" trial
                           ", \"participants\"" (map (fn [pid] [pid (seglob/pid2callsign pid)]) participants)
                           ", \"ac_ucf_ta2_playerprofiler\""
                           (into {} (condense-evidence "ac_ucf_ta2_playerprofiler" ucfknowledge))
                           ", \"ac_gallup_ta2_gelp\""
                           (into {} (condense-evidence "ac_gallup_ta2_gelp" gelpknowledge))
                           ", \"ac_doll_utter\""
                           (into {} dmutterknow)
                           ", \"ac_doll_words\""
                           (into {} dmwordsknow)
                           "}"))
        csvout (with-out-str
                 (println (if tofilep "" "LEADERSHIP-STATISTICS-CSV, ") "\"epoch\"," epoch, ", \"trial\", " (seglob/get-trialID) ", \"trial-num\", " trial
                          ", \"Participants\"," (str "\"" (nth participants 0) "\", \"" (nth participants 1) "\", \"" (nth participants 2) "\", ")
                          "\"Callnames\",  \"Red\", \"Green\", \"Blue\", "
                          "\"ac_ucf_ta2_playerprofiler\", "
                          (csv-flatten (condense-evidence "ac_ucf_ta2_playerprofiler" ucfknowledge))
                          ", \"ac_gallup_ta2_gelp\", "
                          (csv-flatten (condense-evidence "ac_gallup_ta2_gelp" gelpknowledge))
                          ", \"ac_doll_utter\", "
                          (csv-flatten dmutterknow)
                          ", \"ac_doll_words\", "
                          (csv-flatten dmwordsknow)))]
    (when (not tofilep)
      (println jsonout)
      (println csvout))
    [csvout jsonout]))

(defn report-leadership-statistics
  []
  (println "Printing leadership statistics")
  (let [results
        (map (fn [epoch]
               (report-epoch-statistics epoch  true))
             (range 0 6))
        json (apply str (map second results))
        csv  (apply str (map first results))]
    (spit (str "../data/" (seglob/get-trialID) ".json") (str "{\"leadership\" " json   " }"))
    (spit (str "../data/" (seglob/get-trialID) ".csv")  csv)))

(defn rita-handle-state-message
  [tbm tbversion]
  (def messagecounter (+ 1 messagecounter))
  (let [message (:data tbm)
        {x :x ; -2179.4057526765046
         y :y ; 28.0
         z :z ; 155.44119827835647
         motion_x :motion_x ; -0.2775688493315885
         motion_y :motion_y
         motion_z :motion_z ; -0.40585672175957577
         entity_type :entity_type ; human
         name :name ; Player396
         ;;playername :playername
         yaw :yaw
         pitch :pitch
         life :life
         elapsed_milliseconds :elapsed_milliseconds} message
        participantid (seglob/get-participant-id-from-data message)
        id (rsc/get-player-id-from-participant-id (keyword participantid))
        {whereIam :whereIam,
         victimswhereiam :victimswhereiam,
         whatIcanSee :whatIcanSee
         oldx :oldx
         oldy :oldy
         oldz :oldz
         oldw :oldw
         oldwhatIcanSee :oldwhatIcanSee
         neartoportal :neartoportal
         oldneartoportal :oldneartoportal
         neartoswitch :neartoswitch
         oldneartoswitch :oldneartoswitch
         neartovictim :neartovictim
         oldneartovictim :oldneartovictim} (new-player-position id tbm)
        victimswhereiam (if victimswhereiam (set (filter global/RTobject? victimswhereiam)))
        role (seglob/get-role participantid)]
    ;;; Sanity-check the id
    (when (and (not (clojure.string/includes? (str id) "participant"))
               (dplev :all :error :warn))
      (println "ERROR: " participantid "found from the following message:")
      (pprint tbm))
    (when elapsed_milliseconds
        (seglob/update-last-ms-time elapsed_milliseconds)
      (let [last-epoch-reported (seglob/get-last-epoch-reported)
            current-epoch (ras/compute-epoch-from-ms elapsed_milliseconds)]
        (when (> current-epoch (+ last-epoch-reported 1)) ; We just started a new epoch and the last one has not yet been reported
          (report-epoch-statistics (+ last-epoch-reported 1))
          (seglob/set-last-epoch-reported (+ last-epoch-reported 1)))))
    (if (vol/a-room? whereIam) (ensure-room-visited whereIam))
    (set-field-value! id 'pname (keyword participantid))
    #_(if (not (and (= x oldx) (= y oldy) (= z oldz)))
        (when (dplev :all) (println "[" x y z "] whereIam=" whereIam "neartoportal=" neartoportal "neartoswitch="
                                    neartoswitch "neartovictim=" neartovictim)))
    (when (and victimswhereiam (not (empty? victimswhereiam)))
      (rsc/set-victims-encountered role victimswhereiam)
      #_(when (dplev :all) (println participantid "see dead people" (map (fn [x] [(global/RTobject-variable x)
                                                                                  (if (critical-victim? x) :critical :normal)
                                                                                  (get *victims-state* x)])
                                                                         victimswhereiam))))
    (let [seen-untriaged-normal-victims (untriaged-normal-victims role)
          seen-untriaged-critical-victims (untriaged-critical-victims role)
          close-to-other (rsc/player-close-to-another participantid 3.0)
          #_ _ #_ (if (not (empty? close-to-other))
                    (when (dplev :all) (println "Participant " participantid "is close to" (into [] (map #(seglob/get-object-name-from-object-id %)  close-to-other)))))
          old-cog-load (cogload/get-cl-data-from-role role)
          new-cog-load (if victimswhereiam
                         (cogload/change-cognitive-load role {:skipped-normal-victims   seen-untriaged-normal-victims
                                                              :skipped-critical-victims seen-untriaged-critical-victims}) ;+++ divide by type - not all green
                         old-cog-load)
          publish-me (-> []
                         (maybe-report-object-visibility id participantid whereIam whatIcanSee oldwhatIcanSee)
                         (maybe-report-portal-proximity id participantid whereIam neartoportal oldneartoportal elapsed_milliseconds)
                         (maybe-report-room-visited id participantid whereIam neartoportal oldneartoportal [x y z] elapsed_milliseconds)
                         (maybe-report-switch-proximity id participantid whereIam neartoswitch oldneartoswitch)
                         (maybe-report-victim-proximity id participantid whereIam neartovictim oldneartovictim)
                         (maybe-report-occupancy-change id participantid x y z whereIam oldw neartoportal elapsed_milliseconds)
                         (maybe-report-position-change id participantid x y z oldx oldy oldz elapsed_milliseconds)
                         ;; Comment out study-2 pieces for study-3 runs
                         ;;(maybe-report-m3-metric id participantid x y z elapsed_milliseconds)
                         ;; (measures/maybe-report-m7-metric id participantid x y z oldx oldy oldz motion_x motion_y motion_z pitch yaw neartoportal role elapsed_milliseconds)
                         (maybe-report-close-to id participantid close-to-other elapsed_milliseconds)
                         (cogload/maybe-report-cognitive-load id participantid old-cog-load new-cog-load)
                         (intervene/maybe-intervene id participantid elapsed_milliseconds))

          publish  publish-me]
      (if (not (empty? publish)) (when (dplev :publish :all) (println "Messages to publish:" publish)))
      ;; Update stats
      ;;(slearn/set-player-name participantid) ; doesn't work because multiple players now
      (when (and (vol/a-corridor? whereIam) rsc/*in-room*)
        (let [now (seglob/rita-ms-time)]
          (when (dplev :occupancy :all) (println "*** Leaving room" (first rsc/*in-room*)
                                                 "entered at" (second rsc/*in-room*)
                                                 "leaving at" now "=" (int (/ (- now (second *in-room*)) 1000.0)) "Seconds")))
        (slearn/add-time-in-room role [(first rsc/*in-room*) (- (seglob/rita-ms-time) (second rsc/*in-room*))])
        (set-in-room nil))
      (if (and (not (= (seglob/get-role participantid) "None")) (seglob/mission-in-progress))
        (if (and (vol/a-room? whereIam) (not (= (global/RTobject-variable whereIam)
                                                (slearn/last-in-room (seglob/get-role participantid)))))
          (do (slearn/add-room-enter  (seglob/get-role participantid) (global/RTobject-variable whereIam))
              (when rsc/*in-room*
                (slearn/add-time-in-room  (seglob/get-role participantid) [(first rsc/*in-room*) (- (seglob/rita-ms-time) (second rsc/*in-room*))]))
              (set-in-room [(global/RTobject-variable whereIam) (seglob/rita-ms-time)]))))
      (if (seglob/mission-in-progress)
        (maybe-replan-path publish id whereIam x y z messagecounter)
        publish))))



;; {"msg":{"trial_id":"3a5266e9-f9ea-4020-86e3-0f295cc11f85",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:MissionState",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-19T12:21:51.611Z"},
;;  "data":{"mission":"Falcon",
;;          "mission_state":"Start"},
;;  "header":{"message_type":"event",
;;            "version":"0.5",
;;            "timestamp":"2020-06-19T12:21:51.610Z"}}


(declare get-trial-number-from-expt-mission)

(defn start-mission
  [start-mission]
  (let [{mission-state :mission-state
         version :version
         trial-id :trial-id
         experiment-id :experiment-id
         mission :mission
         timestamp :timestamp} start-mission]
    (when (dplev :io :all) (println "In start-mission with") (pprint start-mission)) ;+++ remove me
    (cond (and (seglob/get-mission-started) (not (seglob/get-strike-mode)))
          (when (dplev :io :all)
            (println "MissionState START message received when mission " (seglob/get-mission-started) " is already in progress.")
            (pprint start-mission))

          (and (or (not (seglob/get-mission-started)) (seglob/get-strike-mode))
               (or (= mission-state "Start") ; Spelling not consistant.
                   (= mission-state "START")))
          (do
            (seglob/init-uidnum)
            (seglob/set-mission-started-time (seglob/rita-ms-time))
            (measures/reset-m7-predictions-published)
            (measures/reset-demeanor-training-data)
            (seglob/set-trial-id trial-id)
            (seglob/set-experiment-id! experiment-id)
            (seglob/set-mission-ended-message-sent false)
            (seglob/set-mission-ended-time nil)
            (seglob/set-mission-started true)
            (seglob/set-mission-terminated false)
            (when (dplev :io :all) (println "New mission started at" timestamp "version=" version "trial-id=" trial-id "experiment-id=" experiment-id))
            (if (not (or (= mission "Singleplayer")        ; Singleplayer seems to come up for Sparky missions sometimes.
                         (= mission (seglob/get-loaded-model-name)))) ; We preload "Sparky" as a default because previous datasets did not
                                        ; contain the MissionState message
              (do
                (when (dplev :io :all) (println "Loading" mission "model"))
                (cond
                  (or (clojure.string/includes? mission "Falcon") (clojure.string/includes? mission "FALCON"))
                  (state-estimation-initialization "Falcon"
                                                   2 (get-trial-number-from-expt-mission mission) ;+++
                                                   version experiment-id (seglob/get-team-members))

                  (or (clojure.string/includes? mission "Saturn") (clojure.string/includes? mission "SATURN"))
                  (state-estimation-initialization "Saturn"
                                                   3 (get-trial-number-from-expt-mission mission) ;+++
                                                   version experiment-id (seglob/get-team-members))

                  :otherwise
                  (state-estimation-initialization mission
                                                   1 (get-trial-number-from-expt-mission mission) ;+++
                                                   version experiment-id (seglob/get-team-members)))
                nil))))

    (when (dplev :io :all)
      (println "Using currently loaded model: " (seglob/get-loaded-model-name)))

    ;; Establish if this is the first trial
    (let [psd (seglob/get-participant-strength-data)
          call-sign-map (seglob/get-player-callsign-map)
          first-trial-map (into {} (map (fn [[callsign pid]] {pid (some #{pid} psd)}) call-sign-map))]
      (seglob/set-participant-first-trial-map first-trial-map)

      (cond (seglob/all-participants-first-trial?)
            (if (dplev :io :all) (pprint "This is trail 1 of the experiment."))

            (seglob/all-participants-non-first-trial?)
            (if (dplev :io :all) (pprint "This is trail 2 of the experiment."))

            (seglob/all-participants-non-first-trial?)
            (if (dplev :io :all) (pprint "This is trail 2 of the experiment."))

            (seglob/mixed-participant-history?)
            (if (dplev :io :all :warn) (pprint "Something wrong with this team, we have partial prior knowledge."))

            :otherwise
            (if (dplev :io :all :error) (pprint "*** Something wrong can't get here in start-mission."))))


    ;; Introduce RITA to the participants
    (let [elapsed_milliseconds 0
          call-sign-map (seglob/get-player-callsign-map)
          _ (if (dplev :io :all) (do (println "Making introductions to:") (pprint call-sign-map)))
          interventions
          (remove
           nil?
           (into [] (map (fn [[callsign pid]]
                           (let [role (safe-trim (seglob/get-assigned-role pid))]
                             (if (dplev :all)
                               (println "callsign=" callsign "pid=" pid "role=" role))))
                         call-sign-map)))]
      (if elapsed_milliseconds (seglob/update-last-ms-time elapsed_milliseconds))
      interventions)))

(defn rita-handle-MissionState-message
  [tbm tbversion]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {mission :mission                              ; "Singleplayer" "Sparky", "Falcon", or ...
         mission-state :mission_state} (:data tbm)]    ; "Start"
    (start-mission {:mission-state mission-state,
                    :version version,
                    :trial-id trial-id
                    :experiment-id experiment-id
                    :mission mission})
    (cond (seglob/get-strike-mode)
          nil

          (not seglob/*mission-started*)
          (when (dplev :error :all)
            (println "***** UNEXPECTED MISSION STATE MESSAGE RECEIVED:" tbm)))))

;; {"msg":{"trial_id":"3a5266e9-f9ea-4020-86e3-0f295cc11f85",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:Scoreboard",
;;         "source":"simulator",
;;         "version":"0.5",
;;         "timestamp":"2020-06-19T12:22:36.308Z"},
;;  "data":{"scoreboard":{"Player96":10}},
;;  "header":{"message_type":"observation","version":"0.5","timestamp":"2020-06-19T12:22:36.307Z"}}

;; {"app-id":"TestbedBusInterface",
;;  "mission-id":"87da6996-7642-49bb-b114-8e428c87b5de",
;;  "routing-key":"testbed-message",
;;  "testbed-message":{"data":{"mission_timer":"12 : 9",
;;                             "elapsed_milliseconds":174450.0,
;;                             "scoreboard":{"Player818":0.0,"Player724":0.0,"Player981":10.0,"TeamScore":10.0}},
;;                             "header":{"timestamp":"2021-06-29T21:00:26.103Z",
;;                             "message_type":"observation",
;;                             "version":"0.6"},
;;                             "msg":{"experiment_id":"bc50aea2-b913-4889-9fbd-f4ba2a72763c",
;;                                    "trial_id":"87da6996-7642-49bb-b114-8e428c87b5de",
;;                                    "timestamp":"2021-06-29T21:00:26.103Z",
;;                                    "source":"simulator",
;;                                    "sub_type":"Event:Scoreboard",
;;                                    "version":"0.5"}},
;;  "timestamp":1625000426121,
;;  "received-routing-key":"testbed-message",
;;  "exchange":"rita"}

;;; note: playername is found in scoreboard where participantID should be
(defn rita-handle-scoreboard-message
  [tbm tbversion]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        playername (get-field-value "agentBeliefState.participant1" 'pname);+++ ***
        {scoreboard :scoreboard
         elapsed-milliseconds :elapsed_milliseconds} (:data tbm)
        ;;score (get scoreboard (keyword playername))
        score (apply max (vals scoreboard))
        ]
    (if elapsed-milliseconds (seglob/update-last-ms-time elapsed-milliseconds))
    (doseq [[pid score] scoreboard]
      (if (not (= pid :TeamScore))
        (slearn/add-score! (seglob/get-participant-id-from-player-name pid) score (/ elapsed-milliseconds 1000.0) scoreboard)))
    ;;(set-field-value! "agentBeliefState.psarticipant1" 'score score) ; +++ publish belief-state change? ;+++ ***
    (seglob/set-current-score-at! score elapsed-milliseconds)
    (when (dplev :all) (println "Scoreboard" scoreboard "vals=" (vals scoreboard) "score=" score))
    (let [pub (predict/predict-final-score nil nil (/ elapsed-milliseconds 1000.0) elapsed-milliseconds)
          ppub pub #_(study2/predict-m6-asist pub nil :initial-belief elapsed-milliseconds)]
      ppub)))

;; {"msg":{"trial_id":"a35aea6b-49aa-4d6e-9c0c-3f1e650e560a",
;;         "experiment_id":"9cbf3c96-3efa-4119-aa22-cd2561c641e6",
;;         "sub_type":"Event:location",
;;         "source":"IHMCLocationMonitorAgent",
;;         "version":"0.4",
;;         "timestamp":"2020-06-12T00:03:02.864Z"},
;;  "data":{"exited_area_id":"aw",
;;          "entered_area_id":"as",
;;          "entered_area_name":"Staging Area",
;;          "playername":"Player487",
;;          "exited_area_name":"Waiting Room"},
;;  "header":{"message_type":"event","version":"0.4","timestamp":"2020-06-12T00:03:02.867524Z"}}

(defn rita-handle-location-message
  [tbm tbversion]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {exited-area-id :exited_area_id
         entered-area-id :entered_area_id
         entered-area-name :entered_area_name
         exited-area-name :exited_area_name
         playername :playername} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))]
    #_(when (dplev :all) (println "IHMC: Player" pid "left" exited-area-name "and entered" entered-area-name "data=" (:data tbm))) ; too noisy
    nil))

;;; New messages in testbed 5.0

;;;      "start"
;;;
;; start tbm= {:data {:testbed_version 1.0,
;;                    :date 2020-08-19T23:12:11.3527Z,
;;                    :experiment_name ENTER EXPERIMENT NAME,
;;                    :group_number ,
;;                    :trial_number 000034,
;;                    :name 000023_Easy,
;;                    :experiment_author AT/LT,
;;                    :experiment_mission Falcon_Easy,
;;                    :experimenter AT/LT,
;;                    :condition 3,
;;                    :notes [N/A],
;;                    :study_number 1,
;;                    :subjects [000023]},
;;             :header {:timestamp 2020-08-19T23:12:11.3527Z,
;;                      :message_type trial,
;;                      :version 1.0},
;;             :msg {:sub_type start,
;;                   :source gui,
;;                   :experiment_id e0b770fa-d726-4c7e-8406-728dcfaf8b98,
;;                   :trial_id b66aa7e5-a16c-4abc-a2cb-5ae3bdc675e5,
;;                   :timestamp 2020-08-19T23:12:11.3527Z,
;;                   :version 1.0}}

;;; New version for Study 2 HSR

;; {"app-id":"TestbedBusInterface",
;;  "mission-id":"e69ec81c-0701-4d99-9760-6103bcdaebcf",
;;  "routing-key":"testbed-message",
;;  "testbed-message":{"data":{"testbed_version":"2.0.0-dev.526-c0c4f5c",
;;                             "date":"2021-06-24T01:47:19.6370Z",
;;                             "experiment_name":"TM000120",
;;                             "map_block_filename":"MapBlocks_SaturnA_1.5_xyz.csv",
;;                             "group_number":"1",
;;                             "trial_number":"TM000120_T000440",
;;                             "name":"TM000120_T000440",
;;                             "experiment_author":"MC/MW",
;;                             "experiment_mission":"Saturn_A",
;;                             "experimenter":"MC/MW",
;;                             "condition":"1",
;;                             "notes":["NA"],
;;                             "study_number":"2",
;;                             "subjects":["E000358"," E000359"," E000360"],
;;                             "map_name":"Saturn_1.6_3D",
;;                             "client_info":[{"playername":"RED_ASIST1",
;;                                             "staticmapversion":"SaturnA_64",
;;                                             "participantid":"E000358",
;;                                             "callsign":"Red",
;;                                             "participant_id":"E000358",
;;                                             "uniqueid":"GR097",
;;                                             "markerblocklegend":"A_Anne"},
;;                                            {"playername":"GREEN_ASIST1",
;;                                             "staticmapversion":"SaturnA_24",
;;                                             "participantid":"E000359",
;;                                             "callsign":"Green",
;;                                             "participant_id":"E000359",
;;                                             "uniqueid":"IV654",
;;                                             "markerblocklegend":"B_Sally"},
;;                                            {"playername":"BLUE_ASIST1",
;;                                             "staticmapversion":"SaturnA_34",
;;                                             "participantid":"E000360",
;;                                             "callsign":"Blue",
;;                                             "participant_id":"E000360",
;;                                             "uniqueid":"TQ907",
;;                                             "markerblocklegend":"A_Anne"}],
;;                             "experiment_date":"2021-06-24T01:47:19.6373Z"},
;;                     "header":{"version":"0.6",
;;                               "message_type":"trial",
;;                               "timestamp":"2021-06-24T01:47:19.6370Z"},
;;                     "msg":{"trial_id":"e69ec81c-0701-4d99-9760-6103bcdaebcf",
;;                            "source":"gui",
;;                            "sub_type":"start",
;;                            "timestamp":"2021-06-24T01:47:19.6370Z",
;;                            "experiment_id":"7a01f4d9-a3cb-4bb7-940c-4944729e11ea",
;;                            "version":"0.1"}},
;;  "timestamp":1626219596118,
;;  "received-routing-key":"testbed-message",
;;  "exchange":"rita"}

;;; All parts of SE state that must be reset on a per trial basis are run here.

(defn per-trial-resets
  []
  (if (dplev :all :io)
    (println "Resetting per trial data."))
  (seglob/reset-counters)
  (seglob/reset-players)                ; The players are introduced at the start of each trial
  (seglob/reset-roles-assigned)         ; Role assignments
  (seglob/reset-belief-state)
  (seglob/reset-message-count)          ; Message statistics on a per trial basis
  (seglob/reset-message-occurrence-order) ;ditto
  (seglob/reset-player-performance-histories) ; reset for each trial.
  (seglob/reset-players)                      ; Players announced at the start
  (seglob/reset-role-assignments)                ; +++ merge with above of similar name
  (seglob/reset-player-strength-data)
  (seglob/reset-leadership-votes)
  (seglob/reset-interventions-given)
  (measures/reset-m7-predictions-published)   ; Keep for historical runs.
  (measures/reset-demeanor-training-data)     ; Per trial
  (seglob/reset-considered-markers)           ; Per trial
  (seglob/reset-placed-markers!)
  (seglob/reset-considered-markers)
  (seglob/reset-victim-map)
  (seglob/reset-last-room)
  (seglob/reset-last-ms-time)
  (seglob/reset-last-epoch-reported)
  (rsc/reset-close-to)
  (cogload/reset-cognitive-load-counters)
  (slearn/reset-stat-record)
  (seglob/reset-last-time-addressed)
  (intervene/reset-per-trial-variables)
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Try to keep study2 and study3 differences separate
;;; We should back out of the study3 pieces that were put here before we split out study3
;;; when we have time.


(defn get-trial-number-from-expt-mission
  [expt-mission]
  (cond (clojure.string/includes? expt-mission "SATURN_A") 1
        (clojure.string/includes? expt-mission "SATURN_B") 2
        (clojure.string/includes? expt-mission "SATURN_C") 1
        (clojure.string/includes? expt-mission "SATURN_D") 2
        (clojure.string/includes? expt-mission "Saturn_A") 1
        (clojure.string/includes? expt-mission "Saturn_B") 2
        (clojure.string/includes? expt-mission "Saturn_C") 1
        (clojure.string/includes? expt-mission "Saturn_D") 2
        :otherwise 0))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; STUDY 2

(defn rita-handle-start-study2-message
  [tbm tb-version]
  (per-trial-resets)
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {tb-version :testbed_version
         expt :experiment_name
         grp :group_number
         trialno :trial_number
         name :name
         expt-auth :experiment_author
         expt-mission :experiment_mission
         experimenter :experimenter
         condition :condition
         notes :notes
         study :study_number
         subj-vec :subjects
         ;; New in study 2
         date :date
         experiment_name :experiment_name
         map_block_filename :map_block_filename
         trialID :trial_number
         map_name :map_name
         client_info :client_info
         experiment_date :experiment_date} (:data tbm)
        elapsed-milliseconds 0          ; This is the start, so em is zero :-)
        playernamemap (into {}
                            (remove nil?
                                    (map #(if (or (empty? (first %)) (empty? (second %))) nil %)
                                         (map (fn[client]
                                                [(get client :playername)
                                                 (or (get client :participantid) (get client :participant_id))])
                                              client_info))))
        callsignmap (into {}
                          (remove nil?
                                  (map #(if (or (empty? (first %)) (empty? (second %))) nil %)
                                       (map (fn[client] [(get client :callsign)
                                                         (or (get client :participantid) (get client :participant_id))])
                                            client_info))))
        callsignfrompidmap (into {}
                                 (remove nil?
                                         (map #(if (or (empty? (first %)) (empty? (second %))) nil %)
                                              (map (fn[client] [(or (get client :participantid) (get client :participant_id))
                                                                (get client :callsign)]) client_info))))
        teammembers  (into [] (remove nil? (map #(if (empty? %) nil %)
                                                (map (fn[client] (or (get client :participantid) (get client :participant_id)))
                                                     client_info))))
        subj-vec (into [] (remove nil? (map (fn [x] (if (empty? x) nil (string/trim x))) subj-vec)))
        trialnumber (get-trial-number-from-expt-mission expt-mission)]
    (if (dplev :all :io)
      (println "This is a study 2 trial."))
    (when (dplev :io :all)
      (println "trial number="       trialnumber)
      (println "trialID="            trialID)
      (println "playernamemap="      playernamemap)
      (println "callsignmap="        callsignmap)
      (println "callsignfrompidmap=" callsignfrompidmap)
      (println "teammembers="        teammembers)
      (println "subjects="           subj-vec))
    (seglob/set-trial-id trial-id)
    (seglob/set-experiment-id! experiment-id)
    (rsc/establish-prior-beliefs-about-map-assignment subj-vec)
    (study2/establish-prior-beliefs-about-marker-semantics subj-vec)
    (seglob/set-player-callsign-map! callsignmap)
    (seglob/set-pid-callsign-map! callsignfrompidmap)
    (seglob/set-team-members! teammembers)
    (seglob/set-player-name-map! playernamemap)
    (seglob/set-trial-number! trialnumber)
    (seglob/set-trialID! trialID) ; This is the trial ID like "T000123"
    (seglob/set-mission! expt-mission)
    (seglob/print-ac-usage)
    (let [estimated-map-name (seglob/translate-mission-name (seglob/get-mission))]
      (if estimated-map-name
        (do
          (seglob/set-estimated-map-name estimated-map-name)
          (when (dplev :all) (println "map name set to:" estimated-map-name)))))
    (seglob/select-trial-m7-ground-truth-prompt trialno)
    (slearn/initialize-stat-record experiment-id tb-version trial-id grp trialno name
                                   expt-auth expt-mission experimenter
                                   condition notes study map_name subj-vec)
    (seglob/set-mission-started true)
    (seglob/set-strike-mode false)
    (seglob/set-mission-ended-message-sent false)
    (set-random-seed! 666)
    (when (dplev :io :all) (println "**** Started testbed=" tb-version
             "trial="         trialno
             "mission="       expt-mission
             "condition="     condition
             "study number="  study
             "subjects="      subj-vec
             "playernamemap=" playernamemap))
    (when (dplev :io :all) (println "Loading" expt-mission "model"))
    (cond
      (clojure.string/includes? expt-mission "Training")
      (do
        (when (dplev :all :io)
          (println "*********************************************************************")
          (println "Are you guys crazy?  Are you trying to send me on a training mission?")
          (println "My Mama didn't raise me to do training missions. I just shan't do it.")
          (println "*********************************************************************")
          (anomaly/running-training-model expt-mission trial-id experiment-id 0)
          (seglob/set-mission-started false)
          (seglob/set-strike-mode true)))

      (clojure.string/includes? expt-mission "Falcon")
      (state-estimation-initialization "Falcon"
                                       study (get-trial-number-from-expt-mission expt-mission)
                                       version experiment-id teammembers)

      (clojure.string/includes? expt-mission "Saturn")
      (state-estimation-initialization "Saturn"
                                       study (get-trial-number-from-expt-mission expt-mission)
                                       version experiment-id teammembers)

      :otherwise
      (state-estimation-initialization expt-mission 0 0 version experiment-id teammembers))

    (when (not (seglob/get-strike-mode))
      (initialize-player-condition-priors "agentBeliefState.participant1" condition)
      (initialize-player-condition-priors "agentBeliefState.participant2" condition)
      (initialize-player-condition-priors "agentBeliefState.participant3" condition)
      (initialize-player-condition-priors "agentBeliefState.participant4" condition)
      (initialize-player-condition-priors "agentBeliefState.participant5" condition)
      (initialize-player-condition-priors "agentBeliefState.participant6" condition)

      ;; Initial prior belief publications
      (let [pub   (predict/predict-final-score nil nil 0.0 0)
            ppub  (rsc/update-map-assignment-and-report pub nil 0)
            pppub (markers/update-marker-semantics-and-report ppub nil :initial-belief 0)]
        pppub))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; STUDY 3

(defn rita-handle-start-study3-message
  [tbm tb-version]
  (per-trial-resets)
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {tb-version :testbed_version
         expt :experiment_name
         grp :group_number
         trialno :trial_number
         name :name
         expt-auth :experiment_author
         expt-mission :experiment_mission
         experimenter :experimenter
         condition :condition
         notes :notes
         study :study_number
         subj-vec :subjects
         ;; New in study 2
         date :date
         experiment_name :experiment_name
         map_block_filename :map_block_filename
         trialID :trial_number
         map_name :map_name
         client_info :client_info
         experiment_date :experiment_date} (:data tbm)
        elapsed-milliseconds 0          ; This is the start, so em is zero :-)
        playernamemap (into {}
                            (remove nil?
                                    (map #(if (or (empty? (first %)) (empty? (second %))) nil %)
                                         (map (fn[client]
                                                [(get client :playername)
                                                 (or (get client :participantid)
                                                     (get client :participant_id))])
                                              client_info))))
        callsignmap (into {}
                          (remove nil?
                                  (map #(if (or (empty? (first %)) (empty? (second %))) nil %)
                                       (map (fn[client] [(ras/strfromsymbol (get client :callsign))
                                                         (or (get client :participantid)
                                                             (get client :participant_id))])
                                            client_info))))
        callsignfrompidmap (into {}
                                 (remove nil?
                                         (map #(if (or (empty? (first %)) (empty? (second %))) nil %)
                                              (map (fn[client] [(or (get client :participantid)
                                                                    (get client :participant_id))
                                                                (get client :callsign)]) client_info))))
        teammembers  (into [] (remove nil? (map #(if (empty? %) nil %)
                                                (map (fn[client] (or (get client :participantid)
                                                                     (get client :participant_id)))
                                                     client_info))))
        subj-vec (into [] (remove nil? (map (fn [x] (if (empty? x) nil (string/trim x))) subj-vec)))
        ;; trialnumber (cond (clojure.string/includes? expt-mission "SATURN_A")
        ;;                   1
        ;;                   (clojure.string/includes? expt-mission "SATURN_B")
        ;;                   2
        ;;                   (clojure.string/includes? expt-mission "SATURN_C")
        ;;                   1
        ;;                   (clojure.string/includes? expt-mission "SATURN_D")
        ;;                   2
        ;;                   :otherwise 0)
        ;; rubble-perturbation   (clojure.string/includes? expt-mission "Rubble")
        ;; blackout-perturbation (clojure.string/includes? expt-mission "Blackout")
        trialnumber (get-trial-number-from-expt-mission expt-mission)
        rubble-perturbation   (clojure.string/includes? expt-mission "Rubble")
        blackout-perturbation (clojure.string/includes? expt-mission "Blackout")]
    (when (dplev :io :all)
      (println "This is a study 3 trial.")
      (println "expt-mission="       expt-mission)
      (println "trialnumber="        trialnumber)
      (println "trialID="            trialID)
      (println "playernamemap="      playernamemap)
      (println "callsignmap="        callsignmap)
      (println "callsignfrompidmap=" callsignfrompidmap)
      (println "teammembers="        teammembers)
      (println "subjects="           subj-vec))

    (seglob/set-trial-id trial-id)
    (seglob/set-experiment-id! experiment-id)
    ;; THe next two are study 2 specific, can we remove them from study 3 trials?
    #_(rsc/establish-prior-beliefs-about-map-assignment subj-vec)
    #_(study2/establish-prior-beliefs-about-marker-semantics subj-vec)

    (seglob/set-player-callsign-map! callsignmap)
    (seglob/set-pid-callsign-map! callsignfrompidmap)
    (seglob/set-team-members! teammembers)
    (seglob/set-player-name-map! playernamemap)
    (seglob/set-trialID! trialID)           ; This is the trial ID like "T000123"
    (seglob/set-trial-number! trialnumber)  ; This is the number of the trial withing the sequence, like 1, 2, or 3
    (seglob/set-mission! expt-mission)
    (seglob/set-strike-mode false)
    (seglob/print-ac-usage)

    (let [estimated-map-name (seglob/translate-mission-name (seglob/get-mission))]
      (if estimated-map-name
        (do
          (seglob/set-estimated-map-name estimated-map-name)
          (when (dplev :all) (println "map name set to:" estimated-map-name)))))

    ;; +++ study2 specific remove?
    #_(seglob/select-trial-m7-ground-truth-prompt trialno)

    (slearn/initialize-stat-record experiment-id tb-version trial-id grp trialno name
                                   expt-auth expt-mission experimenter
                                   condition notes study map_name subj-vec)

    (seglob/set-mission-started false) ; Let the mission start do this.
    (seglob/set-mission-ended-message-sent false)

    ;;(set-random-seed! 666)              ; For debugging reproducibility (maybe)

    (when (dplev :io :all) (println "**** Started testbed=" tb-version
                                    "trial="         trialno
                                    "mission="       expt-mission
                                    "condition="     condition
                                    "study number="  study
                                    "subjects="      subj-vec
                                    "playernamemap=" playernamemap))

    (when (dplev :io :all) (println "Loading" expt-mission "model"))
    (cond

      (clojure.string/includes? expt-mission "Training")
      (do
        (when (dplev :all :io)
          (println "*********************************************************************")
          (println "Are you guys crazy?  Are you trying to send me on a training mission?")
          (println "My Mama didn't raise me to do training missions. I just shan't do it.")
          (println "*********************************************************************")
          (anomaly/running-training-model expt-mission trial-id experiment-id 0)
          (seglob/set-mission-started false)
          (seglob/set-strike-mode true)))

      (clojure.string/includes? expt-mission "Falcon")
      (do
        (println "***** We shouldn't have a FALCON model for a study-3 trial SOMETHING IS WRONG *****")
        (state-estimation-initialization "Falcon"
                                         study (get-trial-number-from-expt-mission expt-mission)
                                         version experiment-id teammembers))

      (clojure.string/includes? expt-mission "Saturn")
      (state-estimation-initialization "Saturn"
                                       study (get-trial-number-from-expt-mission expt-mission)
                                       version experiment-id teammembers)

      :otherwise
      (do ;; +++ publish an anomaly here +++
        (println "***** We shouldn't have a non saturn model for a study-3 trial SOMETHING IS WRONG *****")
        (state-estimation-initialization expt-mission
                                         study (get-trial-number-from-expt-mission expt-mission)
                                         version experiment-id teammembers)))
    (when (not (seglob/get-strike-mode))
      (initialize-player-condition-priors "agentBeliefState.participant1" condition)
      (initialize-player-condition-priors "agentBeliefState.participant2" condition)
      (initialize-player-condition-priors "agentBeliefState.participant3" condition)
      (initialize-player-condition-priors "agentBeliefState.participant4" condition)
      (initialize-player-condition-priors "agentBeliefState.participant5" condition)
      (initialize-player-condition-priors "agentBeliefState.participant6" condition)

      ;; THIS IS JUST A TEST- WIRE IN PROPERLY, LATER
      ;;(anomaly/missing-participant-id-in nil "Red" "Medical_Specialist" "client_info" (rsc/timeinmilliseconds timestamp))
      ;; END OF TEST

      ;; Initial prior belief publications
      (let [pub   (predict/predict-final-score nil nil 0.0 0)
            ppub pub
            pppub (markers/update-marker-semantics-and-report ppub nil :initial-belief 0)
            ppppub pppub]                 ; Wire in our collected data here
        ppppub))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; trial start message

(defn as-integer
  [val]
  (let [res (cond (number? val) val
                  (string? val) (read-string val)
                  :otherwise false)]
    (println "In as-integer: val=" val " res=" res)
    res))

(defn rita-handle-trial-start-message
  [tbm tb-version]
  ;; (println "In trial start message with tb-version=" tb-version)
  ;; (pprint tbm)
  (if (number? tb-version)
    (let [{trial-id :trial_id
           experiment-id :experiment_id
           source  :source
           version :version
           timestamp :timestamp} (:msg tbm)
          {tb-version :testbed_version
           expt :experiment_name
           grp :group_number
           trialno :trial_number
           name :name
           expt-auth :experiment_author
           expt-mission :experiment_mission
           experimenter :experimenter
           condition :condition
           notes :notes
           study :study_number
           subj-vec :subjects} (:data tbm)]
      (slearn/initialize-stat-record experiment-id tb-version trial-id grp trialno name
                                     expt-auth expt-mission experimenter
                                     condition notes study subj-vec)
      (seglob/set-mission-started true) ; MissionStart handles this
      (seglob/set-mission-ended-message-sent false)
      (set-random-seed! 666)
      (when (dplev :io :all)
        (println "**** Started testbed=" tb-version "trial=" trialno "mission=" expt-mission
                 "condition=" condition "study number=" study "subjects=" subj-vec
                 "experiment-id=" experiment-id))
      (when (dplev :io :all) (println "Loading" expt-mission "model"))
      (cond
        (clojure.string/includes? expt-mission "Falcon")
        (state-estimation-initialization "Falcon"
                                         study (get-trial-number-from-expt-mission expt-mission)
                                         version experiment-id  (seglob/get-team-members))

        (clojure.string/includes? expt-mission "Saturn")
        (state-estimation-initialization "Saturn"
                                         study (get-trial-number-from-expt-mission expt-mission)
                                         version experiment-id  (seglob/get-team-members))

        :otherwise
        (state-estimation-initialization expt-mission
                                         study (get-trial-number-from-expt-mission expt-mission)
                                         version experiment-id  (seglob/get-team-members)))

      (initialize-player-condition-priors "agentBeliefState.participant1" condition)
      (initialize-player-condition-priors "agentBeliefState.participant2" condition)
      (initialize-player-condition-priors "agentBeliefState.participant3" condition)
      (initialize-player-condition-priors "agentBeliefState.participant4" condition)
      (initialize-player-condition-priors "agentBeliefState.participant5" condition)
      (initialize-player-condition-priors "agentBeliefState.participant6" condition)
      nil)

    (case (or (as-integer (get tbm :study_number)) 3)
      1 (if (dplev :all :io) (println "This is a study 1 trial."))
      2 (rita-handle-start-study2-message tbm tb-version)
      3 (rita-handle-start-study3-message tbm tb-version))))

;;;      "FoV"
;; Unhandled testbed message sub_type: FoV tbm= {:data {:playername ASIST5,
;;                                                      :observation 447.0,
;;                                                      :blocks [{:id 5123.0, :location [-2097.0 62.0 144.0], :type wall_sign,
;;                                                                :number_pixels 669.0, :bounding_box {:x [302.0 338.0], :y [188.0 206.0]}}
;;                                                               {:id 5124.0, :location [-2097.0 62.0 145.0], :type wall_sign,
;;                                                                :number_pixels 706.0, :bounding_box {:x [338.0 375.0], :y [188.0 206.0]}}
;;                                                               ...
;;                                                               {:id 5547.0, :location [-2096.0 61.0 148.0], :type acacia_door,
;;                                                                :number_pixels 1296.0, :bounding_box {:x [448.0 483.0], :y [216.0 251.0]}}]},
;;                                               :header {:message_type observation,
;;                                                        :version 0.5,
;;                                                        :timestamp 2020-08-19T23:14:41.028448Z},
;;                                               :msg {:sub_type FoV,
;;                                                     :experiment_id e0b770fa-d726-4c7e-8406-728dcfaf8b98,
;;                                                     :trial_id b66aa7e5-a16c-4abc-a2cb-5ae3bdc675e5,
;;                                                     :source PyGL_FoV_Agent,
;;                                                     :version 0.5,
;;                                                     :timestamp 2020-08-19T23:14:41.028463Z}}
(defn rita-handle-FoV-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {playername :playername
         observation :observation
         blocks-vec :blocks} (:data tbm)]
    ;; Do nothing with this data for now
    nil))


;;;       "Event:Pause"
;; sub_type: Event:Pause tbm= {:data {:mission_timer 9 : 0,
;;                                    :paused false},
;;                             :header {:timestamp 2020-08-19T23:17:19.853Z,
;;                                      :message_type event,
;;                                      :version 1.0},
;;                             :msg {:experiment_id e0b770fa-d726-4c7e-8406-728dcfaf8b98,
;;                                   :trial_id b66aa7e5-a16c-4abc-a2cb-5ae3bdc675e5,
;;                                   :timestamp 2020-08-19T23:17:19.853Z,
;;                                   :source simulator,
;;                                   :sub_type Event:Pause,
;;                                   :version 1.0}}

(defn rita-handle-event-pause-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {mission-timer :mission_timer
         paused :paused} (:data tbm)
        time-remaining (ras/parse-mission-time mission-timer)]
    (when (dplev :io :all) (println "Mission" (if paused "paused" "not paused") "mission timer=" mission-timer))
    ;; Do nothing with this data for now
    nil))

;;;          "stop"
;; sub_type: stop tbm= {:data {:testbed_version 1.0,
;;                             :date 2020-08-19T23:12:11.3527Z,
;;                             :experiment_name ENTER EXPERIMENT NAME,
;;                             :group_number ,
;;                             :trial_number 000034,
;;                             :name 000023_Easy,
;;                             :experiment_author AT/LT,
;;                             :experiment_mission Falcon_Easy,
;;                             :experimenter AT/LT,
;;                             :condition 3,
;;                             :notes [N/A],
;;                             :study_number 1,
;;                             :subjects [000023]},
;;                      :header {:timestamp 2020-08-19T23:30:32.3602Z,
;;                               :message_type trial,
;;                               :version 1.0},
;;                      :msg {:sub_type stop,
;;                            :source gui,
;;                            :experiment_id e0b770fa-d726-4c7e-8406-728dcfaf8b98,
;;                            :trial_id b66aa7e5-a16c-4abc-a2cb-5ae3bdc675e5,
;;                            :timestamp 2020-08-19T23:12:11.3527Z,
;;                            :version 1.0}}


(defn end-of-mission-wrapup
  []
  (when (dplev :io :all) (println "***** Mission ended *****"))

  (when seglob/*mission-started*
    (when (dplev :all) (println "***** Time's up, game over, put another quarter in *****"))
    #_(when (dplev :io :prediction :all) (println "A total of" (measures/get-m7-predictions-published) "M7 predictions generated"))
    (measures/reset-m7-predictions-published)
    ;; Set everyone's role to "None" so end times for current roles are recorded.
    (if seglob/*stat-record*
      (doseq [pid (.subjects seglob/*stat-record*)]
        (rsc/set-current-role! pid "None" (* 17 60 1000.0))))

    (predict/report-short-statistics)
    (predict/report-detailed-statistics)
    ;;(ts/save-participants-strength "unused")  ; Study2 version
    (ts/save-team-strength)                     ; Study3 version
    (slearn/write-stat-data)
    (seglob/set-mission-started false)
    (if (measures/exists-demeanor-training-data?)
      (measures/save-demeanor-training-data (str "demeanor-" (seglob/get-trial-id) "-td.data")))
    (anomaly/publish-results-at-end-of-mission (seglob/get-last-ms-time))
    (seglob/set-mission-terminated true)
    (seglob/set-mission-ended-time (seglob/rita-ms-time))
    (report-leadership-statistics)))

(defn rita-handle-trial-stop-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)]
    (end-of-mission-wrapup)
    (when (= (System/getenv "RITA_SE_EXIT_ON_STOP") "True")
      (when (dplev :io :all) (println "Exiting RITA_SE"))
      (System/exit 0))
    nil))



;;; "Event:ProximityBlockInteraction"
;;; sub_type: Event:ProximityBlockInteraction
;;; tbm=
;;; {:data {:action_type ENTERED_RANGE,
;;;         :victim_y 60.0,
;;;         :victim_id 17.0,
;;;         :elapsed_milliseconds 807297.0,
;;;         :players_in_range 1.0,
;;;         :mission_timer 1 : 36,
;;;         :playername Player981,
;;;         :victim_x -2126.0,
;;;         :victim_z 15.0},
;;;  :header {:timestamp 2021-06-29T21:10:58.950Z,
;;;           :message_type event,
;;;           :version 0.6},
;;;  :msg {:experiment_id bc50aea2-b913-4889-9fbd-f4ba2a72763c,
;;;        :trial_id 87da6996-7642-49bb-b114-8e428c87b5de,
;;;        :timestamp 2021-06-29T21:10:58.950Z,
;;;        :source simulator,
;;;        :sub_type Event:ProximityBlockInteraction,
;;;        :version 1.1}}

(defn rita-handle-ProximityBlockInteraction-message
  [tbm tb-version]
  (let [data (:data tbm)
        header (:header tbm)
        msg (:msg tbm)
        {em :elapsed_milliseconds
         action_type :action_type
         victim_x :victim_x
         victim_y :victim_y
         victim_z :victim_z
         players_in_range :players_in_range
         victim_id :victim_id
         medic_player_name :medic_player_name} data
        pid (seglob/get-participant-id-from-data data)]
    (if em (seglob/update-last-ms-time em))
    (when (dplev :action :all) (println "Participant " pid action_type "victim_id" victim_id "at x=" victim_x "y=" victim_y "z" victim_z "players in range=" players_in_range))
    nil))



;; testbed message sub_type: Status:SurveyResponse tbm=
;; {:data {:survey_response {"responseId":"R_33Dbs9bxWgaVOXw",
;;                           "values":{"startDate":"2021-06-24T01:48:00Z",
;;                                     "endDate":"2021-06-24T01:50:13Z",
;;                                     "status":0,
;;                                     "ipAddress":"24.44.20.235",
;;                                     "progress":100,
;;                                     "duration":133,
;;                                     "finished":1,
;;                                     "recordedDate":"2021-06-24T01:50:13.957Z",
;;                                     "_recordId":"R_33Dbs9bxWgaVOXw",
;;                                     "locationLatitude":"40.86810302734375",
;;                                     "locationLongitude":"-73.40940093994140625",
;;                                     "distributionChannel":"anonymous",
;;                                     "userLanguage":"EN",
;;                                     "QID68":1,
;;                                     "QID57":2,
;;                                     "QID58":3,
;;                                     "QID59":3,
;;                                     "QID60":2,
;;                                     "QID61":4,
;;                                     "QID64_0_GROUP":["14","16","13","15","18"],
;;                                     "QID64_G0_13_RANK":3,
;;                                     "QID64_G0_14_RANK":1,
;;                                     "QID64_G0_15_RANK":4,
;;                                     "QID64_G0_16_RANK":2,
;;                                     "QID64_G0_18_RANK":5,
;;                                     "QID65":4,
;;                                     "QID66":5,
;;                                     "QID67_7":4,
;;                                     "QID67_2":4,
;;                                     "QID67_3":2,
;;                                     "Q_URL":"https://iad1.qualtrics.com/jfe/form/SV_3CAydNnS2D0UEaq?particpantid=E000360&surveyname=Section5_Mission%202%20prep&uniqueid=TQ907",
;;                                     "surveyname":"Section5_Mission 2 prep",
;;                                     "uniqueid":"TQ907"},
;;                           "labels":{"status":"IP Address",
;;                                     "finished":"True",
;;                                     "QID68":"Group 1",
;;                                     "QID57":"Medical Specialist (Medic)",
;;                                     "QID58":"Search Specialist (Searcher)",
;;                                     "QID59":"Search Specialist (Searcher)",
;;                                     "QID60":"Middle section",
;;                                     "QID61":"I don't know",
;;                                     "QID64_0_GROUP":["RD","RE","RA","RB","RC"],
;;                                     "QID65":"Effective  4 ",
;;                                     "QID66":"Strongly agree\n5",
;;                                     "QID67_7":"Fairly confident  4",
;;                                     "QID67_2":"Fairly confident  4",
;;                                     "QID67_3":"Slightly confident 2"},
;;                           "displayedFields":["QID59",
;;                                              "QID64_G0_2_RANK",
;;                                              "QID64_G0_12_RANK",
;;                                              "QID64_G0_5_RANK",
;;                                              "QID64_G0_8_RANK",
;;                                              "QID64_G0_7_RANK",
;;                                              "QID64_G0_16_RANK",
;;                                              "QID64_G0_1_RANK",
;;                                              "QID58",
;;                                              "QID57",
;;                                              "QID64_G0_10_RANK",
;;                                              "QID64_0_GROUP",
;;                                              "QID64_G0_13_RANK",
;;                                              "QID64_G0_4_RANK",
;;                                              "QID58_4_TEXT",
;;                                              "QID64_G0_11_RANK",
;;                                              "QID67_2","QID67_3",
;;                                              "QID64_G0_6_RANK",
;;                                              "QID67_7",
;;                                              "QID64_G0_3_RANK",
;;                                              "QID64_G0_17_RANK",
;;                                              "QID59_4_TEXT",
;;                                              "QID64_G0_14_RANK",
;;                                              "QID57_4_TEXT",
;;                                              "QID61",
;;                                              "QID60",
;;                                              "QID57_5_TEXT",
;;                                              "QID68",
;;                                              "QID59_5_TEXT",
;;                                              "QID64_G0_15_RANK",
;;                                              "QID58_5_TEXT",
;;                                              "QID66",
;;                                              "QID64_G0_9_RANK",
;;                                              "QID65",
;;                                              "QID64_G0_18_RANK",
;;                                              "QID62"],
;;                           "displayedValues":{"QID59":[1,2,3],
;;                                              "QID64_G0_2_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_G0_12_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_G0_5_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_G0_8_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_G0_7_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_G0_16_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_G0_1_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID58":[1,2,3,4],
;;                                              "QID57":[1,2,3],
;;                                              "QID64_G0_10_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_0_GROUP":["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18"],
;;                                              "QID64_G0_13_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_G0_4_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_G0_11_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID67_2":[1,2,3,4,5],
;;                                              "QID67_3":[1,2,3,4,5],
;;                                              "QID64_G0_6_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID67_7":[1,2,3,4,5],
;;                                              "QID64_G0_3_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_G0_17_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID64_G0_14_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID61":[1,2,3,4],
;;                                              "QID60":[1,2,3,4],
;;                                              "QID68":[1,2,3],
;;                                              "QID64_G0_15_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID66":[1,2,3,4,5],
;;                                              "QID64_G0_9_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID65":[1,2,3,4,5],
;;                                              "QID64_G0_18_RANK":[1,2,3,4,5,6,7,8,9,10.0,11,12,13,14,15,16,17,18],
;;                                              "QID62":[1,2,3]}}}
 ;; :header {:version 0.1,
 ;;          :message_type status,
 ;;          :timestamp 2021-06-24T01:50:41.0565Z},
 ;; :msg {:trial_id e69ec81c-0701-4d99-9760-6103bcdaebcf,
 ;;       :timestamp 2021-06-24T01:50:41.0565Z,
 ;;       :source asistdataingester,
 ;;       :sub_type Status:SurveyResponse,
 ;;       :experiment_id 7a01f4d9-a3cb-4bb7-940c-4944729e11ea,
 ;;       :version 0.4}}

(defn rita-handle-survey-data-message
  [tbm tb-version]
  (let [data (:data tbm)
        header (:header tbm)
        msg (:msg tbm)
        {survey_response :survey_response} data]
    (when (dplev :io :all) (println "Survey data found"))
    nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Recently unhandled messages

;; message_type= event
;; sub_type= Event:PerturbationRubbleLocations
;; tbm= {:data {
;;         :mission_timer 8 : 0,
;;         :elapsed_milliseconds 543069.0,
;;         :mission Saturn_B_Rubble,
;;         :mission_blockage_list [{:x -2157.0, :y 60.0, :z -1.0,
;;                                  :block_type gravel,
;;                                  :room_name NA,
;;                                  :feature_type obstruction}
;;                                 ...]},
;;       :header {:timestamp 2022-03-22T16:13:28.270Z,
;;                :message_type event,
;;                :version 1.1},
;;       :msg {:experiment_id 4b3dc21b-bf17-4368-b1f7-ef63fa8f17aa,
;;             :trial_id de247af4-3937-4060-8a22-2c0fcda60cfc,
;;             :timestamp 2022-03-22T16:13:28.270Z,
;;             :source simulator,
;;             :sub_type Event:PerturbationRubbleLocations,
;;             :version 2.0}}
;;; +++ wire me in +++

;;; testbed message_type= export sub_type= trial
;;; tbm= {:data {
;;;        :index logstash-2022.03.02-000001,
;;;        :metadata {
;;;          :trial {
;;;            :testbed_version 3.5.1-dev.84-1c1474c,
;;;            :date 2022-03-02T18:32:30.345900Z,
;;;            :experiment_name test-a-thon 2022 03 02,
;;;            :group_number 1,
;;;            :trial_number 1,
;;;            :name test-a-thon-2-Saturn-A,
;;;            :experiment_author jcr,
;;;            :experiment_mission Saturn,
;;;            :experimenter jcr,
;;;            :condition 1,
;;;            :notes [test-a-thon 2],
;;;            :study_number 1,
;;;            :subjects [Aptiomiomer1],
;;;            :experiment_date 2022-03-02T18:15:51.238100Z}}},
;;;        :header {
;;;          :timestamp 2022-03-02T19:48:27.136Z,
;;;          :message_type export, :version 1.0},
;;;        :msg {
;;;          :sub_type trial,
;;;          :source metadata-web,
;;;          :experiment_id f97a5942-790d-4b85-b5cb-fc648bba3cc1,
;;;          :trial_id 55cd3a31-548a-4d5b-9376-6f78a93545d6,
;;;          :timestamp 2022-03-02T19:48:27.136Z,
;;;          :version 3.5.1-dev.84-1c1474c}}

(defn rita-handle-export-trial
  [tbm tb-version]
  ;+++ do something here
  　nil)


;; message_type= control
;; sub_type= nil


;; message_type= event
;; sub_type= Event:PuzzleTextSummary
;; tbm= {:data
;;        {:missionName Saturn_A_Blackout,
;;         :medical_specialist_puzzle_summary [MEETING ATTENDANTS],
;;         :engineering_specialist_puzzle_summary [ROOM DAMAGE SEVERITY],
;;         :transport_specialist_puzzle_summary [MEETING LOCATIONS]},
;;       :header {:timestamp 2022-03-22T15:44:21.256Z,
;;                :message_type event,
;;                :version 2.0},
;;       :msg {:experiment_id 4b3dc21b-bf17-4368-b1f7-ef63fa8f17aa,
;;             :trial_id a3896ac5-a01f-4874-8bf8-f221140469ba,
;;             :timestamp 2022-03-22T15:44:21.256Z,
;;             :source simulator,
;;             :sub_type Event:PuzzleTextSummary,
;;             :version 2.0}}

(defn rita-handle-PuzzleTextSummary-message
  [tbm tb-version]
  nil)

;; message_type= event
;; sub_type= Event:PlanningStage
;; tbm= {:data
;;        {:mission_timer 15 : 0,
;;         :elapsed_milliseconds 123002.0,
;;         :state Stop},
;;       :header {:timestamp 2022-03-22T15:46:24.242Z,
;;                :message_type event, :version 1.1},
;;       :msg {:experiment_id 4b3dc21b-bf17-4368-b1f7-ef63fa8f17aa,
;;             :trial_id a3896ac5-a01f-4874-8bf8-f221140469ba,
;;             :timestamp 2022-03-22T15:46:24.242Z,
;;             :source simulator,
;;             :sub_type Event:PlanningStage,
;;             :version 2.0}}

(defn handle-chat-message
  [tbm subtype tb-version]
  (case subtype

    "Event:Chat"
    (lang/rita-handle-chat-message tbm tb-version)

    (do (when (dplev :all :unhandled :io)
          (println "Unhandled testbed message_type= chat sub_type=" subtype "tbm=" tbm))
        nil)))

(defn handle-legacy-message
  [tbm subtype tb-version]
  (case subtype
    ;; System messages

    "state"
    (rita-handle-state-message tbm tb-version)

    "Event:MissionState"
    (rita-handle-MissionState-message tbm tb-version)

    "Event:Scoreboard"
    (rita-handle-scoreboard-message tbm tb-version)

    "start"
    (rita-handle-trial-start-message tbm tb-version)

    "stop"
    (rita-handle-trial-stop-message tbm tb-version)

    "Event:Pause"
    (rita-handle-event-pause-message tbm tb-version)

    "FoV"
    (rita-handle-FoV-message tbm tb-version)

    ;; Player messages

    "Event:ItemEquipped"
    (players/rita-handle-ItemEquipped-message tbm tb-version)

    "Event:Door"
    (players/rita-handle-Door-message tbm tb-version)

    "Event:Lever"
    (players/rita-handle-Lever-message tbm tb-version)

    "Event:Triage"
    (players/rita-handle-Triage-message tbm tb-version)

    "Event:location"
    (rita-handle-location-message tbm tb-version)

    "Event:ItemDrop"
    (players/rita-handle-ItemDropped-message tbm tb-version)

    "Event:PlayerSprinting"
    (players/rita-handle-player-sprinting-message tbm tb-version)

    "Event:PlayerJumped"
    (players/rita-handle-player-jumped-message tbm tb-version)

    "Event:ItemUsed"
    (players/rita-handle-event-item-used-message tbm tb-version)

    "Event:PlayerSwinging"
    (players/rita-handle-player-swinging-message tbm tb-version)

    "Event:RoleSelected"
    (players/rita-handle-RoleSelected-message tbm tb-version)

    "Event:ToolUsed"
    (players/rita-handle-ToolUsed-message tbm tb-version)

    "Event:PlayerFrozenStateChange"
    (players/rita-handle-PlayerFrozenStateChange-message tbm tb-version)

    ;; Building messages

    "Event:Beep"
    (building/rita-handle-beep-message tbm tb-version)

    "Mission:BlockageList"
    (building/rita-handle-blockage-list-message tbm tb-version)

    "Event:RubbleDestroyed"
    (building/rita-handle-RubbleDestroyed-message tbm tb-version)

    "Event:ToolDepleted"
    (building/rita-handle-ToolDepleted-message tbm tb-version)

    ;; Victim messages

    "Event:VictimsExpired"
    (victims/rita-handle-event-victims-expired-message tbm tb-version)

    "Mission:VictimList"
    (vol/rita-handle-victim-list-message tbm tb-version)

    ;; ASR Voice and Chat

    "Event:Chat"
    (lang/rita-handle-chat-message tbm tb-version)

    "Event:VictimPickedUp"
    (victims/rita-handle-VictimPickedUp-message tbm tb-version)

    "VictimPlaced"
    (victims/rita-handle-VictimPlaced-message tbm tb-version)

    "Event:VictimPlaced"
    (victims/rita-handle-VictimPlaced-message tbm tb-version)

    "Status:UserSpeech"
    (lang/rita-handle-UserSpeech-message tbm tb-version)


    ;; Markers
    "Event:MarkerPlaced"
    (markers/rita-handle-MarkerPlaced-message tbm tb-version)

    "Event:ProximityBlockInteraction"
    (rita-handle-ProximityBlockInteraction-message tbm tb-version)

    "asr"
    (lang/rita-handle-asr-message tbm tb-version)

    "asr:transcription"
    (lang/rita-handle-asr-transcription-message tbm tb-version)

    "asr:alignment"
    (lang/rita-handle-asr-alignment-message tbm tb-version)

    "Event:dialogue_event"
    nil                    ; Ignore for now

    "SemanticMap:Initialized"
    nil                    ; Not using at present, but maybe we could, in the future convert this to Pamela

    "audio"                ; Not using at present
    nil

    "Mission:FreezeBlockList"
    nil                    ; Ignore for now (probably shouldn't touch this, since it is ground truth)

    "Mission:ThreatSignList"
    nil                    ; Ignore for now (probably shouldn't touch this, since it is ground truth)

    "measures"
    nil                    ; Ignore for now (probably shouldn't touch this, since it is ground truth)

    "Status:SurveyResponse"
    (rita-handle-survey-data-message tbm tb-version)

    ;; From study 3

    "Status:PlayerName"    ; Ignore for now
    nil

    "Event:dyad"           ; Ignore for now
    nil

    "Event:proximity"      ; Ignore for now
    nil

    "FoV:VersionIn<fo"      ; Ignore for now
    nil

    "heartbeat"            ; Ignore for now
    nil

    "versioninfo"          ; Ignore for now
    nil

    nil
    nil                    ; Ignore sub_type = nil messages

    (when (dplev :all :unhandled :io)
      (println "Unhandled legacy testbed message_type= (:message-type (:header tbm)) sub_type=" subtype "tbm=" tbm))))

(defn handle-agent-message
  [tbm subtype tb-version]
  (case subtype

    "Status:UserSpeech"
    (lang/rita-handle-UserSpeech-message tbm tb-version)

    ;; Added for study 2 evaluation testbed

    "asr"
    (lang/rita-handle-asr-message tbm tb-version)

    "asr:alignment"
    (lang/rita-handle-asr-alignment-message tbm tb-version)


    "audio"                ; Not using at present
    nil

    "measures"
    nil                    ; Ignore for now (probably shouldn't touch this, since it is ground truth)

    ;; From study 3

    "Status:PlayerName"    ; Ignore for now
    (players/rita-handle-PlayerName-message tbm tb-version)

    "FoV:VersionInfo"      ; Ignore for now since we are not using FoV
    nil

    ;; CMU

    "AC:BEARD"
    (acs/rita-handle-ac_beard-message tbm tb-version)

    "AC:TED"
    (acs/rita-handle-ac_ted-message tbm tb-version)

    ;; IHMC?

    "FoV:Profile"
    (acs/rita-handle-ac_fov_profile-message tbm tb-version)

    ;; Cornell

    "AC:Goal_alignment"
    (acs/rita-handle-ac_goal_alignment-message tbm tb-version)

    "AC:Player_compliance"
    (acs/rita-handle-ac_player_compliance-message tbm tb-version)

    "rollcall:response"
    (acs/rita-handle-ac_rollcall_response-message tbm tb-version)

    ;; UAZ

    "versioninfo"
    (acs/rita-handle-versioninfo-message tbm tb-version)

    ;; UCF

    "playerprofile"
    (acs/rita-handle-ac_ucf_ta2_playerprofile-message tbm tb-version)

    ;; Rutgers

    "AC:belief_diff"
    (acs/rita-handle-ac_belief_diff-message tbm tb-version)

    "AC:threat_room_communication"
    (acs/rita-handle-threat_room_communication-message tbm tb-version)

    "AC:threat_room_coordination"
    (acs/rita-handle-threat_room_coordination-message tbm tb-version)

    "AC:victim_type_communication"
    (acs/rita-handle-victim_type_communication-message tbm tb-version)

    ;; CMUFMS

    "Measure:cognitive_load"
    (acs/rita-handle-cognitive-load-message tbm tb-version)

    nil
    nil                    ; Ignore sub_type = nil messages

    (when (dplev :all :unhandled :io) (println "Unhandled testbed message_type= agent sub_type=" subtype "tbm=" tbm))))


(defn handle-event-message
  [tbm subtype tb-version]
  (case subtype

    ;; IHMC

    "Event:Summary"
    (acs/rita-handle-Event_Summary-message tbm tb-version)

    "Event:Utility"
    (acs/rita-handle-Utility-message tbm tb-version)

    "Event:Preparing"
    (acs/rita-handle-Preparing-message tbm tb-version)

    "Event:Addressing"
    (acs/rita-handle-Addressing-message tbm tb-version)

    "Event:Discovered"
    (acs/rita-handle-Discovered-message tbm tb-version)

    "Event:Awareness"
    (acs/rita-handle-Awareness-message tbm tb-version)

    "Event:Completion"
    (acs/rita-handle-Completion-message tbm tb-version)

    ;; Player events

    "Event:ItemEquipped"
    (players/rita-handle-ItemEquipped-message tbm tb-version)

    "Event:Door"
    (players/rita-handle-Door-message tbm tb-version)

    "Event:Lever"
    (players/rita-handle-Lever-message tbm tb-version)

    "Event:Triage"
    (players/rita-handle-Triage-message tbm tb-version)

    "Event:ItemDrop"
    (players/rita-handle-ItemDropped-message tbm tb-version)

    "Event:Beep"
    (building/rita-handle-beep-message tbm tb-version)

    "Event:PlayerSprinting"
    (players/rita-handle-player-sprinting-message tbm tb-version)

    "Event:PlayerJumped"
    (players/rita-handle-player-jumped-message tbm tb-version)

    "Event:VictimsExpired"
    (victims/rita-handle-event-victims-expired-message tbm tb-version)

    "Event:ItemUsed"
    (players/rita-handle-event-item-used-message tbm tb-version)

    "Event:PlayerSwinging"
    (players/rita-handle-player-swinging-message tbm tb-version)

    "Event:RoleSelected"
    (players/rita-handle-RoleSelected-message tbm tb-version)

    "Event:ToolUsed"
    (players/rita-handle-ToolUsed-message tbm tb-version)

    "Event:PlayerFrozenStateChange"
    (players/rita-handle-PlayerFrozenStateChange-message tbm tb-version)


    ;; Testbed Events

    "Event:MissionState"
    (rita-handle-MissionState-message tbm tb-version)

    "Event:location"
    (rita-handle-location-message tbm tb-version)

    "Event:Pause"
    (rita-handle-event-pause-message tbm tb-version)

    "Event:ProximityBlockInteraction"
    (rita-handle-ProximityBlockInteraction-message tbm tb-version)

    "Event:PuzzleTextSummary"
    (rita-handle-PuzzleTextSummary-message tbm tb-version)

    ;; Building events

    "Event:RubbleDestroyed"
    (building/rita-handle-RubbleDestroyed-message tbm tb-version)

    "Event:ToolDepleted"
    (building/rita-handle-ToolDepleted-message tbm tb-version)

    "Event:Perturbation"
    (building/rita-handle-perturbation-message tbm tb-version)

    "Event:RubbleCollapse"
    (building/rita-handle-rubble-collapse-message tbm tb-version)

    "Event:Signal"
    (building/rita-handle-signal-message tbm tb-version)

    "Event:PerturbationRubbleLocations"
    (building/rita-handle-PerturbationRubbleLocations-message tbm tb-version)

    ;; Victim events

    "Event:VictimPickedUp"
    (victims/rita-handle-VictimPickedUp-message tbm tb-version)

    "Event:VictimEvacuated"
    (victims/rita-handle-victim-evacuated-message tbm tb-version)

    "Event:VictimPlaced" ;     "VictimPlaced"
    (victims/rita-handle-VictimPlaced-message tbm tb-version)

    ;; Marker events

    "Event:MarkerPlaced"
    (markers/rita-handle-MarkerPlaced-message tbm tb-version)

    "Event:MarkerRemoved"
    (markers/rita-handle-marker-removal-message tbm tb-version)

    ;; Gallup events
    "agent:gallup_agent_gelp"           ; Now this
    (acs/rita-handle-ac_gallup_ta2_gelp-message tbm tb-version)

    "agent:ac_gallup_ta2_gelp"          ; Was this
    (acs/rita-handle-ac_gallup_ta2_gelp-message tbm tb-version)

    "standard"
    (acs/rita-handle-ac_gallup_ta2_standard-message tbm tb-version)

    "bullion"
    (acs/rita-handle-ac_gallup_ta2_bullion-message tbm tb-version)

    "Event:dialogue_event" ; Ignore for now
    (lang/rita-handle-dialogue_event-message tbm tb-version)

    "Event:PlanningStage"
    (acs/rita-handle-PlanningStage-message tbm tb-version)

    "Event:dyad"           ; Ignore for now
    (do
      (when (and false (not (empty? tbm)) (dplev :warn :unhandled))
        (println "Unhandled Event:dyad message  received:")
        (pprint tbm))
      nil)

    "Event:proximity"      ; Ignore for now
    (do
      (when (and false (not (empty? tbm)) (dplev :warn :unhandled))
        (println "Unhandled Event:proximity message  received:")
        (pprint tbm))
      nil)

    (do (when (dplev :all :unhandled :io)
          (println "Unhandled testbed message_type= event sub_type=" subtype "tbm=" tbm))
        nil)))

(defn handle-export-message
  [tbm subtype tb-version]
  (case subtype
    "trial"
    (rita-handle-export-trial tbm tb-version)

    (do (when (dplev :all :unhandled :io)
          (println "Unhandled testbed message_type= export sub_type=" subtype "tbm=" tbm))
        nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; GROUNDTRUTH messages

(defn handle-groundtruth-message
  [tbm subtype tb-version]
  (case subtype
    "Mission:FreezeBlockList"
    (building/rita-handle-FreezeBlockList tbm tb-version)

    "Mission:ThreatSignList"
    (gt/rita-handle-mission_threatsignlist-message tbm tb-version)

    "Mission:RoleText"
    (players/rita-handle-RoleText-message tbm tb-version)

    "Mission:BlockageList"
    (building/rita-handle-blockage-list-message tbm tb-version)

    "Mission:VictimList"
    (vol/rita-handle-victim-list-message tbm tb-version)

    "SemanticMap:Initialized"
    nil                    ; Not using at present, but maybe we could, in the future convert this to Pamela

    (do (when (dplev :all :unhandled :io)
          (println "Unhandled testbed message_type= groundtruth sub_type=" subtype "tbm=" tbm))
        nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; OBSERVATION messages

(defn handle-observation-message
  [tbm subtype tb-version]
  (case subtype
    "state"
    (rita-handle-state-message tbm tb-version)

    "FoV"
    (rita-handle-FoV-message tbm tb-version)

    "Event:Scoreboard"
    (rita-handle-scoreboard-message tbm tb-version)

    "asr:transcription"
    (lang/rita-handle-asr-transcription-message tbm tb-version)

    "Mission:ThreatSignList"
    (gt/rita-handle-mission_threatsignlist-message)

    (do (when (dplev :all :unhandled :io)
          (println "Unhandled testbed message_type= observation sub_type=" subtype "tbm=" tbm))
        nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; STATUS messages

(defn rita-handle-heartbeat
  [tbm tb-version]
  nil)

(defn handle-status-message
  [tbm subtype tb-version]
  (case subtype
    "Status:SurveyResponse"
    nil ;(rita-handle-survey-data-message tbm tb-version) ;Ignore for now

    "heartbeat"            ; Ignore for now
    (rita-handle-heartbeat tbm tb-version)

    (do (when (dplev :all :unhandled :io)
          (println "Unhandled testbed message_type= status sub_type=" subtype "tbm=" tbm))
        nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; TRIAL messages

(defn handle-trial-message
  [tbm subtype tb-version]
  (case subtype
    "start"
    (do
      (rita-handle-trial-start-message tbm tb-version)
      ;;(println "###### trial start message ######")
      ;;(pprint tbm)
      )

    "stop"
    (rita-handle-trial-stop-message tbm tb-version)

    "state"
    (do
      (if (dplev :warn :unhandled)
        (println "Unhandled trial-state message  received:")
        (pprint tbm))
      nil); (rita-handle-trial-state-message tbm tb-version)

    "versioninfo"          ; Ignore for now
    (do
      (if (dplev :warn :unhandled)
        (println "Unhandled trial-versioninfo message  received:")
        (pprint tbm))
      nil); (rita-handle-trial-version-message tbm tb-version)

    (do (when (dplev :all :unhandled :io)
          (println "Unhandled testbed message_type= trial sub_type=" subtype "tbm=" tbm))
        nil)))

(defn record-message
  [message-type subtype message]
  (let [msgkey [message-type subtype]
        prv (get (seglob/get-message-count) msgkey)]
    (cond
      ;; We have seen this message type before increment its count
      prv
      (let []
        ;;(println "New message? prv=" prv "message count is:")
        ;;(pprint (seglob/get-message-count))
        (seglob/set-message-count (merge (seglob/get-message-count) {msgkey (+ prv 1)}))
        ;;(println "message count is now:")
        #_ (pprint (seglob/get-message-count)))

      ;; This is the first time we have seen this message type
      :otherwise
      (let []
        ;; (println "New message? prv=" prv "message count is:")
        ;; (pprint (seglob/get-message-count))
        (seglob/set-message-count (merge (seglob/get-message-count) {msgkey 1}))
        ;; (println "message count is now:")
        ;; (pprint (seglob/get-message-count))
        (when (dplev :all :io :msgorder)
          (println "*** First instance of message of type " msgkey "observed")
          (if (dplev :all :msgorder-full)
            (pprint message)))
        (seglob/add-message-occurrence msgkey)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Toplevel message dispatcher

;;; Dispatch message processing
(defn rita-se-handle-message
  [tbm]
  (let [{;;version :version
         message-type :message_type
         timestamp :timestamp} (:header tbm)
        {subtype :sub_type
         version :version} (:msg tbm)
        ;;Temporary hack to deal with typo with "Status:SurveyResponse"
        subtype (or subtype (get (:msg tbm) :subtype)) ;;sub_type vs subtype
        tb-version (case (clojure.string/trim (str version))
                     "0.1"    :tb1
                     "0.1.1"  :tb011
                     "0.2"    :tb2
                     "0.3"    :tb3
                     "0.4"    :tb4
                     "0.5"    :tb5
                     "0.5.2"  :tb052
                     "1.0"    :tb100
                     "1.01"   :tb101
                     "1.02"   :tb102
                     "1.1"    :tb110
                     "1.2.8"  :tb128
                     "2.0"    :tb200
                     "2.1"    :tb210
                     "3.0"    :tb300
                     "3.5.1"  :tb351
                     "4.0.6"  :tb406

                     (do (println "Testbed version=" version)
                         :tb5))              ; Default to testbed 5 update after future releases!
        timestring (:mission_timer (:data tbm))
        time-remaining (ras/parse-mission-time timestring)]
        (cond (seglob/get-strike-mode)
              (do
                (if (and (= message-type "event")
                         (= subtype "Event:MissionState"))
                  (rita-handle-MissionState-message tbm tb-version))
                nil)

              :otherwise
              (do
                (if timestamp
                  (seglob/set-header-time  timestamp))
                (seglob/set-last-received-message tbm)
                (when (and (seglob/get-mission-terminated)
                           (= (System/getenv "RITA_EXIT_AT_END_OF_MISSION") "true")
                           (number? (seglob/get-mission-ended-time))
                           (> (- (seglob/rita-ms-time) (seglob/get-mission-ended-time)) 2000)) ;+++ constant +++
                  ;; Wait two seconds after mission ends before exiting to allow any final messages to flow
                  (when (dplev :io :all) (println "Exiting..."))
                  (System/exit 0))

                (set-field-value! "agentBeliefState.participant1" 'seconds-remaining time-remaining);+++ ***
                (when  (and seglob/*mission-started* (= (:mission_timer (:data tbm)) "0 : 0"))
                  (when (dplev :all :io)
                    (println "Message type order of first appearance:")
                    (pprint (seglob/get-message-occurrence-order))
                    (pprint "Counts of message types:")
                    (pprint (seglob/get-message-count)))
                  (end-of-mission-wrapup))

                (if (dplev :all)
                  (println "Handling message of type" message-type "sub_type=" subtype))

                ;; Bear trap
                #_(when (> (seglob/get-last-ms-time) 824500.0)
                    (println "The end is nigh")
                    (println "Handling message of type" message-type "sub_type=" subtype)
                    (pprint tbm))

                (record-message message-type subtype tbm)


                ;; Top level dispatch of messages based on message type.
                (let [pubs (case message-type
                             "agent"       (handle-agent-message       tbm subtype tb-version)
                             "chat"        (handle-chat-message        tbm subtype tb-version)
                             "event"       (handle-event-message       tbm subtype tb-version)
                             "export"      (handle-export-message      tbm subtype tb-version)
                             "groundtruth" (handle-groundtruth-message tbm subtype tb-version)
                             "observation" (handle-observation-message tbm subtype tb-version)
                             "status"      (handle-status-message      tbm subtype tb-version)
                             "trial"       (handle-trial-message       tbm subtype tb-version)
                             (do
                               (if (dplev :io :warn :all)
                                 (println "Unhandled message, message_type=" message-type "sub_type=" subtype))
                               nil))
                      fpubs (concat pubs (map predict/successful-prediction-message (seglob/get-successful-predictions)))
                      epubs (ritamsg/add-mission-ended-message fpubs)
                      gpubs (concat epubs (seglob/get-and-reset-msgs-for-rmq-and-mqtt))]
                  (if (not (empty? gpubs))
                    (when (dplev :publish :all) (println "About to publish" gpubs)))
                  (seglob/set-successful-predictions ())
                  (doall gpubs))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; The RITA State-Estimator "Brain" thread

(def ^:dynamic *rita-brain-thread* nil)
(def ^:dynamic *rita-stop* false)
(def ^:dynamic *rita-promise* nil)
(def ^:dynamic *one-message-at-a-time* (Object.))
(def ^:dynamic *rita-se-lock* (Object.))
(def ^:dynamic *rita-message-result-promise* (Object.))

(defonce testbed-msg-q (new LinkedBlockingQueue))
(defonce se-result-q (new LinkedBlockingQueue))
(def q-check-size 1)

(defn read-testbed-message-timeout []
  (.poll testbed-msg-q 1 TimeUnit/SECONDS))

(defn read-testbed-message-nb []
  (.poll testbed-msg-q))

; Infinite Q size. So put will always return immediately.
; Ensure Q size is always small. Otherwise it implies we are handling
; messages at slower rate than they are arriving.
(defn write-testbed-message-nb [message]
  ; pre condition
  (let [qsize (.size testbed-msg-q)]
    (when (not= 0 qsize)
      (when (dplev :warn :all) (println "WARN: testbed-message-q size is" qsize))))
  (.put testbed-msg-q message))

(defn read-se-result-timeout []
  (.poll se-result-q 1 TimeUnit/SECONDS))

(defn read-se-result-b []
  (.take se-result-q))

(defn read-se-result-nb []
  (.poll se-result-q))

(defn write-se-result-nb [tb-message results]
  (let [qsize (.size se-result-q)]
    (when (not= 0 qsize)
      (when (dplev :warn :all) (println "WARN: se-result-q size is" qsize))))

  ; even if the results are nil we write the value so that handler function can be released.
  (.put se-result-q {:tb-message tb-message :results results}))

(defn clear-testbed-message-q []
  (when (dplev :io :all) (println "Clearing testbed-message-q" (.size testbed-msg-q)))
  (while (not (nil? (read-testbed-message-nb)))))

(defn clear-se-result-q []
  (when (dplev :io :all) (println "Clearing se-result-q" (.size se-result-q)))
  (while (not (nil? (read-se-result-nb)))))

(defn print-q-sizes []
  (when (dplev :all) (println "Testbed message Q size" (.size testbed-msg-q)))
  (when (dplev :all) (println "SE result Q size" (.size se-result-q))))

(defn handle-testbed-message-with-queues [message]
  ; Write message non blockingly
  (write-testbed-message-nb message)
  ; wait for message blockingly
  (let [result-msg (read-se-result-b)]
    #_(when (dplev :all) (println "Returning results for"))
    #_(when (dplev :all) (pprint (:tb-message result-msg)))
    (:results result-msg)))

(def ^:dynamic *heartbeat-time* 0)      ; Time of last report
(def ^:dynamic *heartbeat-period* 5000) ; 5 seconds
(def ^:dynamic *DEBUGGING-RITA* true)
(def ^:dynamic *LAST-STILL-RUNNING-MESSAGE* 0)

(defn state-estimation-main-loop-with-queues []
  ;; Do one time "power on" initializations here.
  (seglob/reset-ac-default-startup-state)
  (while (not *rita-stop*)
    ;; Take message non-blockingly when we want to do other processing
    ;;(read-testbed-message-nb)

    ;; Take message blockingly but with timeout so that we can process *rita-stop*
    (let [tbm (read-testbed-message-timeout)
          time-before (System/currentTimeMillis)
          tbm-time (:timestamp (:header tbm))]
      (when (> (- time-before *LAST-STILL-RUNNING-MESSAGE*) 10000)
        (def ^:dynamic *LAST-STILL-RUNNING-MESSAGE* time-before)
        (println "!!!RITA still alive at:" tbm-time "total messages received:" (seglob/get-total-messages-received)))
      (when tbm
        ;; process message
        ;; write the result non-blockingly
        (write-se-result-nb
         tbm
         (if (= (System/getenv "DEBUGGING-RITA") "yes") ; $ export DEBUGGING-RITA=yes
           (rita-se-handle-message tbm)
           (try (rita-se-handle-message tbm)
              (catch Exception e
                (when (dplev :error :io :all)
                  (println (str "Exception:" (.getMessage e)) "while processing message: " tbm))
                (clojure.stacktrace/print-stack-trace e)))))
        (when (> (- (System/currentTimeMillis) *heartbeat-time*) *heartbeat-period*)
          (let [time-after (System/currentTimeMillis)
                time-processing (- time-after time-before)
                trialid (seglob/get-trial-id)
                expid (seglob/get-experiment-id)
                current-ms-time (seglob/rita-ms-time) ; Prakash time
                hbm (asist-msg/make-heartbeat-message current-ms-time (seglob/get-trial-id) (seglob/get-experiment-id))]
            (when (dplev :all) (println "Heartbeat: " hbm))
            (asist-msg/publish-heartbeat-message current-ms-time trialid expid (seglob/get-mqtt-connection))
            (def ^:dynamic *heartbeat-time* (System/currentTimeMillis))))
        #_(when-not tbm
            ;; nil value for tbm implies we timed out.
            (when (dplev :all) (println "Testbed message is nil")))
        ))))

(defn stop-state-estimater
  []
  (def ^:dynamic *rita-stop* false))

(defn handle-testbed-message
  [message]
  ;; Wait for processing of previous message to be processed
  (while (nil? *rita-promise*) (Thread/sleep 100)
                               (when (dplev :all) (println "Waiting for *rita-promise*")))

  #_(if (> *debug-verbosity* 0) (when (dplev :all) (println "*rita-promise* fulfilled. Moving forward")))

  (locking *one-message-at-a-time*
    (let [apromise (locking *rita-se-lock*
                     (let [rp *rita-promise*]
                       rp))] ; leave room for more complexity later
      ;; Deliver the message
      (deliver apromise message)
      (let [result-promise (locking *rita-se-lock*
                             (def ^:dynamic *rita-message-result-promise* (promise))
                             *rita-message-result-promise*)]
        ;; Wait for the result and return it.
        (deref result-promise)))))

(defn state-estimation-main-loop
  []
  ;; Do one time "power on" initializations here.
  (seglob/reset-ac-default-startup-state)

  (while (not *rita-stop*)
    (let [mypromise (locking *rita-se-lock*
                      (def ^:dynamic *rita-promise* (promise))
                      *rita-promise*)
          ;; Wait for a mesage
          message (deref mypromise)
          ;; Handle the message
          result (try (rita-se-handle-message message)
                      (catch Exception e
                        (let [errmsg (str "caught exception: " (.getMessage e)"with - " (seglob/debug-get))]
                          (println "ERROR: Exception caught:" errmsg)
                          (clojure.stacktrace/print-stack-trace e)
                          nil)))
          respromise (locking *rita-se-lock* *rita-message-result-promise*)]
      ;; Return the result

      (when true ;(seglob/msgs-for-rmq-and-mqtt?)
        (println "About to publish these messages, including to mqtt")
        (pprint (concat result, ; These are the local messages
                                  (seglob/get-and-reset-msgs-for-rmq-and-mqtt))))
      (deliver respromise (concat result, ; These are the local messages
                                  (seglob/get-and-reset-msgs-for-rmq-and-mqtt)) )))) ; These are for rmq and mqtt

(defn start-rita-brain-thread
  []
  (let [th (Thread. (fn []
                      (when (dplev :io :all) (println "start-rita-brain-thread version"
                               (rsc/get-build-string)
                               (pamela.tools.utils.util/getCurrentThreadName)))
                      (state-estimation-initialization "Sparky" :tb4 1 1 "sparky" [])
                      #_(state-estimation-initialization "Saturn" :tb4 3 1 "Saturn" [])
                      (state-estimation-main-loop-with-queues)
                      (when (dplev :io :all) (println "start-rita-brain-thread -- Done "
                               (pamela.tools.utils.util/getCurrentThreadName))))
                    "StateEstimation-Thread")]
    (def ^:dynamic *rita-brain-thread* th)
    (.start th)))

;;; Fin
