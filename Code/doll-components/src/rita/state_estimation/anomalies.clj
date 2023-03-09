;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.anomalies
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
            ;[rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            ;;[rita.state-estimation.rlbotif :as rlbotif]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ras :as ras]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.rita-se-core :as rsc :refer :all] ; back off from refer all +++
            [rita.state-estimation.cognitiveload :as cogload]
            [rita.state-estimation.interventions :as intervene]
            [rita.state-estimation.statlearn :as slearn]

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

#_(in-ns 'rita.state-estimation.anomalies)


(defn running-training-model
  [exptid trialid exptid elapsed-milliseconds]
  (let [em (if (not (number? elapsed-milliseconds)) 123456 elapsed-milliseconds)
        the-data {:experiment exptid,
                  :trial trialid,
                  :anomaly-id :training-mission
                  :anomaly (str "RITA sent on a training-mission (" exptid
                                ")? NO, it's not going to happen.  I can't and I shan't. "
                                "RITA is on strike. RITA is going to sit this one out")}
        msg-with-data (asist-msg/make-rmq-to-mqtt-msg em "rita_anomaly" trialid exptid the-data)]
    (rsc/publish-rmq-mqtt :mqtt-message msg-with-data)))

;; Missing PID

(defn missing-participant-id-in
  [pid call-sign role where elapsed-milliseconds]
  (let [exptid (seglob/get-experiment-mission)
        trialid (seglob/get-trial-id)
        em (if (not (number? elapsed-milliseconds)) 123456 elapsed-milliseconds)
        the-data {:experiment exptid,
                  :trial trialid,
                  :anomaly-id :missing-pid
                  :anomaly (str "Missing Participant ID in " where),
                  :callsign call-sign,
                  :role role}
        msg-with-data (asist-msg/make-rmq-to-mqtt-msg em "rita_anomaly" trialid exptid the-data)
        ]
    (rsc/publish-rmq-mqtt :mqtt-message msg-with-data)))


(defn removal-of-nonexistant-marker
  [pid x y z mtype elapsed-milliseconds]
  (let [exptid (seglob/get-experiment-mission)
        trialid (seglob/get-trial-id)
        em (if (not (number? elapsed-milliseconds)) 123456 elapsed-milliseconds)
        the-data {:experiment exptid,
                  :trial trialid,
                  :anomaly (str "Participant ID has removed a nonexistant marker of type " mtype " at [" x y z"]"),
                  :anomaly-id :marker-remove-nonexistant
                  :pid pid,
                  :marker-type mtype
                  :location [x y z]}
        msg-with-data (asist-msg/make-rmq-to-mqtt-msg em "rita_anomaly" trialid exptid the-data)
        ]
    (rsc/publish-rmq-mqtt :mqtt-message msg-with-data)))

(defn removal-of-already-removed-marker
  [pid x y z mtype elapsed-milliseconds]
  (let [exptid (seglob/get-experiment-mission)
        trialid (seglob/get-trial-id)
        em (if (not (number? elapsed-milliseconds)) 123456 elapsed-milliseconds)
        the-data {:experiment exptid,
                  :trial trialid,
                  :anomaly (str "Participant ID has removed a marker that has already been removed of type " mtype " at [" x y z"]"),
                  :anomaly-id :marker-remove-already-removed
                  :pid pid,
                  :marker-type mtype
                  :location [x y z]}
        msg-with-data (asist-msg/make-rmq-to-mqtt-msg em "rita_anomaly" trialid exptid the-data)]
    (rsc/publish-rmq-mqtt :mqtt-message msg-with-data)))

(defn header-time-ms
  []
  (ras/timeinmilliseconds (seglob/get-header-time)))

;;; +++ not an anomaly, move elsewhere
(defn publish-results-at-end-of-mission
  [headertime]
  (let [exptid (seglob/get-experiment-mission)
        trialid (seglob/get-trial-id)
        em (if (not (number? headertime)) 123456 headertime)
        the-data {:experiment exptid,
                  :trial trialid,
                  :measures (str "Final results"),
                  :predictions-published (apply + (vals (seglob/get-se-predictions)))
                  :prediction-successful (apply + (vals (seglob/get-se-successful-predictions)))
                  :interventions-given   (seglob/get-interventions-given)
                  :measure-id :final-results}
        msg-with-data (asist-msg/make-rmq-to-mqtt-msg (header-time-ms) "rita_anomaly" trialid exptid the-data)]
    (when (dplev :all :io)
      (println "Publishing end of mission measures as follows:" msg-with-data))
    (rsc/publish-rmq-mqtt :mqtt-message msg-with-data)))

;;; Fin
