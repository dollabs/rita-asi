;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.asrandchat
  "Observations about asr and chat messages."
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
            [rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            ;;[rita.state-estimation.rlbotif :as rlbotif]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.teamstrength :as ts]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.rita-se-core :as rsc :refer :all] ; back off from refer all +++
            [rita.state-estimation.cognitiveload :as cogload]
            [rita.state-estimation.interventions :as intervene]
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

#_(in-ns 'rita.state-estimation.asrandchat)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; ASR

;; {"app-id":"TestbedBusInterface",
;;  "mission-id":"af3900dc-02f0-4240-a544-61de18bcb394",
;;  "routing-key":"testbed-message",
;;  "testbed-message":{"data":{"asr_system":"google",
;;                             "alternatives":[{"confidence":0.0,
;;                                              "text":"all right"}],
;;                             "participant_id":"E000322",
;;                             "text":"all right",
;;                             "is_final":false,
;;                             "id":"37667773-8326-4b1c-9484-49ce53d8dd34"},
;;                     "header":{"version":"0.1",
;;                               "message_type":"observation",
;;                               "timestamp":"2021-06-16T00:12:41.547416000Z"},
;;                     "msg":{"source":"tomcat_speech_analyzer",
;;                            "version":"0.1",
;;                            "trial_id":"af3900dc-02f0-4240-a544-61de18bcb394",
;;                            "experiment_id":"762477d6-ebb6-4dfc-884d-7ede1e908707",
;;                            "sub_type":"asr",
;;                            "timestamp":"2021-06-16T00:12:41.547442000Z"}},
;;  "timestamp":1627484924725,
;;  "received-routing-key":"testbed-message",
;;  "exchange":"rita"}

(defn rita-handle-asr-message
  [tbm tb-version]
  (let [data (:data tbm)
        {pid  :participant_id
         text :text
         is_final :is_final } data
        header (:header tbm)
        msg (:msg tbm)
        {thetext :text
         pid :participant_id} data]
    (when (dplev :speech :all)
      (println "ASR data found, " pid "said:" thetext)
      (pprint tbm))
    ;;(ts/increment-strength-data-for pid (seglob/get-trial-number) em :spoken-utterances) WHERE IS EM?
    nil))

;; ASR transcription data found,  RED_ASIST_2 said: sweetheart logging back in
;; {:data
;;  {:asr_system "google",
;;   :end_timestamp "2022-03-30T01:49:11.276249000Z",
;;   :start_timestamp "2022-03-30T01:49:10.276249000Z",

;;   :is_final true,

;;   :alternatives
;;   [{:confidence 0.6536611318588257, :text "sweetheart logging back in"}
;;    {:confidence 0.6979223489761353, :text "sweetheart locking back in"}],
;;   :participant_id "RED_ASIST_2",

;;   :text "sweetheart logging back in",

;;   :id "6be597ef-bc94-48bb-a76d-8e981be78658"},
;;  :header
;;  {:timestamp "2022-03-30T01:49:09.631042000Z",
;;   :version "0.1",
;;   :message_type "observation"},
;;  :msg
;;  {:sub_type "asr:transcription",
;;   :replay_id "a16920a1-b98a-4285-b749-b32d42656342",
;;   :trial_id "edcfbabe-3da6-4896-bdd3-3f419132d352",
;;   :source "tomcat_speech_analyzer",
;;   :replay_parent_id "aec81ecb-7fdb-4b4b-9bf7-6ebab112021a",
;;   :experiment_id "1551846d-efe9-46cb-bffb-0c0f97c64689",
;;   :replay_parent_type "REPLAY",
;;   :version "3.5.1",
;;   :timestamp "2022-03-30T01:49:09.631094000Z"}}

(defn rita-handle-asr-transcription-message
  [tbm tb-version]
  (let [data (:data tbm)

        header (:header tbm)
        msg (:msg tbm)
        {thetext :text
         call_name :participant_id} data
        pnm (seglob/get-player-name-map)
        pid (get pnm call_name)
        em (seglob/get-last-ms-time)]
    (when (:is_final data)
      (let [words (into [] (map clojure.string/trim (clojure.string/split thetext #" ")))
            numwords (count thetext)]
        (when (dplev :speech :all)
          (println "ASR transcription data found, " pid "said:" words))
        (ts/increment-strength-data-for pid (seglob/get-trial-number) em :spoken-utterances)
        (ts/increment-strength-data-for pid (seglob/get-trial-number) em :spoken-words numwords)))
    nil))

(defn rita-handle-asr-alignment-message
  [tbm tb-version]
  (let [data (:data tbm)
        header (:header tbm)
        msg (:msg tbm)]
    (when (dplev :all) (println "ASR alignment data found"))
    nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Chat

;;;      "Event:Chat"
;; Unhandled testbed message sub_type: Event:Chat tbm= {:data {:mission_timer 10 : 3,
;;                                                             :sender @,
;;                                                             :addressees [@a],
;;                                                             :text {"text":"",
;;                                                                    "extra":[{"text":"3...",
;;                                                                              "color":"white"}]}},
;;                                                      :header {:timestamp 2020-08-19T23:14:36.947Z,
;;                                                               :message_type chat,
;;                                                               :version 1.0},
;;                                                      :msg {:experiment_id e0b770fa-d726-4c7e-8406-728dcfaf8b98,
;;                                                            :trial_id b66aa7e5-a16c-4abc-a2cb-5ae3bdc675e5,
;;                                                            :timestamp 2020-08-19T23:14:36.947Z,
;;                                                            :source simulator,
;;                                                            :sub_type Event:Chat,
;;                                                            :version 0.5}}

(defn rita-handle-chat-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {mission_timer :mission_timer
         sender :sender
         adressees :adressees
         textstruct :text} (:data tbm)
        text (try (get (json/read-str textstruct) "text") (catch Exception e (str "except:" (.getMessage e))))
        extra-text-vec (try (get (json/read-str textstruct) "extra") (catch Exception e (str "except:" (.getMessage e))))
        alltext (str text (if extra-text-vec (apply str (map (fn[textst] (:text textst)) extra-text-vec)) ""))]
    ;; json in the message was unexpected. !!!
    ;;(when (dplev :all) (println "chat structure=" textstruct "text=" text "extra-text-vec=" extra-text-vec))
    (when (dplev :chat :all) (println "Chat from (" sender ")" alltext))
    nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Speech

(defn rita-handle-UserSpeech-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {playername :playername
         text :text} (:data tbm)
        pid (seglob/get-participant-id-from-data (:data tbm))]
    (when (dplev :speech :all) (println "Player" pid "said" text))))


(defn rita-handle-dialogue_event-message
  [tbm tb-version]
  (when (dplev :speech :all)
    (println "Event:dialogue_event message received:")
    (pprint tbm))
  nil)


;;; Fin
