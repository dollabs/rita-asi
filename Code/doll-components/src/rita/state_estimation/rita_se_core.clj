;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.rita-se-core
  "RITA State Estimation."
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
            [rita.state-estimation.teamstrength :as ts]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.ritamessages :as ritamsg]
            [rita.state-estimation.interventions :as intervene]
            [rita.state-estimation.predictions :as predict]
            [rita.state-estimation.ras :as ras]
            [rita.state-estimation.study2 :as study2]
            [rita.state-estimation.study3 :as study3]
            [rita.state-estimation.victims :as victims]
            [rita.state-estimation.planning :as plan]
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

#_(in-ns 'rita.state-estimation.rita-se-core)

(defmacro build-string
  [build-prefix]
  (str build-prefix "-built-" (.toString (java.time.LocalDateTime/now))))

(def ^:dynamic *se-build-number* (build-string "Saturn-2109031215"))

;;; publish to RMQ AND MQTT

(defn publish-rmq-mqtt
  [type-of-message the-message]
  ;; +++ track number of each type of message sent during a trial +++
  (ritamsg/add-rmq-and-mqtt-message type-of-message the-message))

(defn get-build-string
  []
  *se-build-number*)



(defn set-current-role!
  [pid role tm]
  (let [previous-role (seglob/get-roles pid)]
    (if (and previous-role (not (= (first previous-role) "None")))
      (let [[prole prolest] previous-role
            prtime (- tm prolest)]
        (slearn/add-role-time! pid prole (/ prolest 1000.0) (/ prtime 1000.0)))))
  (seglob/set-roles (merge (seglob/get-role-assignments) {pid [role tm]})))

(defn establish-role-profile
  []
  (loop [roles (vals (seglob/get-role-assignments))
         search 0
         med 0
         hammer 0]
    (let [rroles (rest roles)]
      (if (empty? roles)
        [hammer med search]
        (case (first roles)
          "Search_Specialist"             (recur rroles (+ search 1) med hammer)
          "Engineering_Specialist"        (recur rroles search med (+ hammer 1))
          "Medical_Specialist"            (recur rroles search (+ med 1) hammer)
          "Transport_Specialist"          (recur rroles (+ search 1) med hammer)
          "Hazardous_Material_Specialist" (recur rroles search med (+ hammer 1))
                                          (recur rroles search med hammer))))))

(defn num-meds
  []
  (nth (establish-role-profile) 1))

(defn num-search
  []
  (nth (establish-role-profile) 2))

(defn num-hazard
  []
  (nth (establish-role-profile) 0))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Map semantics (M3)

(defn in-area2?
  [x y z]
  (and (< x -2185) (>= y 27)))

(defn in-area3?
  [x y z]
  (and (< y 27) (>= x -2185) (<= x 2134)))

(defn in-area6?
  [x y z]
  (and (> x -2134) (>= y 27)))

;; (defn get-map-assignment-beliefs
;;   []
;;   (let [teammembers (seglob/get-team-members)
;;         maps ["SaturnA_24" "SaturnA_34" "SaturnA_64"] ; or ["SaturnB_24" "SaturnB_34" "SaturnB_64"]
;;         assignments (into [] (map (fn[tm mp] {:participant_id tm :map mp}) teammembers maps))]
;;     assignments))


(defn establish-prior-beliefs-about-map-assignment
  [subjects]
  (let [prior-beliefs-about-map-assignment
        (into {} (map
                  (fn [participant] {participant
                                     {"_24" (/ 1.0 6.0),
                                      "_34" (/ 1.0 6.0),
                                      "_64" (/ 1.0 6.0)}})
                  subjects))]
    (seglob/set-belief-state! "map-assignments" prior-beliefs-about-map-assignment)))

(defn update-map-assignment-and-report
  [pubs id em]
  ;; Generate a prediction of marker semantics
  ;; Report
  (study2/predict-m3-asist pubs id em))

(def ^:dynamic *area-entered* {:area2 nil, :area3 nil, :area6 nil})

;;;+++ add case for not being the first to visit an area
(defn maybe-report-m3-metric
  [pubs id participantid x y z elapsed_milliseconds]
  (let [prior-beliefs (seglob/get-belief-state "map-assignments")
        participant-beliefs (get prior-beliefs participantid)
        best-belief (or (and participant-beliefs (val (apply max-key val participant-beliefs))) 0.0)] ; robust against unknown participants
    #_(if (dplev :all) (println "**** maybe-report-m3-metric: prior-beliefs=" prior-beliefs "priors for" participantid "=" (get prior-beliefs participantid) "best=" best-belief))
    (cond (and (in-area2? x y z)
               (not (:area2 *area-entered*)) ; First to enter this area
               (< best-belief 0.7))     ; Don't second guess his map assignment
          (do
            (def ^:dynamic *area-entered* (merge *area-entered* {:area2 participantid}))
            (seglob/update-belief! "map-assignments" participantid {"_24" 0.8, "_34" 0.1, "_64" 0.1})
            (update-map-assignment-and-report pubs id elapsed_milliseconds))

          (and (in-area3? x y z)
               (not (:area3 *area-entered*))
               (< best-belief 0.7))     ; Don't second guess his map assignment
          (do
            (def ^:dynamic *area-entered* (merge *area-entered* {:area3 participantid}))
            (seglob/update-belief! "map-assignments" participantid {"_24" 0.1, "_34" 0.8, "_64" 0.1})
            (update-map-assignment-and-report pubs id elapsed_milliseconds))

          (and (in-area6? x y z)
               (not (:area6 *area-entered*))
               (< best-belief 0.7))     ; Don't second guess his map assignment
          (do
            (def ^:dynamic *area-entered* (merge *area-entered* {:area6 participantid}))
            (seglob/update-belief! "map-assignments" participantid {"_24" 0.1, "_34" 0.1, "_64" 0.8})
            (update-map-assignment-and-report pubs id elapsed_milliseconds))

          :otherwise pubs)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; export RITA_TARGET_PLAN_LENGTH=100
;;; export RITA_PERIODIC_REPLAN_FREQUENCY=500
;;; export RITA_EXIT_AT_END_OF_MISSION=true

(defn num-beeps-for-room
  [aroom]
  (let [now (seglob/rita-ms-time)
        lbr (seglob/get-last-beeped-room)]
    (if (and lbr
             (> (- now (nth lbr 2)) 20000)) ;+++ constant
      (seglob/set-last-beeped-room nil)) ; expire old beeps
    (if (and lbr
             (< (- now (nth lbr 2)) 20000) ; 20 seconds +++ constant
             (= aroom (nth lbr 0)))
      (nth lbr 1)
      0)))

(defn room-already-visited?
  [aroom]
  (> (bs/get-belief-in-variable (global/RTobject-variable aroom) :visited) 0.8))


(def ^:dynamic *debug-verbosity* 1)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; data collection

(def ^:dynamic *in-room* nil)           ; Keep track of how long we are in a room

(defn set-in-room
  [nuval]
  (def ^:dynamic *in-room* nuval))

;;;
(def ^:dynamic *room-to-room* nil)      ; Keep track of trajectories between rooms.


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Make minecraft names more readable and understandable

(defn xlate-name
  [objname]
  (case objname
    "wall_sign"      "wall sign"
    "wool"           "wool"
    "lever"          "switch"
    "wooden_button"  "wooden button"
    "stone_button"   "stone button"
    "flower_pot"     "flower pot"
    "prismarine"     "green victim"
    "block_victim_1" "green victim"
    "gold_block"     "gold victim"
    "block_victim_2" "gold victim"
    objname))

(def ^:dynamic *victims-state* {})
(def ^:dynamic *victims-encountered* {}) ; a map of noncritical victims encountered by role
(def ^:dynamic *critical-victims-encountered* {}) ; a map of critical victims encountered by role

(defn critical-victim?
  [obj]
  (if (global/RTobject? obj)
    (case (global/RTobject-type obj)
      block_victim_1 false
      block_victim_2 true
      false)
    false))

;;; *victims-state* indicates the state of a victim, if it is nil, it has not been processed

(defn set-normal-victims-encountered
  [role newvalue]
  (let [deleteset (set (filter (fn [v] (get *victims-state* v)) newvalue))    ; have been processed
        addset (set (filter (fn [v] (not (get *victims-state* v))) newvalue)) ; have not been processed
        victims-encountered (clojure.set/union (clojure.set/difference (get *victims-encountered* role #{}) deleteset) addset)]
    (when (not (= victims-encountered (get *victims-encountered* role #{})))
      #_(when (dplev :all) (println "For role" role "normal untriaged victims changed to" (map global/RTobject-variable victims-encountered)
               "addset=" (map global/RTobject-variable addset)
               "deleteset=" (map global/RTobject-variable deleteset)))
      (def ^:dynamic *victims-encountered* (merge *victims-encountered* {role victims-encountered})))))

(defn set-critical-victims-encountered
  [role newvalue]
  (let [deleteset (set (filter (fn [v] (get *victims-state* v)) newvalue))    ; have been processed
        addset (set (filter (fn [v] (not (get *victims-state* v))) newvalue)) ; have not been processed
        critical-victims-encountered (clojure.set/union (clojure.set/difference (get *critical-victims-encountered* role #{}) deleteset) addset)]
    (when (not (= critical-victims-encountered (get *critical-victims-encountered* role #{})))
      #_(when (dplev :all) (println "For role" role "critical untriaged victims changed to" (map global/RTobject-variable critical-victims-encountered)
               "addset=" (map global/RTobject-variable addset)
               "deleteset=" (map global/RTobject-variable deleteset)))
      (def ^:dynamic *critical-victims-encountered* (merge *critical-victims-encountered* {role critical-victims-encountered})))))

(defn set-victims-encountered
  [role newvalue]
  (let [cvs (filter critical-victim? newvalue)
        ncvs (filter (fn [x] (and (global/RTobject? x) (not (critical-victim? x)))) newvalue)]
    (if (not (empty? cvs))  (set-critical-victims-encountered role cvs))
    (if (not (empty? ncvs)) (set-normal-victims-encountered role ncvs))))

(defn untriaged-normal-victims
  [role]
  (get *victims-encountered* role))

(defn untriaged-critical-victims
  [role]
  (get *critical-victims-encountered* role))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Reporting belief state changes

(def ^:dynamic *close-to* {})
(def ^:dynamic *last-reported-closeto* 0)
(def ^:dynamic *closeness-reporting-minimum-interval* 60000)
(def ^:dynamic *room-visited-knowledge* {})

(defn reset-close-to
  []
  (def ^:dynamic *last-reported-closeto* 0)
  (def ^:dynamic *close-to* {}))

#_(defn make-chat-message
  [to what when trial expt]
  {:to to,
   :what what,
   :when when,
   :trial trial
   :experiment expt})

(defn pid-string
  [pid-all-formats]
  (cond (string? pid-all-formats)
        pid-all-formats

        (symbol? pid-all-formats)
        (str pid-all-formats)

        (keyword? pid-all-formats)
        (subs (str pid-all-formats) 1)

        :otherwise (str pid-all-formats)))

(defn maybe-report-close-to
  [pubs id participantid close-to-pid em]
  (if (> *last-reported-closeto* em) (reset-close-to))
  (let [old-close-to-db *close-to*
        old-close-to-pid-set (get old-close-to-db  participantid)
        close-to-pid-set (into #{} close-to-pid)
        sincelast (- em *last-reported-closeto*)
       ]
    ;;  Update close-to db

    (def ^:dynamic *close-to* (merge *close-to* {participantid close-to-pid-set}))

    ;; Now, do re report anythin?
    (cond (or (< sincelast *closeness-reporting-minimum-interval*)
              (empty? close-to-pid-set))
          pubs                          ; Too early to report or no closeness to report

          (not (empty? (clojure.set/difference old-close-to-pid-set close-to-pid-set)))
          pubs
          ;; Intervention obsoleted by the new intervention mechanism
          #_(let [intervention (if (= (count close-to-pid-set) 1)
                               (str "Bravo, " (seglob/pid2callsign (pid-string participantid))
                                    " and " (seglob/pid2callsign (pid-string (seglob/get-object-name-from-object-id (first close-to-pid))))
                                    " are working together!")
                               "Bravo, everyone is working together!")
                cmsg (asist-msg/make-intervention-chat-message
                      (into [] (map str ;(map seglob/pid2callsign
                                    (conj (map (fn [pidkeyword] (subs (str pidkeyword) 1))
                                               (map seglob/get-object-name-from-object-id
                                               close-to-pid))
                                          participantid)))
                      intervention
                      nil
                      nil
                      em
                      (seglob/rita-ms-time)
                      (seglob/get-trial-id)
                      (seglob/get-experiment-id))]
            (when (dplev :intervention :demo :all)
              (println "***Publishing intervention via intervention chat message to MQTT:" (seglob/get-mqtt-connection))
              (pprint cmsg))
            (asist-msg/publish-intervention-chat-msg-mqtt cmsg  (seglob/get-mqtt-connection))
            (ritamsg/add-message pubs "chat-intervention" :chat-intervention cmsg))

          :otherwise
          pubs)))

(def ^:dynamic *room-enter-encouragement* true) ;+++

(defn maybe-intervene-with-some-encouragement
  [pub id participantid vroomnames proomnames em]
  (cond (or (not *room-enter-encouragement*)
            (and (get *room-visited-knowledge* (first vroomnames))
                 (get *room-visited-knowledge* (second vroomnames))))
        pub                                 ; Been there, nothing to add

        (and *room-enter-encouragement*
             (not (get *room-visited-knowledge* (first vroomnames))))
        (let [intervention  (str "Hey, " (seglob/pid2callsign (pid-string participantid))
                                 ", you are the first person to enter" (first proomnames)
                                 ". Keep the team appraised of what you find.")
              cmsg (asist-msg/make-intervention-chat-message
                    [participantid]
                    intervention
                    nil
                    nil
                    em
                    (seglob/rita-ms-time)
                    (seglob/get-trial-id)
                    (seglob/get-experiment-id))]
          (def ^:dynamic *room-visited-knowledge* (conj *room-visited-knowledge* {(first vroomnames) [id em]}))
          (def ^:dynamic *room-enter-encouragement* false)
          (when (dplev :intervention :demo :all)
            (println "***Publishing intervention via intervention chat message to MQTT:" (seglob/get-mqtt-connection))
            (pprint cmsg))
          (asist-msg/publish-intervention-chat-msg-mqtt cmsg  (seglob/get-mqtt-connection))
          pub)

        (and *room-enter-encouragement*
             (> (count vroomnames) 1)
             (not (get *room-visited-knowledge* (second vroomnames))))
        (let [intervention (str "Hey, " (seglob/pid2callsign (pid-string participantid))
                                ", you are the first person to enter " (second proomnames)
                                ". Keep the team appraised of what you find.")
              cmsg (asist-msg/make-intervention-chat-message
                    [participantid]
                    intervention
                    nil
                    nil
                    em
                    (seglob/rita-ms-time)
                    (seglob/get-trial-id)
                    (seglob/get-experiment-id))]
          (def ^:dynamic *room-visited-knowledge* (conj *room-visited-knowledge* {(second vroomnames) [id em]}))
          (when (dplev :intervention :demo :all) (println "***Publishing intervention via intervention chat message: " cmsg "to MQTT:" (seglob/get-mqtt-connection)))
          (asist-msg/publish-intervention-chat-msg-mqtt cmsg  (seglob/get-mqtt-connection))
          pub)))

(defn maybe-report-room-visited
  [pub id playername whereIam neartoportal oldneartoportal at em]
  (when (not (= playername "Ed"))
    (if (> *debug-verbosity* 0)
      (if (and (global/RTobject? neartoportal) (not (= neartoportal oldneartoportal)))
        (when (dplev :all) (println "In maybe-report-room-visited neartoportal=" (global/RTobject-variable neartoportal)))))
    (if (and (global/RTobject? neartoportal)
             (not (= neartoportal oldneartoportal)))
      (let [pname (eval/deref-field ['v-name] neartoportal :normal)
            _ (when (dplev :all) (println playername "Near portal -" pname "at" at)) ; Print name
            vname (global/RTobject-variable neartoportal) ; variable name
            allothersides (get-rooms-from-portal neartoportal)
            othersides (remove (fn [r] (= r whereIam))
                               (get-unvisited-rooms-from-portal neartoportal))
            proomnames (map (fn [r] (get-object-vname r)) othersides)
            _ (if (not (empty? proomnames)) (when (dplev :all) (println "that lead(s) to -" proomnames)))
            vroomnames (map global/RTobject-variable othersides)]
        (case (count othersides)
          1 (let [pub (ritamsg/add-bs-change-message
                       pub
                       {:subject (first vroomnames)
                        :changed :state
                        :values :visited
                        :agent-belief 1.0})
                  ]
              (predict/match-prediction id :next-room-to-visit (first vroomnames))
              (when (dplev :occupancy :all) (println playername "has visited: " (first proomnames) "(" (first vroomnames) ")"))
              (set-last-room-visited (first othersides))

              ;; Maybe Predict next room
              (let [pub (predict/maybe-predict-next-room pub id playername [(first (slearn/last-visited-room (seglob/get-role playername))) (first vroomnames)])]
                ;; Update stats
                (if (and (vol/a-room? (first othersides))
                         (not (= (first vroomnames) (slearn/last-visited-room (seglob/get-role playername)))))
                  (slearn/add-room-visited (seglob/get-role playername) [(first vroomnames) (seglob/rita-ms-time)]))

                (bs/set-belief-in-variable (first vroomnames) :visited 1.0)
                ;;(maybe-intervene-with-some-encouragement pub id playername vroomnames proomnames em) this was just a test

                pub))
          2 (let [pub (ritamsg/add-bs-change-message
                       pub
                       {:subject (second vroomnames)
                        :changed :state
                        :values :visited
                        :agent-belief 1.0})
                  pub (ritamsg/add-bs-change-message
                       pub
                       {:subject (first vroomnames)
                        :changed :state
                        :values :visited
                        :agent-belief 1.0})]

              ;; Update stats
              (if (and (vol/a-room? (second othersides))
                       (not (= (second vroomnames) (slearn/last-visited-room (seglob/get-role playername)))))
                (slearn/add-room-visited (seglob/get-role playername) [(second vroomnames) (seglob/rita-ms-time)]))

              ;; Update stats
              (if (and (vol/a-room? (first othersides))
                       (not (= (first vroomnames) (slearn/last-visited-room (seglob/get-role playername)))))
                (slearn/add-room-visited (seglob/get-role playername) [(first vroomnames) (seglob/rita-ms-time)]))

              (bs/set-belief-in-variable (first vroomnames) :visited 1.0)
              (when (dplev :occupancy :all) (println playername "has visited: " (second proomnames) "(" (second vroomnames) ")"))
              (bs/set-belief-in-variable (second vroomnames) :visited 1.0)
              (when (dplev :occupancy :all) (println playername "has visited: " (first proomnames) "(" (first vroomnames) ")"))
              (set-last-room-visited (first vroomnames))

              #_(maybe-intervene-with-some-encouragement pub id playername vroomnames proomnames em)
              pub)
          (do
            (if (not (empty? allothersides)) (set-last-room-visited (first allothersides)))
            pub)))
      pub)))

(defn maybe-report-portal-proximity
  [pub id playername whereIam neartoportal oldneartoportal em]
  (or
   (if (not (= playername "Ed"))
    (if (and neartoportal (not (= neartoportal oldneartoportal)))
      (let [pname (eval/deref-field ['v-name] neartoportal :normal)]
        (if (> *debug-verbosity* 0) (when (dplev :proximity :all) (println playername "close to a portal: " pname))) ; "I am at:" whereIam
        (let [pub (ritamsg/add-bs-change-message
                   pub
                   {:subject playername
                    :changed :proximity
                    :values pname
                    :agent-belief 1.0})]
          (predict/maybe-predict-portal-use pub id playername pname whereIam neartoportal em)))))
   pub))

(defn maybe-report-switch-proximity
  [pub id playername whereIam neartoswitch oldneartoswitch]
  (or (if (and (not (= playername "Ed")) neartoswitch (not (= neartoswitch oldneartoswitch)))
        (do (if (> *debug-verbosity* 0) (when (dplev :proximity :all) (println playername "close to a switch")))
            (let [pub (ritamsg/add-bs-change-message
                       pub
                       {:subject playername
                        :changed :proximity
                        :values "switch"
                        :agent-belief 1.0})]
              (predict/maybe-predict-switch-use pub id playername whereIam neartoswitch))))
      pub))

(defn maybe-report-victim-proximity
  [pub id playername whereIam neartovictim oldneartovictim]
  (or (if (and (not (= playername "Ed")) neartovictim (not (= neartovictim oldneartovictim)))
        (do (if (> *debug-verbosity* 0) (when (dplev :proximity :all) (println playername "close to a" (victims/color-of-victim neartovictim) "victim")) (global/RTobject-variable neartovictim))
            (let [pub (ritamsg/add-bs-change-message
                       pub
                       {:subject playername
                        :changed :proximity
                        :values "victim" ; (global/RTobject-variable neartovictim)
                        :agent-belief 0.8})]
              (predict/maybe-predict-victim-triage pub id playername whereIam neartovictim))))
      pub))

(defn maybe-report-object-visibility
  [pub id playername whereIam whatIcanSee oldwhatIcanSee]
  (let [icanc (set whatIcanSee)
        icudc (set oldwhatIcanSee)
        nucanc (clojure.set/difference icanc icudc)
        nuc-names (sort (map (fn [c] (first c)) nucanc)) ]
    (or (if (and (not (= playername "Ed")) whatIcanSee (not (empty? nucanc)))
          (do (if (> *debug-verbosity* 0)
                (when (dplev :visibility :all) (println "Player" playername "may notice" (map xlate-name nuc-names))))
              (let [pub (ritamsg/add-bs-change-message
                         pub
                         {:subject playername
                          :changed :NewNoticedObjects
                          :values nuc-names
                          :agent-belief 0.5})]
                pub)))
        pub)))

(defn ensure-room-visited
  [aroom]
  (let [var (global/RTobject-variable aroom)
        visitedp (>= (bs/get-belief-in-variable var :visited) 0.8)]
    (if (not visitedp)
      (do (bs/set-belief-in-variable var :visited 1.0)
          (when (dplev :occupancy :all) (println "Visited status marked for room" var))))))

(defn maybe-replan-path
  [publish id whereIam x y z messagecounter]
  ;; Check to see if there are any plan requests
  ;; we will do one at a time to avoid taking too long
  (if (and *portals*
           (= (seglob/get-loaded-model-name) "Falcon")
           (vol/get-from-room whereIam x y z))
    (let [newplanrequest (plan/dequeue-new-plan-request-reason)]
      (cond
        ;; to enable periodic replanning set env var RITA_PERIODIC_REPLAN_FREQUENCY=500
        ;;(or some other number)
        (or newplanrequest (and (seglob/get-auto-periodic-replan-frequency) ; force a replan every n messages
                                (= (mod messagecounter (seglob/get-auto-periodic-replan-frequency)) 0)))
        (let [newplanrequest (or newplanrequest
                                 (mphyp/get-plan-hypo-id (mphyp/get-plan-hypothesis-by-rank 0)))
              hypo (mphyp/get-plan-hypothesis-by-id newplanrequest)
              apsp (and hypo (mphyp/get-plan-hypo-apsp-model hypo))
              player (first (eval/find-objects-of-name "/Main.player"))
              fromroom (vol/get-from-room whereIam x y z)
              ;;_ (when (dplev :all) (println "Planning starting from" fromroom))
              _ (vol/set-from-room "Player-Opportunistic" fromroom)
              prevbel (bs/get-belief-in-variable (global/RTobject-variable fromroom) :visited)
              _ (bs/set-belief-in-variable (global/RTobject-variable fromroom) :visited 0.0)
              _ (vol/set-unvisited-room-near-mc-choice-method :mcclose)
              _ (let [plansteps (System/getenv "RITA_TARGET_PLAN_LENGTH")
                      tpl (if (string? plansteps) (Integer. (string/trim plansteps)) 5)]
                  (vol/set-target-plan-length tpl))]
          (if (and hypo (not (empty? apsp)))
            (do (when (dplev :planning :all) (println "Replanning hypothesis" (global/prs newplanrequest)))
                (binding [seglob/*room-apsp* apsp] ;+++ really?
                  (let [_ (if (= newplanrequest "learned") (global/set-verbosity 0))
                        [pamelatext tpnjson] (pexp/generate-fresh-plan 'Main 10 200) ;+++ parameters get from elsewhere
                        _ (bs/set-belief-in-variable (global/RTobject-variable fromroom) :visited prevbel)
                        _ (when (dplev :planning :all) (println "** New plan generated for Player" newplanrequest "=" pamelatext))
                        planmsg {:hypothesis-id (mphyp/get-plan-hypo-id hypo)
                                 :hypothesis-rank (mphyp/get-plan-hypo-rank hypo)
                                 :pamela pamelatext ; Optional here for debugging only
                                 :htn {}
                                 :tpn tpnjson
                                 :current-state {}}]
                    (global/set-verbosity 0)
                    (ritamsg/add-message publish "generated-plan" :generated-plan planmsg))))
            (do
              (when (dplev :error :all) (println "ERROR: Unknown plan hypothesis ID specified for ne plan request:" newplanrequest))
              publish)))

        :otherwise publish))
    publish))

(defn get-player-id-from-participant-id
  [participantid]
  (let [foundid (or (seglob/get-player-object-name participantid)
                    (seglob/add-player participantid))
        usingid (if (not (clojure.string/includes? (str foundid) "participant"))
                  "agentBeliefState.participant1" foundid)]
    (when (and (not= foundid usingid)
               (dplev :all :error :warn))
      (println "ERROR: When looking up the name" participantid "found" foundid "using" usingid "instead")
      (println "While processing the following message:")
      (pprint (seglob/get-last-received-message)))
    usingid))


(defn get-player-position
  [id]
  [(get-field-value id 'x)
   (get-field-value id 'y)
   (get-field-value id 'z)])

(defn player-close-to-another
  [p1id closeness]
  (let [p1objname (seglob/get-player-object-name p1id)
        p1pos (get-player-position p1objname)
        players (seglob/get-player-object-names)
        otherplayers (remove #(= % p1objname) players)
        playerpos (map get-player-position otherplayers)]
    #_(when (dplev :all) (println "player-close-to-another: p1objname=" (pr-str p1objname)
             "players=" (pr-str players)
             "playerpos=" (pr-str playerpos)))
    (remove nil? (map (fn [p2 p2pos]
                        (if (< (apply ras/straight-line-distance (concat p1pos p2pos))
                               closeness)
                          p2))
                      otherplayers
                      playerpos))))

(defn update-inter-player-distances-for-pid
  [pid em]
  (let [players (seglob/get-team-members) ; PIDs
        playerobjs (map get-player-id-from-participant-id players)
        positions (map get-player-position playerobjs)
        distances (into {}
                        (let [player pid
                              playerobj (get-player-id-from-participant-id pid)
                              playerposn (get-player-position playerobj)]
                         (remove nil?
                                 (map (fn [player2 player2posn]
                                         (if (= player player2)
                                            nil
                                            {[player player2] (apply ras/straight-line-distance ; replace-with-better-measure+++
                                                                     (concat playerposn player2posn))}))
                                      players positions))))]
    (let [player pid]
          (doseq [player2 players]
            (let [dist (get distances [player player2] false)]
              (when dist
                (ts/record-distance player player2 dist em)
                (ts/record-distance player2 player dist em)))))))

(defn update-inter-player-distances
  [em]
  (doseq [player (seglob/get-team-members)]
    (update-inter-player-distances-for-pid player em)))

;;; Force current position at start of a new 10 second interval (thus at least 18 datapoints/epoch)
(def ^:dynamic *last-position-sec-time* -1)

(defn maybe-report-position-change
  [pub id pid x y z oldx oldy oldz em]
  (let [sectime (quot em 10000)]
    (or (if (not (and (= x oldx) (= y oldy) (= z oldz) (== *last-position-sec-time* sectime)))
          (do
            (if (dplev :all) (println pid "has moved to [" x y z "]"))
            (if (== *last-position-sec-time* sectime)
              (update-inter-player-distances-for-pid pid em) ; only update player that moved
              (update-inter-player-distances em))            ; snapshot position of all players
            (if (not (== *last-position-sec-time* sectime))
              (def ^:dynamic *last-position-sec-time* sectime))
            nil))                              ; We don't want to publish these
        pub)))

(defn players-close-to-each-other
  [closeness]
  (let [players (seglob/get-player-object-names)
        playerpos (map get-player-position players)]
    (if (> (count players) 1)
      (into #{}
            (concat (map
                     (fn [p1 p1pos]
                       (loop [p2 players
                              p2pos playerpos
                              close []]
                         (cond (or
                                (empty? p2)
                                (= (first p1) (first p2)))
                               (recur (rest p2) (rest p2pos) close)

                               (< (apply ras/straight-line-distance (concat p1pos p2pos))
                                  closeness)
                               (recur (rest p2)
                                      (rest p2pos)
                                      (conj close [(first p1) (first p2)]))

                               :otherwise
                               (recur (rest p2) (rest p2pos) close))))
                     players
                     playerpos)))
      [])))

(defn new-player-position
  [id tbm &[closeness]]
  (let [message (:data tbm)
        {x :x ; -2179.4057526765046
         y :y ; 28.0
         z :z ; 155.44119827835647
         motion_x :motion_x ; -0.2775688493315885
         motion_y :motion_y
         motion_z :motion_z ; -0.40585672175957577
         entity_type :entity_type ; human
         playername :name ; Player396
         yaw :yaw
         pitch :pitch
         life :life} message
        whereIam (where-am-I x z y)
        victimswhereiam (victims-in-volume whereIam)
        whatIcanSee (what-can-I-see x z y whereIam 4) ;
        oldx (or (get-field-value id 'x) 0)
        oldy (or (get-field-value id 'y) 0)
        oldz (or (get-field-value id 'z) 0)
        oldw (get-field-value id 'space) ; Where was I

        oldwhatIcanSee   (what-can-I-see oldx oldz oldy whereIam 4)
        neartoportal     (vol/close-to-a-portal x z y closeness)
        oldneartoportal  (vol/close-to-a-portal oldx oldz oldy)
        neartoswitch     (vol/close-to-a-switch x z y closeness)
        oldneartoswitch  (vol/close-to-a-switch oldx oldz oldy)
        neartovictim     (vol/close-to-a-victim x z y closeness)
        oldneartovictim  (vol/close-to-a-victim oldx oldz oldy)]
    (set-field-value! id 'x x)
    (set-field-value! id 'y y)
    (set-field-value! id 'z z)
    (set-field-value! id 'motion_x motion_x)
    (set-field-value! id 'motion_y motion_y)
    (set-field-value! id 'motion_z motion_z)
    (set-field-value! id 'pitch pitch)
    (set-field-value! id 'yaw yaw)
    (set-field-value! id 'space whereIam)

    ;(when (dplev :all) (println "****** In new-player-position: id="id "whereIam=" whereIam "whatIcanSee=" whatIcanSee "x y z=" x y z))

    {:whereIam whereIam,
     :victimswhereiam victimswhereiam,
     :whatIcanSee whatIcanSee
     :oldx oldx
     :oldy oldy
     :oldz oldz
     :oldw oldw
     :oldwhatIcanSee oldwhatIcanSee
     :neartoportal neartoportal
     :oldneartoportal oldneartoportal
     :neartoswitch neartoswitch
     :oldneartoswitch oldneartoswitch
     :neartovictim neartovictim
     :oldneartovictim oldneartovictim}))


(defn victim-type
  [victim]
  ;;; NYI
  :victim)

(defn register-selected-tool
  [playername equippeditemname]
  ;; NYI
  nil)

(defn register-door-state
  [door playername open]
  (if door
    (do
      (bs/set-belief-in-variable (global/RTobject-variable door) (if open :open :closed) 1.0)
      (ritamsg/add-bs-change-message
       []
       {:subject (second door)
        :changed :state
        :values (if open :open :closed)
        :agent-belief 1.0}))))

(defn register-lever-state
  [lever playername powered]
  (if lever
    (bs/set-belief-in-variable (global/RTobject-variable lever) (if powered :on :off) 1.0)
    (ritamsg/add-bs-change-message
     []
     {:subject :lever
      :changed :state
      :object (if powered :on :off)
      :agent-belief 1.0})))

;;;+++ very temporary hack +++


(defn register-victim-triage
  [victim playername action]
  (if victim
    (do
      (def ^:dynamic *victims-state* (merge *victims-state* {victim action}))   ;; NYI temporary hack - fix me +++
      (bs/set-belief-in-variable (global/RTobject-variable victim)
                                 (case action
                                   :triage-started
                                   :being-triaged

                                   :victim-rescued
                                   :saved

                                   (do
                                     (when (dplev :warn :all) (println "******* register-victim-triage: unknown action=" action))
                                     :awaiting-triage))
                                 1.0)))

  nil)

(def ^:dynamic *debug-interface* true)

(defn load-model-for-rita-mission
  [pamela-json-string root]
  (if *debug-interface* (when (dplev :io :all) (println "Resetting the model")))
  (rt/resetall)
  (if *debug-interface* (when (dplev :io :all) (println "Loading the model")))
  ;(try
    (do
      (load-model-from-json-string pamela-json-string root nil)
      (establish-connectivity-propositions root)
      (establish-part-of-propositions root))
    #_(catch Exception e (when (dplev :error :all) (println (str "Bad PAMELA json model: " (.getMessage e)))));)
  nil)

(defn lights-on?
  [tbversion]
  (case tbversion
    "1.00"
    true
    "1.01"
    true
    "1.02"
    true

    false))

(defn state-estimation-initialization
  [model study trialno version expt teammembers]
  (rt/resetall)                         ; Allow for sequential missions by starting from nothing.

  (when (dplev :io :all) (println "State-estimator importing RITA models"))

  ;; This is for disconnected debugging only - get message from startup in future
  (seglob/set-loaded-model-name model) ;+++ move me

  ;; +++ what do we want to use for "expt" and where do we get it from
  (seglob/set-participant-strength-file (str "../runtime-learned-models/" "expt" ".known-participants.edn"))

  (let [building-model-file (str "../data/Map/" model ".pamela.json-ir")
        building-model-apsp-pathname (str "../data/Map/" model ".apsp.edn")
        mission-file (str "../data/" "mission" ".pamela.json-ir") ; mission1.pamela.json-ir
        building-model-json (if (.exists (io/file building-model-file))
                              (slurp building-model-file)
                              (when (dplev :error :all) (println "***** MISSING FILE. The building model file:"
                                                                 building-model-file
                                                                 "is missing.  SE cannot run without this file. *****")))
        mission-model-json (if (.exists (io/file mission-file))
                             (slurp mission-file)
                             (when (dplev :error :all) (println "***** MISSING FILE.  mission model file:" mission-file
                                                                "is missing.  SE cannot run without this file. *****")))
        ;; Get participant strength data
        participant-strength-file (seglob/get-participant-strength-file)
        participant-strength (if (.exists (io/file participant-strength-file))
                               (read-string (slurp participant-strength-file)))

        ;; get training (strength) data
        training-data-file  (seglob/get-training-data-file)
        _ (seglob/reset-training-data)
        training-strength-data (if (.exists (io/file training-data-file))
                                 (read-string (slurp training-data-file)))

        learnedmodel (seglob/learned-model-path)]

    (seglob/set-building-model-apsp-pathname building-model-apsp-pathname)

    ;; Set prediction score to zero
    (seglob/set-se-predictions {})
    (seglob/set-se-successful-predictions {})

    ;; Establish possible prior knowledge of team members strengths (from trial 1)
    (when (dplev :teamstrength :all)
      (when participant-strength
        (println "Read team strength data from data from" participant-strength-file)
        (println "Contains data for participants" (keys participant-strength))
        (println "Adding team strengths for" teammembers "for trial" trialno)))
    (ts/set-participant-strength-data participant-strength teammembers trialno) ; Strengths from a prior trials (map)

    ;; Establish training data strengths (from no assistant)
    (if (dplev :teamstrength :all)
      (when training-strength-data
        (println "Read training strength data from data from" training-data-file)))
    (ts/set-training-data-data training-strength-data) ; Strengths from trainin data

    (if building-model-json
      (do
        (when (dplev :io :all) (println "Loading building model" building-model-file))
        (load-model-for-rita-mission building-model-json model)
        (vol/establish-rita-propositions)
        (vol/establish-volumes)
        (vol/we-believe-all-doors-closed 1.0)
        (vol/we-believe-all-rooms-unvisited 1.0)
        (vol/we-believe-all-victims-awaiting-triage 1.0)

        (if (lights-on? version)
          (vol/we-believe-all-lights-on 1.0)
          (vol/we-believe-all-lights-off 1.0))

        (if (or (= model "Falcon")
                ;;(= model "Saturn")
                )
          (vol/establish-paths))

        ;;(vol/we-believe-building-power (if (= model "Falcon") :no-power :electricity-on) 1.0)
        (if learnedmodel
          (do
            (when (dplev :io :all) (println "Loading learned-model from " learnedmodel))
            (slearn/load-learned-model learnedmodel))
          (do
            (when (dplev :io :all) (println "Learned-model file not specified (" learnedmodel")"))))

        (let [pf (System/getenv "RITA_PERIODIC_REPLAN_FREQUENCY")]
          (if (string? pf)
            (seglob/set-auto-periodic-replan-frequency
             (Integer/parseInt (string/trim (System/getenv "RITA_PERIODIC_REPLAN_FREQUENCY"))))))

        (if mission-model-json
          (do
            (when (dplev :io :all) (println "Loading mission model" mission-file))
            (load-model-from-json-string mission-model-json "Main" nil)
            (when (dplev :all) (bs/print-propositions))
            (when (dplev :all) (rt/describe-current-model))
            (when (dplev :io :all) (println "Model instantiated"))
            (seglob/set-loaded-model-name model) ;+++ move me
            :loaded)
          (when (dplev :error :all) (println "No mission model named" mission-file "found or loaded"))))
      (when (dplev :error :all) (println "No building model named" building-model-file "found or lmaded")))))

(defn player-triage-strategy
  [id]
  (get-field-value id 'triage-strategy))

(defn player-beep-response
  [id]
  (get-field-value id 'beep-response))

(defn seconds-remaining
  [id]
  (let [id "agentBeliefState.participant1"] ; for now participant1 is the keeper of time remaining ***+++
    (get-field-value id 'seconds-remaining)))

(defn set-player-triage-strategy!
  [id strategy]                            ;must be one of [:gold-victims-only :green-victims-only :all-victims]
  (set-field-value! id 'triage-strategy strategy))

(defn set-player-beep-response!
  [id response]
  (set-field-value! id 'beep-response response))

(defn find-player-nearest ;*** +++
  [x y z]
  {:playername "a player"
   :id "agentBeliefState.participant1"})

(defn initialize-player-condition-priors
  [id condition]
  (seglob/reset-player-performance-histories)
  (let [obj (global/get-object-from-id id)]
    (case condition
      (1 "1")                                 ; Told about cost/benefit gold vs green victims and how the device beeps are interpreted.
      (do (set-player-beep-response! id :always)
          (set-player-triage-strategy! id :gold-victims-only))

      (2 "2")
      (do (set-player-beep-response! id :never)
           (set-player-triage-strategy! id :gold-victims-only))

      (3 "3")
      (do (set-player-beep-response! id :never)
          (set-player-triage-strategy! id :all-victims))

      (when (dplev :error :all) (println "Unknown condition value" (with-out-str (prn condition)) "specified")))
    (when (dplev :all) (println "Initialized-participant" obj))))

;;; Fin

;;; blocks_in_building_2.json

;;; local testing - delete later +++
;;; (def world (import-world-from-file  "/Users/paulr/checkouts/bitbucket/asist_rita/Code/data/TB2-blocks_in_building.json"))
;;; (def world (import-world-from-file  "/Users/paulr/checkouts/bitbucket/asist_rita/Code/data/blocks_in_building_2.json.json"))
;;; (def w-dimensions (world-dimensions world))
;;; (def w-size (world-size world))
;;; (def w-types (world-object-types world))
;;; (def w-doors (get-world-objects-of-type world :wooden_door))
;;; (def doors (door-finder world))
;;; (def mca (make-minecraft-array world))
;;; (print-world world mca)
;;; (find-objects-of-interest world)
;;; (print-object-of-interest-as-pamela-constructors)
;;; (where-am-I -2190.5 167.5 28.0)
;;; (where-am-I -3000 500 100)
;;; (rt/deref-field ['v-name] (where-am-I -2190.5 167.5 28.0) :normal)
;;; (where-am-I -2168.373128255208 153.699951171875 28.0)
;;; (get-field-value "agentBeliefState.participant1" 'x)
;;; (set-field-value! "agentBeliefState.participant1" 'x 45.0)


; The current version of StateEstimation(rita_se-core.clj) has concurrency related issues such that not all testbed messages are
; processed by it. Traditional models of addressing concurrency issues using locks etc inherently have deadlock related issues. To address these
; issues, many frameworks and concepts have been developed over time but the most simplest (to me) is Concurrent Sequential Processing (CSP)[1]
; techniques.

; In the current impl, `state-estimation-main-loop` is responsible for consuming incoming messages, processing them and
; providing results for publishing to RMQ. It waits for messages via deref on a promise and provides the result via a promise.
; It is also designed to perform additional processing if necessary but in its current incarnation, it waits for one message at
; a time.

; `handle-testbed-message` is responsible for taking the message from cli.clj, handing it to `state-estimation-main-loop`
; waiting for the result to be made available by the main loop and returning back to cli.clj

; Above functionality can be implemented using 2 Queues, one for handing the message to main loop and other for receiving the results
; from the main loop.

;; [1] https://en.wikipedia.org/wiki/Communicating_sequential_processes
;;; (core/solveit :samples 1 :max-depth 100 :rawp true)
