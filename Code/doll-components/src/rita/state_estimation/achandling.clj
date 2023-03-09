;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.achandling
  "Observations from the testbed."
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
  (:gen-class));; required for uberjar

#_(in-ns 'rita.state-estimation.achandling)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Gallup

;;; testbed message_type= event sub_type= agent:ac_gallup_ta2_gelp
;;; tbm= {:data
;;;        {:gelp_msg_id 50a9c104-5558-46c9-bec4-99a09882b947,
;;;         :gelp_pub_minute 10.0,
;;;         :created_ts 2022-03-02T18:42:30.972861Z,
;;;         :gelp_results [{:gelp_components_upper_bound [5.540358749631349
;;;                                                       6.135846556138347
;;;                                                       6.210332070737904
;;;                                                       6.052647868467458
;;;                                                       6.258981550909246
;;;                                                       6.0335938077905835
;;;                                                       6.384239353882064
;;;                                                       6.661264362392096],
;;;                         :gelp_overall_lower_bound 3.7211623202551007,
;;;                         :gelp_components_lower_bound [1.1016174503382716
;;;                                                       1.429895146603355
;;;                                                       2.6355378692636138
;;;                                                       1.0468487993384206
;;;                                                       1.7278283526375025
;;;                                                       1.653148426583229
;;;                                                       2.378406493691415
;;;                                                       2.6326225328763795],
;;;                         :gelp_overall_upper_bound 9.233855337525718,
;;;                         :callsign Red,
;;;                         :gelp_overall 6.477508828890409,
;;;                         :gelp_components [3.3209880999848105
;;;                                           3.782870851370851
;;;                                           4.4229349700007585
;;;                                           3.5497483339029396
;;;                                           3.993404951773374
;;;                                           3.8433711171869063
;;;                                           4.3813229237867395
;;;                                           4.6469434476342375],
;;;                         :participant_id x202203021}
;;;                        {:gelp_components_upper_bound [5.540358749631349
;;;                                                       6.135846556138347
;;;                                                       6.210332070737904
;;;                                                       6.052647868467458
;;;                                                       6.258981550909246
;;;                                                       6.0335938077905835
;;;                                                       6.384239353882064
;;;                                                       6.661264362392096],
;;;                         :gelp_overall_lower_bound 3.7211623202551007,
;;;                         :gelp_components_lower_bound [1.1016174503382716
;;;                                                       1.429895146603355
;;;                                                       2.6355378692636138
;;;                                                       1.0468487993384206
;;;                                                       1.7278283526375025
;;;                                                       1.653148426583229
;;;                                                       2.378406493691415
;;;                                                       2.6326225328763795],
;;;                         :gelp_overall_upper_bound 9.233855337525718,
;;;                         :callsign Green,
;;;                         :gelp_overall 6.477508828890409,
;;;                         :gelp_components [3.3209880999848105
;;;                                           3.782870851370851
;;;                                           4.4229349700007585
;;;                                           3.5497483339029396
;;;                                           3.993404951773374
;;;                                           3.8433711171869063
;;;                                           4.3813229237867395
;;;                                           4.6469434476342375],
;;;                         :participant_id x202203023}
;;;                        {:gelp_components_upper_bound [5.540358749631349
;;;                                                       6.135846556138347
;;;                                                       6.210332070737904
;;;                                                       6.052647868467458
;;;                                                       6.258981550909246
;;;                                                       6.0335938077905835
;;;                                                       6.384239353882064
;;;                                                       6.661264362392096],
;;;                         :gelp_overall_lower_bound 3.7211623202551007,
;;;                         :gelp_components_lower_bound [1.1016174503382716
;;;                                                       1.429895146603355
;;;                                                       2.6355378692636138
;;;                                                       1.0468487993384206
;;;                                                       1.7278283526375025
;;;                                                       1.653148426583229
;;;                                                       2.378406493691415
;;;                                                       2.6326225328763795],
;;;                         :gelp_overall_upper_bound 9.233855337525718,
;;;                         :callsign Blue,
;;;                         :gelp_overall 6.477508828890409,
;;;                         :gelp_components [3.3209880999848105
;;;                                           3.782870851370851
;;;                                           4.4229349700007585
;;;                                           3.5497483339029396
;;;                                           3.993404951773374
;;;                                           3.8433711171869063
;;;                                           4.3813229237867395
;;;                                           4.6469434476342375],
;;;                         :participant_id x202203022}]},
;;;       :header {
;;;         :version 1.1,
;;;         :message_type event,
;;;         :timestamp 2022-03-02T18:42:30.972936Z},
;;;       :msg {
;;;         :trial_id 55cd3a31-548a-4d5b-9376-6f78a93545d6,
;;;         :timestamp 2022-03-02T18:42:30.972936Z,
;;;         :version 0.5.1,
;;;         :experiment_id f97a5942-790d-4b85-b5cb-fc648bba3cc1,
;;;         :sub_type agent:ac_gallup_ta2_gelp,
;;;         :source ac_gallup_ta2_gelp}}

(defn rita-handle-ac_gallup_ta2_gelp-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_gallup_ta2_gelp")
    (let [results (:gelp_results (:data tbm))
          em (seglob/get-last-ms-time)]
      (doseq [aresult results]
        (let [{olb      :gelp_overall_lower_bound
               oub      :gelp_overall_upper_bound
               overall  :gelp_overall
               callsign :callsign
               pid      :participant_id} aresult]
          (println "Handling GELP message")
          (pprint aresult)
          (ie/vote-leader "ac_gallup_ta2_gelp" pid overall (max (- overall olb) (- oub overall))  em))))))

;;; testbed message_type= event sub_type= standard
;;; tbm= {:data {:gold_results [],
;;;              :gold_pub_minute 0.0,
;;;              :gold_msg_id 849e3069-8907-4926-b04c-aac67d92d67b,
;;;              :created_ts 2022-03-31T01:16:45.399452Z,
;;;              :gold_feature_inventory [agg_action_terms agg_approval_terms agg_avg_num_words agg_coordination_terms
;;;                                       agg_dominant_speaker agg_entities_all agg_first_word agg_last_word
;;;                                       agg_long_utterance agg_minute agg_num_utterances agg_pc_QID830_1 agg_pc_QID830_10
;;;                                       agg_pc_QID830_11 agg_pc_QID830_12 agg_pc_QID830_13 agg_pc_QID830_14 agg_pc_QID830_15
;;;                                       agg_pc_QID830_2 agg_pc_QID830_3 agg_pc_QID830_4 agg_pc_QID830_5 agg_pc_QID830_6
;;;                                       agg_pc_QID830_7 agg_pc_QID830_8 agg_pc_QID830_9 agg_planning_all agg_rme_vars_QID751
;;;                                       agg_rme_vars_QID753 agg_rme_vars_QID755 agg_rme_vars_QID757 agg_rme_vars_QID759
;;;                                       agg_rme_vars_QID761 agg_rme_vars_QID763 agg_rme_vars_QID765 agg_rme_vars_QID767
;;;                                       agg_rme_vars_QID769 agg_rme_vars_QID771 agg_rme_vars_QID773 agg_rme_vars_QID775
;;;                                       agg_rme_vars_QID777 agg_rme_vars_QID779 agg_rme_vars_QID781 agg_rme_vars_QID783
;;;                                       agg_rme_vars_QID785 agg_rme_vars_QID787 agg_rme_vars_QID789 agg_rme_vars_QID791
;;;                                       agg_rme_vars_QID793 agg_rme_vars_QID795 agg_rme_vars_QID797 agg_rme_vars_QID799
;;;                                       agg_rme_vars_QID801 agg_rme_vars_QID803 agg_rme_vars_QID805 agg_rme_vars_QID807
;;;                                       agg_rme_vars_QID809 agg_rme_vars_QID811 agg_rme_vars_QID813 agg_rme_vars_QID815
;;;                                       agg_rme_vars_QID817 agg_rme_vars_QID819 agg_rme_vars_QID821 agg_role_terms
;;;                                       agg_sa_QID13_1 agg_sa_QID13_10 agg_sa_QID13_11 agg_sa_QID13_12 agg_sa_QID13_13
;;;                                       agg_sa_QID13_14 agg_sa_QID13_15 agg_sa_QID13_2 agg_sa_QID13_3 agg_sa_QID13_4
;;;                                       agg_sa_QID13_5 agg_sa_QID13_6 agg_sa_QID13_7 agg_sa_QID13_8 agg_sa_QID13_9
;;;                                       agg_sd_QID832_16 agg_sd_QID832_17 agg_sd_QID832_18 agg_sd_QID832_19
;;;                                       agg_sd_QID832_20 agg_sd_QID832_21 agg_sd_QID832_22 agg_sd_QID832_23
;;;                                       agg_sd_QID832_24 agg_sd_QID832_25 agg_sd_QID832_26 agg_sd_QID832_27 agg_sd_QID832_28
;;;                                       agg_sd_QID832_29 agg_sd_QID832_9 agg_time_spent_speaking agg_vge_QID867_2
;;;                                       agg_vge_QID867_3 agg_vge_QID867_4 agg_vge_QID868_1 agg_vge_QID868_2 agg_vge_QID868_3
;;;                                       agg_vge_QID868_4 agg_vge_QID868_5 agg_vge_QID868_6 agg_vge_QID869 agg_vge_QID870
;;;                                       agg_vge_QID871 agg_vge_QID872_1 agg_vge_QID872_2 agg_vge_QID872_3 agg_vge_QID872_4
;;;                                       agg_vge_QID872_5 agg_vge_QID872_6 agg_vge_QID872_7 agg_vge_QID872_8 agg_vge_QID873
;;;                                       agg_vge_QID874_1 agg_vge_QID875 mod_lgb_Motivation_count mod_lgb_Motivation_per_min
;;;                                       mod_lgb_Motivation_per_min_adjusted mod_lgb_Compensatory_Helping_count
;;;                                       mod_lgb_Compensatory_Helping_per_min mod_lgb_Compensatory_Helping_per_min_adjusted
;;;                                       mod_lgb_Contingent_Planning_count mod_lgb_Contingent_Planning_per_min
;;;                                       mod_lgb_Contingent_Planning_per_min_adjusted mod_lgb_Deliberate_Planning_count
;;;                                       mod_lgb_Deliberate_Planning_per_min mod_lgb_Deliberate_Planning_per_min_adjusted
;;;                                       mod_lgb_pq_clarifying_roles_count mod_lgb_pq_clarifying_roles_per_min
;;;                                       mod_lgb_pq_clarifying_roles_per_min_adjusted mod_lgb_Reactive_Planning_count
;;;                                       mod_lgb_Reactive_Planning_per_min mod_lgb_Reactive_Planning_per_min_adjusted
;;;                                       mod_lgb_Transactive_Memory_count mod_lgb_Transactive_Memory_per_min
;;;                                       mod_lgb_Transactive_Memory_per_min_adjusted]},
;;;       :header {:timestamp 2022-03-31T01:16:45.399538Z,
;;;                :version 1.1,
;;;                :message_type event},
;;;       :msg {:trial_id 5a9fb978-c31a-4b44-9ead-94a02a4ec52d,
;;;             :version 0.1.1,
;;;             :sub_type standard,
;;;             :source ac_gallup_ta2_gold,
;;;             :timestamp 2022-03-31T01:16:45.399538Z,
;;;             :experiment_id 1925b5cf-8fa6-4637-9bbe-fb5003b74341,
;;;             :replay_id 1993221a-3bcd-4bf8-aa50-556a2040e291,
;;;             :replay_parent_type TRIAL}}


;;; +++ Do we want a separate button for this? +++
(defn rita-handle-ac_gallup_ta2_standard-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_gallup_ta2_gelp")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_gallup_ta2_gold (standard)")))

;;; Unhandled testbed message_type= event sub_type= bullion
;;; tbm= {:data {:gold_pub_minute 22854.0,
;;;              :gold_results [{:participant_id E000622, :agg_trial 20220414_224227, :agg_minute 22854.0}
;;;                             {:participant_id E000606, :agg_trial 20220414_224227, :agg_minute 22854.0}
;;;                             {:participant_id E000608, :agg_trial 20220414_224227, :agg_minute 22854.0}
;;;                             {:participant_id E000622, :agg_trial 20220414_224227, :agg_minute 22854.0}
;;;                             {:participant_id E000606, :agg_trial 20220414_224227, :agg_minute 22854.0}
;;;                             {:participant_id E000608, :agg_trial 20220414_224227, :agg_minute 22854.0}
;;;                             ],
;;;               :gold_feature_inventory [],
;;;               :created_ts 2022-04-14T22:42:35.025284Z,
;;;               :gold_msg_id 43207052-cda0-41eb-9d35-8a185827c464},
;;;               :header {:timestamp 2022-03-30T01:48:15.176332Z,
;;;                        :version 1.1,
;;;                        :message_type event},
;;;               :msg {:sub_type bullion,
;;;                     :replay_id a16920a1-b98a-4285-b749-b32d42656342,
;;;                     :trial_id edcfbabe-3da6-4896-bdd3-3f419132d352,
;;;                     :source ac_gallup_ta2_gold,
;;;                     :replay_parent_id aec81ecb-7fdb-4b4b-9bf7-6ebab112021a,
;;;                     :experiment_id 1551846d-efe9-46cb-bffb-0c0f97c64689,
;;;                     :replay_parent_type TRIAL,
;;;                     :version 0.1.1, :timestamp
;;;                     2022-03-30T01:48:15.176332Z}}

;;; +++ Do we want a separate button for this? +++
(defn rita-handle-ac_gallup_ta2_bullion-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_gallup_ta2_gelp")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_gallup_ta2_gold (bullion)")))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; CMUFMS_TA2

;;; testbed message_type= agent sub_type= Measure:cognitive_load
;;; tbm= {:data {:created 2022-03-31T01:24:04.781024Z,
;;;              :cognitive_load {:confidence 0.6033700230125376,
;;;                               :value 0.42885970890089203},
;;;              :agent AC_CMUFMS_TA2_Cognitive,
;;;              :probability_of_forgetting {:confidence 0.7054069703850167,
;;;                                          :value 0.30014122886198513},
;;;              :elapsed_milliseconds 181359.0,
;;;              :id 5559c139-4f87-4514-ae41-1df977601dc6},
;;;       :header {:timestamp 2022-03-31T01:24:04.776Z,
;;;                :version 1.1,
;;;                :message_type agent},
;;;       :msg {:trial_id 5a9fb978-c31a-4b44-9ead-94a02a4ec52d,
;;;             :version 1.0,
;;;             :sub_type Measure:cognitive_load,
;;;             :source AC_CMUFMS_TA2_Cognitive,
;;;             :timestamp 2022-03-31T01:24:04.776Z,
;;;             :experiment_id 1925b5cf-8fa6-4637-9bbe-fb5003b74341,
;;;             :replay_id 1993221a-3bcd-4bf8-aa50-556a2040e291,
;;;             :replay_parent_type TRIAL}}

(defn rita-handle-cognitive-load-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_cmufms_ta2_cognitive")
    (let [{cogload  :cognitive_load,
           ac-name  :agent,
           p-forget :probability_of_forgetting,
           em       :elapsed_milliseconds} (:data tbm) ; eg.  181359.0
          {cogload-confidence :confidence,            ; eg. 0.6033700230125376
           cogload-value      :value} cogload         ; eg. 0.42885970890089203
          {forget-confidence  :confidence,            ; eg. 0.7054069703850167
           forget-value       :value} p-forget]       ; eg.  0.30014122886198513
      (cogload/vote-cogload ac-name "unknown" {:cogload-confidence cogload-confidence,
                                               :cogload-value cogload-value,
                                               :forget-confidence forget-confidence,
                                               :forget-value forget-value})
      (seglob/warn-ignoring-message-one-time "ac_cmufms_ta2_cognitive")
      nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; IHMC

;; testbed message_type= agent sub_type= FoV:Profile
;; tbm= {:data {:vendor UNKNOWN,
;;              :render_time_proc {:std 0.0,
;;                                 :mean 0.0,
;;                                 :min 0.0,
;;                                 :max 0.0},
;;              :renderer UNKNOWN,
;;              :publish_time_perf {:std 0.0,
;;                                  :mean 0.0,
;;                                  :min 0.0,
;;                                  :max 0.0},
;;              :publish_time_proc {:std 0.0,
;;                                  :mean 0.0,
;;                                  :min 0.0,
;;                                  :max 0.0},
;;              :render_time_perf {:std 0.0,
;;                                 :mean 0.0,
;;                                 :min 0.0,
;;                                 :max 0.0},
;;              :version UNKNOWN,
;;              :sl_version UNKNOWN,
;;              :backend UNKNOWN},
;;       :header {:version 0.5,
;;                :message_type agent,
;;                :timestamp 2022-01-19T22:35:50.229880Z},
;;       :msg {:sub_type FoV:Profile,
;;             :trial_id add1aeef-a1c0-4621-b7d6-51efe129c99c,
;;             :source pygl_fov,
;;             :version 0.5,
;;             :experiment_id 934c548a-54ef-4e1e-bdbb-613bd395764b,
;;             :timestamp 2022-01-19T22:35:50.229923Z}}

(defn rita-handle-ac_fov_profile-message
  [tbm tb-version]
  (if (seglob/observing-ac "pygl_fov")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "pygl_fov")))

;; testbed  message_type= event sub_type= Event:Addressing
;;   tbm= {:data
;;          {:jag
;;            {:addressing {:x2 1.0},
;;             :elapsed_milliseconds 377608.0,
;;             :is_complete true,
;;             :id 38dd4b9f-116f-453d-b40f-bc05f50026b3},
;;           :participant_id x2},
;;         :header {:version 1.1,
;;                  :message_type event,
;;                  :timestamp 2022-03-16T15:57:07.933590Z},
;;         :msg {:source ac_ihmc_ta2_joint-activity-interdependence,
;;               :sub_type Event:Addressing,
;;               :timestamp 2022-03-16T15:57:07.933590Z,
;;               :experiment_id 3c652ea4-f9ea-4f8f-b899-350a89bdb695,
;;               :version 1.1.0,
;;               :trial_id d1774451-2dd5-47f8-ade2-c873d659a34b}}

(defn rita-handle-Addressing-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_ihmc_ta2")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_ihmc_ta2")))

;; testbed message_type= event sub_type= Event:Summary
;; tbm= {:data {:jag {:instances {:E000606 478b2132-5f85-45ac-829c-8634b40f5312,
;;                                :E000622 de8945ba-3ede-408b-b904-772267e8da8d,
;;                                :E000608 df8058fa-2fa5-43f0-ba65-f602ecb0250a},
;;                    :urn urn:ihmc:asist:rescue-victim,
;;                    :inputs {:victim-type critical, :victim-id 23.0}},
;;              :active_duration 65915.0,
;;              :redundancy_ratio 0.0,
;;              :joint_activity_efficiency 3.114405540316033E-4},
;;       :header {:timestamp 2022-03-30T02:06:02.023999Z,
;;                :version 1.1,
;;                :message_type event},
;;       :msg {:sub_type Event:Summary,
;;             :replay_id a16920a1-b98a-4285-b749-b32d42656342,
;;             :trial_id edcfbabe-3da6-4896-bdd3-3f419132d352,
;;             :source ac_ihmc_ta2_joint-activity-interdependence,
;;             :replay_parent_id aec81ecb-7fdb-4b4b-9bf7-6ebab112021a,
;;             :experiment_id 1551846d-efe9-46cb-bffb-0c0f97c64689,
;;             :replay_parent_type TRIAL,
;;             :version 1.2.8,
;;             :timestamp 2022-03-30T02:06:02.023999Z}}

(defn rita-handle-Event_Summary-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_ihmc_ta2")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_ihmc_ta2")))

;; message_type= event sub_type= Event:Discovered
;; tbm= {:data
;;        {:jag
;;          {:urn urn:ihmc:asist:search-area,
;;           :children [{:urn urn:ihmc:asist:get-in-range,
;;                       :children [],
;;                       :inputs {:area llcn},
;;                       :outputs {},
;;                       :id 64308827-b417-4897-940b-3e6ab5d584f3}],
;;           :inputs {:area llcn},
;;           :outputs {},
;;           :id 92219a29-04ac-4738-a641-e3158eff0c19},
;;           :participant_id x1},
;;       :header {:version 1.1,
;;                :message_type event,
;;                :timestamp 2022-03-16T15:57:17.703439Z},
;;       :msg {:source ac_ihmc_ta2_joint-activity-interdependence,
;;             :sub_type Event:Discovered,
;;             :timestamp 2022-03-16T15:57:17.703439Z,
;;             :experiment_id 3c652ea4-f9ea-4f8f-b899-350a89bdb695,
;;             :version 1.1.0,
;;             :trial_id d1774451-2dd5-47f8-ade2-c873d659a34b}}

(defn rita-handle-Discovered-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_ihmc_ta2")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_ihmc_ta2")))

;; message_type= event sub_type= Event:Awareness
;; tbm= {:data
;;        {:jag {:urn urn:ihmc:asist:search-area,
;;               :awareness {},
;;               :elapsed_milliseconds 387369.0,
;;               :is_complete false,
;;               :id 92219a29-04ac-4738-a641-e3158eff0c19},
;;         :participant_id x1},
;;       :header {:version 1.1,
;;                :message_type event,
;;                :timestamp 2022-03-16T15:57:17.703671Z},
;;       :msg {:source ac_ihmc_ta2_joint-activity-interdependence,
;;             :sub_type Event:Awareness,
;;             :timestamp 2022-03-16T15:57:17.703671Z,
;;             :experiment_id 3c652ea4-f9ea-4f8f-b899-350a89bdb695,
;;             :version 1.1.0,
;;             :trial_id d1774451-2dd5-47f8-ade2-c873d659a34b}}

(defn rita-handle-Awareness-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_ihmc_ta2")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_ihmc_ta2")))

;; message_type= event sub_type= Event:Completion
;; tbm= {:data
;;        {:jag
;;          {:addressing {:x1 0.0},
;;           :elapsed_milliseconds 387369.0,
;;           :is_complete true,
;;           :id fa20be72-1ae2-4d18-8c00-b3dcf63d17d0},
;;         :participant_id x1},
;;       :header {:version 1.1,
;;                :message_type event,
;;                :timestamp 2022-03-16T15:57:17.704435Z},
;;       :msg {:source ac_ihmc_ta2_joint-activity-interdependence,
;;             :sub_type Event:Completion,
;;             :timestamp 2022-03-16T15:57:17.704435Z,
;;             :experiment_id 3c652ea4-f9ea-4f8f-b899-350a89bdb695,
;;             :version 1.1.0,
;;             :trial_id d1774451-2dd5-47f8-ade2-c873d659a34b}}

(defn rita-handle-Completion-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_ihmc_ta2")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_ihmc_ta2")))

;; message_type= event sub_type= Event:Preparing
;; tbm= {:data
;;        {:jag
;;          {:preparing {},
;;           :elapsed_milliseconds 732225.0,
;;           :is_complete false,
;;           :id aa1a53d0-f0d7-40db-abfc-d1a385ff39a4},
;;         :participant_id x1},
;;       :header {:version 1.1,
;;                :message_type event,
;;                :timestamp 2022-03-16T16:03:02.550950Z},
;;       :msg {:source ac_ihmc_ta2_joint-activity-interdependence,
;;             :sub_type Event:Preparing,
;;             :timestamp 2022-03-16T16:03:02.550950Z,
;;             :experiment_id 3c652ea4-f9ea-4f8f-b899-350a89bdb695,
;;             :version 1.1.0,
;;             :trial_id d1774451-2dd5-47f8-ade2-c873d659a34b}}

(defn rita-handle-Preparing-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_ihmc_ta2")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_ihmc_ta2")))

;;           sub_type= Event:Utility
;;           tbm= {:data
;;                  {:utility NA},
;;                 :header
;;                   {:version 1.1,
;;                    :timestamp 2021-12-03T20:48:04.730937Z,
;;                    :message_type event},
;;                 :msg {:trial_id bcc7da14-764e-488d-ab74-a0ac967359b2,
;;                       :sub_type Event:Utility,
;;                       :source rutgers_utils_ac,
;;                       :version 0.1,
;;                       :timestamp 2021-12-03T20:48:04.730937Z,
;;                       :experiment_id 0386eac0-0dbd-4bdd-84d6-33f5b4074186}}


(defn rita-handle-Utility-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_ihmc_ta2")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_ihmc_ta2")))

(defn rita-handle-PlanningStage-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_ihmc_ta2")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_ihmc_ta2")))

;;; testbed message_type= agent sub_type= AC:belief_diff
;;; tbm= {:data {:BLUE_marker [0.696 1.013 0.0 0.0 1.136 0.0 ...],
;;;              :time_in_seconds 1015.375,
;;;              :BLUE_indiv [0.793 0.793 0.0 0.0 0.793 0.793 ...],
;;;              :RED_indiv [0.0 0.0 0.814 0.0 0.814 0.814 0.814 ...],
;;;              :GREEN_marker [0.0 0.0 0.0 0.0 1.626 0.0 1.626 ...],
;;;              :GREEN_indiv [0.0 0.0 0.0 0.0 1.549 0.0 1. ...],
;;;              :RED_marker [0.0 0.0 0.0 0.0 1.325 0.0 ...],
;;;              :shared [0.0 0.0 0.0 0.0 1.247 ...],
;;;              :room_id [A1 A2 A3 A4 A4A B1 B2 B3 ... overall]},
;;;              :header {:timestamp 2022-03-30T02:06:38.218000Z,
;;;                       :version 0.1,
;;;                       :message_type agent},
;;;              :msg {:sub_type AC:belief_diff,
;;;                    :replay_id a16920a1-b98a-4285-b749-b32d42656342,
;;;                    :trial_id edcfbabe-3da6-4896-bdd3-3f419132d352,
;;;                    :source AC_Rutgers_TA2_Utility,
;;;                    :replay_parent_id aec81ecb-7fdb-4b4b-9bf7-6ebab112021a,
;;;                    :experiment_id 1551846d-efe9-46cb-bffb-0c0f97c64689,
;;;                    :replay_parent_type TRIAL,
;;;                    :version 0.1,
;;;                    :timestamp 2022-03-30T02:06:38.218000Z}}

(defn rita-handle-ac_belief_diff-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_rutgers_utility_ta2")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_rutgers_utility_ta2")))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; CMU

;; message_type= agent sub_type= AC:BEARD
;; tbm= {:data {:GREEN_ASIST2
;;              {:role Transport_Specialist,
;;               :walking_skill -1.0,
;;               :mission_knowledge 30.0,
;;               :gaming_experience -1.0,
;;               :rmie -1.0,
;;               :sbsod -1.0,
;;               :anger 2.5,
;;               :victim_moving_skill -1.0,
;;               :anxiety 2.0},
;;              :BLUE_ASIST2
;;              {:role Engineering_Specialist,
;;               :walking_skill -1.0,
;;               :mission_knowledge 30.0,
;;               :gaming_experience -1.0,
;;               :rmie -1.0,
;;               :sbsod -1.0,
;;               :anger 2.0,
;;               :victim_moving_skill -1.0,
;;               :anxiety 2.0},
;;              :RED_ASIST2
;;              {:role Medical_Specialist,
;;               :walking_skill -1.0,
;;               :mission_knowledge 30.0,
;;               :gaming_experience -1.0,
;;               :rmie -1.0,
;;               :sbsod -1.0,
;;               :anger 1.0,
;;               :victim_moving_skill -1.0,
;;               :anxiety 1.0},
;;              :team
;;              {:mission_knowledge_mean 30.0,
;;               :mission_knowledge_sd 0.0,
;;               :walking_skill_mean -1.0,
;;               :sbsod_mean 0.0,
;;               :victim_moving_skill_sd -1.0,
;;               :gaming_experience_sd 0.0,
;;               :victim_moving_skill_mean -1.0,
;;               :anxiety_sd 0.471,
;;               :rmie_mean 0.0,
;;               :rmie_sd 0.0,
;;               :anxiety_mean 1.67,
;;               :sbsod_sd 0.0,
;;               :walking_skill_sd -1.0,
;;               :anger_mean 1.83,
;;               :anger_sd 0.624,
;;               :gaming_experience_mean 0.0}},
;;       :header {:version 1.1,
;;                :message_type agent,
;;                :timestamp 2022-01-19T22:35:50.209580Z},
;;       :msg {:sub_type AC:BEARD,
;;             :trial_id add1aeef-a1c0-4621-b7d6-51efe129c99c,
;;             :source ac_cmu_ta2_beard,
;;             :version 0.1,
;;             :experiment_id 934c548a-54ef-4e1e-bdbb-613bd395764b,
;;             :timestamp 2022-01-19T22:35:50.209580Z}}

(defn rita-handle-ac_beard-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_cmu_ta2_beard")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_cmu_ta2_beard")))

;; testbed message_type= agent sub_type= AC:TED
;; tbm= {:data {:elapsed_ms 100500.0,
;;              :process_skill_use_s 24.816,
;;              :message_freq 0.0,
;;              :triage_count 1.0,
;;              :process_workload_burnt_agg 0.0662,
;;              :process_effort_agg 0.0828,
;;              :dig_rubble_count 6.0,
;;              :process_coverage 56.0,
;;              :explore_count 56.0,
;;              :delta_ms 10019.0,
;;              :process_triaging_agg 0.0062,
;;              :action_dig_rubble_s 6.549,
;;              :process_workload_burnt 0.0062,
;;              :action_explore_s 15.494,
;;              :team_score 0.0,
;;              :process_skill_use_rel 0.9777,
;;              :process_coverage_agg 0.1263,
;;              :team_score_agg 0.0,
;;              :action_triage_s 2.773,
;;              :message_consistency_agg 0.0,
;;              :move_victim_count 0.0,
;;              :action_move_victim_s 0.0,
;;              :process_effort_s 25.383,
;;              :message_equity 0.0,
;;              :process_skill_use_agg 0.0581,
;;              :inaction_stand_s 4.674},
;;       :header {:version 1.1,
;;                :message_type agent,
;;                :timestamp 2022-01-19T22:37:30.707743Z},
;;       :msg {:sub_type AC:TED,
;;             :trial_id add1aeef-a1c0-4621-b7d6-51efe129c99c,
;;             :source cmuta2-ted-ac,
;;             :version 0.1,
;;             :experiment_id 934c548a-54ef-4e1e-bdbb-613bd395764b,
;;             :timestamp 2022-01-19T22:37:30.707743Z}}

(defn rita-handle-ac_ted-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_cmu_ta2_ted")
    (let []

      　nil)
    (seglob/warn-ignoring-message-one-time "ac_cmu_ta2_ted")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; UCF

;;; testbed message_type= agent sub_type= playerprofile
;;; tbm= {:data {:participant_id E000607,
;;;              :player-profile HighTaskLowTeam,
;;;              :role transporter,
;;;              :task-potential-category HighTask,
;;;              :callsign Green,
;;;              :team-potential-category LowTeam},
;;;       :header {:timestamp 2022-03-31T01:16:47.521621Z,
;;;                :version 1.1,
;;;                :message_type agent},
;;;       :msg {:trial_id 5a9fb978-c31a-4b44-9ead-94a02a4ec52d,
;;;             :version 0.1,
;;;             :sub_type playerprofile,
;;;             :source ac_ucf_ta2_playerprofiler,
;;;             :timestamp 2022-03-31T01:16:47.521621Z,
;;;             :experiment_id 1925b5cf-8fa6-4637-9bbe-fb5003b74341,
;;;             :replay_id 1993221a-3bcd-4bf8-aa50-556a2040e291,
;;;             :replay_parent_type TRIAL}}

(defn rita-handle-ac_ucf_ta2_playerprofile-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_ucf_ta2_playerprofiler")
    (let [{pid :participant_id,
           role :role,                                            ; eg. transporter
           callsign :callsign,                                    ; eg; Green
           profile :player-profile,                               ; eg. HighTaskLowTeam
           taskpotential :task-potential-category,                ; eg. HighTask
           teampotential :team-potential-category} (:data tbm)]   ; eg. LowTeam
      (println "Handling playerprofile message")
      (pprint (:data tbm))
      (case teampotential
        "LowTeam"
       (ie/vote-leader "ac_ucf_ta2_playerprofiler" pid 0.0 1.0 (seglob/get-last-ms-time)) ; Binary when all votes are in they will be normalized

        "HighTeam"
        (ie/vote-leader "ac_ucf_ta2_playerprofiler" pid 1.0 1.0 (seglob/get-last-ms-time)) ;

        (if (dplev :all :warn :error)
          (println "Warning: Unhandled value in ucf-player-profile:" teampotential)))
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_ucf_ta2_playerprofiler")))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Rutgers

;;; testbed message_type= agent sub_type= AC:threat_room_communication
;;; tbm= {:data {:nearest_room [H1 K2 H1 M3 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1
;;;                             H1 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1 H1],
;;;              :time_in_seconds -0.001,
;;;              :time_placed [409.883 651.132 704.983 758.532 798.583 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001
;;;                            -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001
;;;                            -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001 -0.001],
;;;              :is_observed_threat_room [false true false true false false false false false false false false
;;;                                        false false false false false false false false false false false false
;;;                                        false false false false false false false false false false],
;;;              :player_placed [GREEN_ASIST2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2 GREEN_ASIST2 BLUE_ASIST2 BLUE_ASIST2
;;;                              BLUE_ASIST2 BLUE_ASIST2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2
;;;                              RED_ASIST_2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2
;;;                              RED_ASIST_2 RED_ASIST_2 BLUE_ASIST2 BLUE_ASIST2 BLUE_ASIST2 BLUE_ASIST2 BLUE_ASIST2
;;;                              BLUE_ASIST2 BLUE_ASIST2 BLUE_ASIST2 BLUE_ASIST2 BLUE_ASIST2 BLUE_ASIST2]},
;;;       :header {:timestamp 2022-03-31T01:39:24.157936Z,
;;;                :version 0.1,
;;;                :message_type agent},
;;;       :msg {:trial_id 5a9fb978-c31a-4b44-9ead-94a02a4ec52d,
;;;             :version 0.2,
;;;             :sub_type AC:threat_room_communication,
;;;             :source AC_Rutgers_TA2_Utility,
;;;             :timestamp 2022-03-31T01:39:24.157936Z,
;;;             :experiment_id 1925b5cf-8fa6-4637-9bbe-fb5003b74341,
;;;             :replay_id 1993221a-3bcd-4bf8-aa50-556a2040e291,
;;;             :replay_parent_type TRIAL}}

(defn rita-handle-threat_room_communication-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_rutgers_ta2_utility")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_rutgers_ta2_utility")))

;;; testbed message_type= agent sub_type= AC:threat_room_coordination
;;; tbm= {:data {:threat_activation_time [237.488 322.085 361.434 415.487 442.533
;;;                                       483.134 586.034 642.094 725.435 775.233 855.634],
;;;       :time_in_seconds 912.882,
;;;       :threat_activation_player [RED_ASIST_2 BLUE_ASIST2 RED_ASIST_2 BLUE_ASIST2 RED_ASIST_2 RED_ASIST_2
;;;                                  BLUE_ASIST2 RED_ASIST_2 RED_ASIST_2 BLUE_ASIST2 RED_ASIST_2],
;;;       :threshold: 10.0,
;;;       :wait_time [23.144000000000005 16.897000000000048 26.700999999999965 2.69399999999996 141.94799999999998 99.798
;;;                   9.048000000000002 21.936999999999898 46.847000000000094 23.15000000000009 57.247999999999934],
;;;       :room_id [J4 K2 J4 J4 K2 K2 K2 K2 M3 M3 C2]},
;;;       :header {:timestamp 2022-03-31T01:36:16.606357Z,
;;;                :version 0.1,
;;;                :message_type agent},
;;;       :msg {:trial_id 5a9fb978-c31a-4b44-9ead-94a02a4ec52d,
;;;             :version 0.2,
;;;             :sub_type AC:threat_room_coordination,
;;;             :source AC_Rutgers_TA2_Utility,
;;;             :timestamp 2022-03-31T01:36:16.606357Z,
;;;             :experiment_id 1925b5cf-8fa6-4637-9bbe-fb5003b74341,
;;;             :replay_id 1993221a-3bcd-4bf8-aa50-556a2040e291,
;;;             :replay_parent_type TRIAL}}

(defn rita-handle-threat_room_coordination-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_rutgers_ta2_utility")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_rutgers_ta2_utility")))



;;; testbed message_type= agent sub_type= AC:victim_type_communication
;;; tbm= {:data {:time_in_seconds -0.001,
;;;       :victims_match_marker_block [15 15   20 10 6 28    ],
;;;       :vicitm_type_in_vicinity [B B B B A,C A B B    ],
;;;       :vicinity_threshold_in_blocks 2.0,
;;;       :marker_block_type [B B A A A A B B B A A A],
;;;       :time_placed [246.833 289.283 344.782 345.733 466.083 689.832 744.481 960.482 -0.001 -0.001 -0.001 -0.001],
;;;       :player_placed [RED_ASIST_2 RED_ASIST_2 GREEN_ASIST2 GREEN_ASIST2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2
;;;                           RED_ASIST_2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2 RED_ASIST_2]},
;;;       :header {:timestamp 2022-03-31T01:39:36.431993Z,
;;;                :version 0.1,
;;;                :message_type agent},
;;;       :msg {:trial_id 5a9fb978-c31a-4b44-9ead-94a02a4ec52d,
;;;             :version 0.2,
;;;             :sub_type AC:victim_type_communication,
;;;             :source AC_Rutgers_TA2_Utility,
;;;             :timestamp 2022-03-31T01:39:36.431993Z,
;;;             :experiment_id 1925b5cf-8fa6-4637-9bbe-fb5003b74341,
;;;             :replay_id 1993221a-3bcd-4bf8-aa50-556a2040e291,
;;;             :replay_parent_type TRIAL}}

(defn rita-handle-victim_type_communication-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_rutgers_ta2_utility")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_rutgers_ta2_utility")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Cornell (not yet)

;; testbed message_type= agent sub_type=AC:Player_compliance
;; tbm= {:data {:N_requests_open_blue 0.0,
;;              :elapsed_ms 320143.0,
;;              :N_requests_open_red 2.0,
;;              :compliance_ratio_green 0.8,
;;              :compliance_ratio_blue 1.0,
;;              :avg_response_time_red 66914.0,
;;              :avg_response_time_green 85942.66666666667,
;;              :avg_response_time_blue 129452.5,
;;              :N_requests_open_green 1.0,
;;              :compliance_ratio_red 0.5},
;;       :header {:version 1.1,
;;                :message_type agent,
;;                :timestamp 2022-01-19T22:41:10.347026Z},
;;       :msg {:sub_type AC:Player_compliance,
;;             :trial_id add1aeef-a1c0-4621-b7d6-51efe129c99c,
;;             :source AC_CORNELL_TA2_TEAMTRUST,
;;             :version 1.0,
;;             :experiment_id 934c548a-54ef-4e1e-bdbb-613bd395764b,
;;             :timestamp 2022-01-19T22:41:10.347026Z}}

(defn rita-handle-ac_player_compliance-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_cornell_ta2_teamtrust")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_cornell_ta2_teamtrust")))

;;; Unhandled testbed message_type= agent sub_type= AC:Goal_alignment
;;; tbm= {:data {:Team {:goal_alignment_overall 0.10436100980079804,
;;;                     :goal_alignment_current false,
;;;                     :goal_alignment_recent 0.0},
;;;              :elapsed_ms 1017682.0,
;;;              :Red {:goal_alignment_overall {:Green 0.5282872999570007, :Blue 0.23859562740480458},
;;;                    :goal_alignment_current {:Green true, :Blue false},
;;;                    :current_goal transport,
;;;                    :goal_alignment_recent {:Green 0.6799637124209359, :Blue 0.06758561600685432}},
;;;              :Green {:goal_alignment_overall {:Red 0.5282872999570007, :Blue 0.32820642499236913},
;;;                      :goal_alignment_current {:Red true, :Blue false},
;;;                      :current_goal transport,
;;;                      :goal_alignment_recent {:Red 0.6799637124209359, :Blue 0.14532671421011517}},
;;;              :Blue {:goal_alignment_overall {:Red 0.23859562740480458, :Green 0.32820642499236913},
;;;                     :goal_alignment_current {:Red false, :Green false},
;;;                     :current_goal explore,
;;;                     :goal_alignment_recent {:Red 0.06758561600685432, :Green 0.14532671421011517}}},
;;;       :header {:timestamp 2022-03-30T02:06:37.053000Z,
;;;                :version 1.1,
;;;                :message_type agent},
;;;       :msg {:sub_type AC:Goal_alignment,
;;;             :replay_id a16920a1-b98a-4285-b749-b32d42656342,
;;;             :trial_id edcfbabe-3da6-4896-bdd3-3f419132d352,
;;;             :source AC_CORNELL_TA2_TEAMTRUST,
;;;             :replay_parent_id aec81ecb-7fdb-4b4b-9bf7-6ebab112021a,
;;;             :experiment_id 1551846d-efe9-46cb-bffb-0c0f97c64689,
;;;             :replay_parent_type TRIAL,
;;;             :version 1.0,
;;;             :timestamp 2022-03-30T02:06:37.053000Z}}

(defn rita-handle-ac_goal_alignment-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_cornell_ta2_teamtrust")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_cornell_ta2_teamtrust")))

;; testbed message_type= agent sub_type= rollcall:response
;; tbm= {:data {:version 0.0.1,
;;              :uptime 11079.164945,
;;              :status up,
;;              :rollcall_id 65743884-7e47-453d-bbff-e81bfd18ef81},
;;       :header {:version 0.6,
;;                :message_type agent,
;;                :timestamp 2022-01-19T22:33:13.942872Z},
;;       :msg {:sub_type rollcall:response,
;;             :trial_id add1aeef-a1c0-4621-b7d6-51efe129c99c,
;;             :source AC_CORNELL_TA2_TEAMTRUST,
;;             :version 0.1,
;;             :experiment_id 934c548a-54ef-4e1e-bdbb-613bd395764b,
;;             :timestamp 2022-01-19T22:33:13.942872Z}}

(defn rita-handle-ac_rollcall_response-message
  [tbm tb-version]
  (if (seglob/observing-ac "ac_cornell_ta2_teamtrust")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_cornell_ta2_teamtrust")))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; UAZ

;; testbed message_type= agent sub_type= versioninfo
;; tbm= {:data {:agent_name speech_analyzer_agent,
;;              :owner UAZ, :version 3.3.1,
;;              :publishes [{:sub_type asr:transcription,
;;                           :message_type observation,
;;                           :topic agent/asr/intermediate}
;;                          {:sub_type asr:transcription,
;;                           :message_type observation,
;;                           :topic agent/asr/final}],
;;              :subscribes [{:sub_type start,
;;                            :message_type trial,
;;                            :topic trial}
;;                           {:sub_type stop,
;;                            :message_type trial,
;;                            :topic trial}],
;;              :config []},
;;       :header {:version 0.1,
;;                :message_type agent,
;;                :timestamp 2022-01-19T22:33:10.561315Z},
;;       :msg {:sub_type versioninfo,
;;             :trial_id add1aeef-a1c0-4621-b7d6-51efe129c99c,
;;             :source speech_analyzer_agent,
;;             :version 0.1,
;;             :experiment_id 934c548a-54ef-4e1e-bdbb-613bd395764b,
;;             :timestamp 2022-01-19T22:33:10.561315Z}}

(defn rita-handle-versioninfo-message
  [tbm tb-version]
  (if (seglob/observing-ac "speech_analyzer_agent")
    (do ;+++ do something here
      　nil)
    (seglob/warn-ignoring-message-one-time "ac_cornell_ta2_teamtrust")))

;;; Fin
