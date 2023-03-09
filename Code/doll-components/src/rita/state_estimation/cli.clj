;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.cli
  "RITA AttackModelGenerator main."
  (:import java.util.Date)
  (:require [clojure.tools.cli :as cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [clojure.data.codec.base64 :as base64]
            [clojure.string :as string]
            [clojure.pprint :as pp :refer [pprint]]
            [me.raynes.fs :as fs]
            [clojure.tools.logging :as log]
            [clojure.instant :as instant]
            [environ.core :refer [env]]
            [mbroker.rabbitmq :as rmq]
            [mbroker.asist-msg :as asist-msg]
            [clojure.java.shell :as shell]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [rita.common.core :as rc :refer :all]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            [rita.state-estimation.planning :as plan]
            [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
            [pamela.tools.belief-state-planner.montecarloplanner :as bs]
            [pamela.tools.belief-state-planner.ir-extraction :as irx]
            [pamela.tools.utils.util :as pt-util]
            ;[rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.rita-se-core :as rsc]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.rlbotif :as rlbot]
            [rita.state-estimation.observations :as obs]
            [clojure.java.io :as io])
  (:gen-class)) ;; required for uberjar

#_(in-ns 'rita.state-estimation.cli)

;;;+++ moveme
;(def ^:dynamic *debug-level* #{:demo :interventions }) ; :all
;(defn dplev
;  [& x]
;  (some *debug-level* x))

(defonce tb-msg-count (atom 0))
(def debug false)

(defn testx
  []
  (asist-msg/publish-intervention-chat-msg-mqtt
   (asist-msg/make-intervention-chat-message ["foo"] "bar" 10000 {:some-explaination ["here"]} 1000 (System/currentTimeMillis) "baz" "quux")
   @mqtt-connection))

(defn update-tb-msg-count []
  #_(when (dplev :all)
      (println "java all thread state")
      (pprint (pt-util/get-all-threads)))
  (swap! tb-msg-count inc)
  (when debug (= 0 (mod @tb-msg-count 100))
    (when (dplev :all) (println "SE received tb messages: " @tb-msg-count (pt-util/getCurrentThreadName)))))

(defn shutdown []
  (println "Received Control-C ")
  (println "Final count of tb messages: " @tb-msg-count))

;;; Startup message carries important information for the run

(defcondition startup-rita-observed [startup-rita] [ch]
  (seglob/message-received)
  (let [mission-id (:mission-id startup-rita)
        lpm (:learned-participant-model startup-rita)]
    (when lpm
      (let [data (read-string lpm)]
        (if (and data (= (:format-version data) slearn/learned-model-format))
          (do
            (when (dplev :all :io)
              (println "RITA started with a correctly formatted LPM"))
            (seglob/set-learned-model! data true)) ; overwrite default model
          (when (dplev :error :all :io)
            (println "Error: the received learned model was not of the correct format ("
                     (:format-version data) ") should be:"  slearn/learned-model-format)))
        (when (dplev :io :all) (println "Startup-RITA message received for mission" mission-id startup-rita))))))

;;; Startup message carries important information for the run

(defcondition shutdown-rita-observed [shutdown-rita] [ch]
  (seglob/message-received)
  (let [mission-id (:mission-id shutdown-rita)]
    (when (dplev :io :all) (println "Shutdown-RITA message received for mission" mission-id shutdown-rita))))

(defcondition timing-data-extraction [start-timing-data-extraction] [ch]
  (seglob/message-received)
  (let [mission-id (:mission-id start-timing-data-extraction)
        rtd (:rtd-filename start-timing-data-extraction)]
    (if rtd (seglob/set-rtd-pathname rtd))
    (when (dplev :all) (println "Extracting timing data to:" rtd))))

(defn remote-create-learned-participant
  [lpm-file rtd-files]
  (rsc/state-estimation-initialization "Saturn" "1.00" 3 1 "none" []) ; Initialize the Falcon Model +++
  (cond (not (string? lpm-file))
        (when (dplev :error :all) (println "lpm-file" lpm-file "is not a string."))

        (not (vector? rtd-files))
        (when (dplev :error :all) (println "rtd-files" rtd-files "is not a vector of pathnames"))

        (not (every? string? rtd-files))
        (when (dplev :error :all) (println "at least 1 of the rtd-files (" rtd-files ") is not a string"))

        :otherwise
        (let [datasets (slearn/read-training-data-from-list-of-files rtd-files)]
          ;; +++ initialize the world first +++
          (when (dplev :io :all) (println "read" (count rtd-files) "rtd files"))
          (slearn/write-learned-data datasets lpm-file)
          (when (dplev :io :all) (println "Generated lpm file" lpm-file)))))

;; (defn test-remote-learned-participant
;;   []
;;   (remote-create-learned-participant
;;    "test.lpm"
;;    ["/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/00efb266-b7c5-4b13-9a98-18c46141151c-1-T000055-Research_Account.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/010689c1-dac0-45e6-a7df-ef80df15d64c-1-T000208-Research_Account.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/01ebfe8d-0006-4131-b50a-44447020b476-1-T000133-ASIST1.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/05e93dd9-8673-4931-b4de-94e9d5674b8c-1-T0000177-Research_Account.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/06757476-729c-4895-bd48-13e4816fe13e-1-T000171-ASIST5.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/08658ca3-8bd8-41db-8282-2b401633288d-1-T000245-Research_Account.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/099ae37e-2ac8-4b9e-9476-36b10420df2a-1-T000269-ASIST1.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/0a8e9b06-4906-41f5-b4e2-c9552b1ec496-1-T000186-Research_Account.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/0b627eac-7ec4-45fc-a0ad-a2f4a0d4c2d2-1-T000111-ASIST5.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/0c9b2b79-9168-4c2c-abff-ae4114e7a916-1-T000112-Research_Account.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/0e8e17c2-5655-44f1-836a-0711236f8e67-1-T000117-ASIST1.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/0fa850fd-45ae-4f10-a962-26c7c03fe4fe-1-T000130-Research_Account.edn"
;;     "/Users/paulr/checkouts/bitbucket/asist_rita/Code/train/0ff3e12a-7fc0-4503-b8a3-ac0c3a99e2f4-1-T000095-ASIST5.edn"]))


(defcondition time-to-create-an-lpm [create-learned-participant-model] [ch]
  (seglob/message-received)
  (let [mission-id (:mission-id create-learned-participant-model)
        lpm-file (:lpm-file create-learned-participant-model)
        rtd-files (:training-rtd-files create-learned-participant-model)]
    (remote-create-learned-participant lpm-file rtd-files)))

(defcondition new-plan-request-received [new-plan-request] [ch]
  (seglob/message-received)
  (when (dplev :all) (println "Generative Planner got new plan request for mission" (:mission-id new-plan-request)))
  (let [{:keys [hypothesis-id fail-reason]} (:new-plan-request new-plan-request)]
    (when (dplev :all) (println "new-plan-request-received reason=" fail-reason  "\nhypothesis-id=" hypothesis-id))
    ;; +++ use fail-reason
    (plan/enqueue-new-plan-request-reason hypothesis-id)
    (when (dplev :all) (println "se: after new plan request"))))

(defcondition testbed-message [testbed-message] [ch]
  (seglob/message-received)
  (when (dplev :all)
    (println "Testbed message received: " testbed-message))
  (update-tb-msg-count)
  (let [mission-id (:mission-id testbed-message)
        tbm (:testbed-message testbed-message)
        ts (:timestamp (:msg tbm))
        dt (and ts (clojure.instant/read-instant-date ts))
        mstime (and ts (java.util.Date/.getTime dt))
        data (:data tbm)
        ;;nutime (obs/set-rita-ms-time mstime)
        results (if (not (empty? (:msg tbm)))
                  #_(obs/handle-testbed-message tbm)
                  (obs/handle-testbed-message-with-queues tbm))]

    ;; (if (not (empty? results)) (when (dplev :all) (println "*** RESULTS=" results)))
    (if (not (empty? results))
      (doseq [{mtopic :mtopic
               mtype :mtype
               msg :message}
              results]
        ;;(when (dplev :all) (println "ch=" ch "mission-id=" mission-id "mtopic=" mtopic "mtype=" mtype "msg=" msg))
        (case :mtype
          :generated-plan (publish-message ch mission-id mtopic msg)

          (publish-message ch mission-id mtopic (if mtype {mtype msg})))))))

;; const dataTest = {
;;         'routing-key': 'control-panel',
;;         'app-id': 'rita-controller',
;;          data: {
;;           ac_aptima_ta3_measures: false,
;;           ac_cmu_ta2_beard: false,
;;           ac_cmu_ta2_ted: false,
;;           ac_cornell_ta2_teamtrust: false,
;;           'ac_cornell_ta2_asi-facework': false,
;;           ac_gallup_ta2_gelp: false,
;;           ac_cmufms_ta2_cognitive: false,
;;           ac_rutgers_ta2_utility: false,
;;           ac_ucf_ta2_playerprofiler: false,
;;         },
;;       };


(defcondition ac-controller  [ac-controller] [ch]
  (seglob/message-received)
  (if (= (:app-id ac-controller) "control-panel")
    (let [data (:data ac-controller)]
      (println "Received control panel AC usage switches:")
      (pprint data)
      (if data (seglob/set-ac-usage data))
      (seglob/print-ac-usage))
    (println "Received wrong format control-panel message" ac-controller)))

(def previous-time -9999999)
(def previous-clock-message nil)
(def clock-counter 0)

(defcondition clock-message [clock] [ch]
  (seglob/message-received)
  (if (== (mod clock-counter 1000) 0) (when (dplev :all) (println "clock message received: " clock)))
  (locking seglob/clocklock
    (if (or (= (:app-id clock) "TestbedBusInterface")
            (= (:app-id clock) "log-player"))
      (let [ts (:timestamp clock)]
        (seglob/set-mqtt-connection @mqtt-connection)
        (def clock-counter (+ clock-counter 1))
        (if (< ts previous-time)
          (do
            (when (dplev :all) (println "*** new ts" ts " earlier than last" previous-time "by" (- previous-time ts) "milliseconds"))
            (when (dplev :all) (println "*** prv message:" previous-clock-message "latest clock message:" clock))))
        (cond (and previous-time (or (> (- ts previous-time) 10000) (> (- previous-time ts) 10000)))
              (do
                #_(when (dplev :all) (println "***** Curious jump in time, counter=" clock-counter
                           "from" previous-time
                           "to" ts
                           "by" (- ts previous-time) "milliseconds"))
                (def previous-time ts)
                (def previous-clock-message clock)
                (seglob/set-rita-ms-time ts))

              :otherwise
              (do (def previous-time ts)
                  (def previous-clock-message clock)
                  (seglob/set-rita-ms-time ts)))
        ts)
      (do
        (when (dplev :all) (println "*** Ignoring clock message with :app-id =" (pr-str (:app-id clock)) "message=" (pr-str clock)))
        (:timestamp clock)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; :ground_truth_prompt_msgs
;; [{:resolved_elapsed_time 62918,
;;   :Block Location: Z 23,
;;   :Ground Truth redacted,
;;   :Trial T000422,
;;   :Subject E000332,
;;   :Block Location: X -2090,
;;   :Door ID c_110_-52_111_-51,
;;   :Start Elapsed Time 60757,
;;   :Measure M7,
;;   :Trial ID 9035290e-dd17-4cd3-bb13-c9f5a4265d78,
;;   :Block Type Marker Block 1, :Block Placed By Red}
;;   ...  ]

(defcondition ground_truth_prompt [ground_truth_prompt] [ch]
  (seglob/message-received)
  (when (dplev :all) (println "m7 ground truth prompt message received"))
  (let [gtpms (:ground_truth_prompt_msgs ground_truth_prompt)]
    (doseq [gtpm gtpms]
      (let [tl-x (get gtpm (keyword "Block Location: X"))
            tl-y (get gtpm (keyword "Block Location: Y") 60)
            tl-z (get gtpm (keyword "Block Location: Z"))
            bl-measure (get gtpm (keyword "Measure"))
            bl-type (get gtpm (keyword "Block Type"))
            bl-placed-by (get gtpm (keyword "Block Placed By"))
            subj (get gtpm (keyword "Subject"))
            door-id (get gtpm (keyword "Door ID"))]
        #_ (when (dplev :all) (println "(Door" tl-x tl-z tl-y tl-x tl-z (+ tl-y 1) door-id ") ; marker placed by" subj "marker type" bl-type "measure" bl-measure))
        nil))
    (seglob/set-m7-ground-truth-prompt gtpms))
  nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; This section will ultimately be transferred to generative planner

;; This is an example handler used only to test RITA's inter-component plumbling
;; It should be replaced with somethig real
#_(defcondition mission-pamela-and-solver [mission-pamela mission-solver] [ch]
  (let [{:keys [mission-id mission-pamela mission-ir-json]} mission-pamela
        {solver :mission-solver} mission-solver]
    (when (dplev :all)
      (println "Mission-Pamela and Mission-Solver message received for mission" mission-id)
      (pprint mission-pamela)
      (pprint mission-ir-json)
      (pprint solver))

    (let [htn {:name "test-htn"}
          tpn {:name "test-tpn"}
          current-state {:name "test-current-state"}]
      (publish-message ch mission-id "generated-plan"
                       {:htn htn
                        :tpn tpn
                        :current-state current-state}))))

#_(defn test-send-generated-plan [& [fname]]
  (let [;_htn (slurp "resources/public/test-environment.htn.json")
        fname (if fname fname
                        "resources/public/test-environment.tpn.json"
                        ;"test/example-plan/example-plan.tpn.json"
                        )
        tpn (pt-json/from-file fname)]
    (when (dplev :all) (println "test-send-generated-plan Publishing " fname))
    (publish-message (get-channel) "test-mission-id" "generated-plan" {:htn           {}
                                                                       :tpn           tpn
                                                                       :current-state {}})))
#_(defn publish-plan
  []
  (let [pamela-filename (io/resource (str "public/" "test-environment.pamela"))
        _ (assert pamela-filename)
        pamela-model (slurp pamela-filename)
        model-in-ir-json (pamela-string-to-ir-json pamela-model)
        ]
    (publish-message (:channel conn-info) mission-id "mission-pamela"
                     {:mission-pamela pamela-model
                      :mission-ir-json model-in-ir-json})
    (publish-message (:channel conn-info) mission-id "mission-solver"
                     {:mission-solver {}})))


(defmain -main 'StateEstimation
  ;;Optionally add some initialization code here
  ;;(slearn/initialize-stat-record)
  (seglob/reset-total-messages-received)
  (seglob/set-stat-record! nil)
  (seglob/verify-data-directory-exists)
  ;; (if (System/getenv "RITA_TEST")
  ;;   (do
  ;;     (when (dplev :all) (println "TESTING remote learner"))
  ;;     (test-remote-learned-participant)))
  (.addShutdownHook (Runtime/getRuntime) (Thread. shutdown))
  (make-mqtt-connection)
  (seglob/set-mqtt-connection @mqtt-connection)
  (obs/start-rita-brain-thread)
  (Thread/sleep 3000)                  ; Wait for a minute to let SE stabalize +++ temporary - remove later
  #_(rlbot/start-rlbot-thread))

;;; (-main)
