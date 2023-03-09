;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.ritamessages
  "RITA Messages."
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
            ;[rita.state-estimation.volumes :as vol :refer :all]
            ;[rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            ;;[rita.state-estimation.rlbotif :as rlbotif]
            ;[rita.state-estimation.statlearn :as slearn]
            ;[rita.state-estimation.multhyp :as mphyp]
            ;[rita.state-estimation.interventions :as intervene]
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Functions that package up publish messages

(defn add-rmq-and-mqtt-message
  [mtype msg]
  (seglob/add-message-for-rmq-and-mqtt {:mtopic "mqtt-message" :mtype mtype :message msg}))

(defn add-message
  [existingmsgs mtopic mtype msg]
  (conj existingmsgs {:mtopic mtopic :mtype mtype :message msg}))

(defn add-mission-ended-message
  [existing-messages]
  (let [missionmsg seglob/*mission-terminated*]
    (if (and missionmsg (not seglob/*mission-ended-message-sent*))
      (do (seglob/set-mission-ended-message-sent true)
          (add-message existing-messages "mission-ended" :mission-ended true))
      existing-messages)))

(defn add-bs-change-message
  [existingmsgs newmsg]
  (add-message existingmsgs "belief-state-changes" :belief-state-changes newmsg))

(defn publish-intervention
  [dest prompt em expl]                      ; dest = a PID (string) or a vector of PIDs (for multiple).
  (let [cmsg (asist-msg/make-intervention-chat-message
                (if (vector? dest) dest [dest]) ; address to vector of pids
                prompt                          ; chat-text
                nil                             ; duration
                expl                            ; explanation
                em                              ; elapsed-milliseconds
                (seglob/rita-ms-time)           ; hdr-time-ms
                (seglob/get-trial-id)           ; trial-id
                (seglob/get-experiment-id))]    ; exp-id
    (when (dplev :triage :intervention :demo :all)
      (println "***Publishing intervention via intervention chat message with prompt \"" prompt "\" to MQTT:"
               (seglob/get-mqtt-connection) "with destination " dest)
      #_(pprint cmsg))
    (asist-msg/publish-intervention-chat-msg-mqtt cmsg  (seglob/get-mqtt-connection))
    (add-message nil "chat-intervention" :chat-intervention cmsg)))
