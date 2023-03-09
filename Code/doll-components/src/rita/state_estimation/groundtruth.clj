;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.groundtruth
  "Ground Truth from the testbed."
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

#_(in-ns 'rita.state-estimation.groundtruth)

;;; testbed Mission:ThreatSignList
;;; message_type= observation sub_type= Mission:ThreatSignList
;;; tbm= {:data {:mission Saturn_A_Rubble,
;;;              :elapsed_milliseconds 39.0,
;;;              :mission_threatsign_list [{:block_type block_rubble_collapse,
;;;                                         :feature_type collapse threat,
;;;                                         :y 59.0,
;;;                                         :z 1.0,
;;;                                         :room_name NA,
;;;                                         :x -2202.0} ...],
;;;              :mission_timer 17 : 3},
;;;              :header {:timestamp 2022-03-30T01:49:39.357Z,
;;;                       :version 1.1,
;;;                       :message_type groundtruth},
;;;              :msg {:sub_type Mission:ThreatSignList,
;;;                    :replay_id a16920a1-b98a-4285-b749-b32d42656342,
;;;                    :trial_id edcfbabe-3da6-4896-bdd3-3f419132d352,
;;;                    :source simulator,
;;;                    :replay_parent_id aec81ecb-7fdb-4b4b-9bf7-6ebab112021a,
;;;                    :experiment_id 1551846d-efe9-46cb-bffb-0c0f97c64689,
;;;                    :replay_parent_type REPLAY,
;;;                    :version 0.1, :timestamp 2022-03-30T01:49:39.358Z}}

(defn rita-handle-mission_threatsignlist-message
  [tbm tb-version]
  (do ;+++ do something here
    　nil))

;;; +++ Migrate other groundtruth messages here
;;; Fin
