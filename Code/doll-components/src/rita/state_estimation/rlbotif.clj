;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.rlbotif
  "RITA RL Bot Interface."
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
            [rita.state-estimation.rmq-connection :as rmq-plant]
            [clojure.java.shell :as shell]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [rita.common.core :as rc :refer :all]
            [rita.state-estimation.secoredata :as seglob :refer [dplev  dont-repeat]]
            [rita.state-estimation.spacepredicates :as spreads]
            [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
            [pamela.tools.belief-state-planner.montecarloplanner :as bs]
            [pamela.tools.belief-state-planner.ir-extraction :as irx]
            [pamela.tools.belief-state-planner.coredata :as global]
            [pamela.tools.utils.util :as pt-util]
            ;[rita.state-estimation.import-minecraft-world :as imw]
            [rita.state-estimation.rita-se-core :as rsc]
            [rita.state-estimation.statlearn :as slearn]
            [rita.state-estimation.volumes :as vol]
            [rita.state-estimation.observations :as obs]
            [clojure.java.io :as io]
            [nano-id.core :refer [nano-id]]
            [cheshire.core :refer :all])

  (:gen-class)) ;; required for uberjar

#_(in-ns 'rita.state-estimation.rlbotif)
(def ^:dynamic *debug-verbosity* 0)
(def ^:dynamic *visualization-enabled* false)
(def ^:dynamic default-publisher-routing-key "observations")
(def ^:dynamic *rlbot-thread* nil)
(def ^:dynamic *rlbot-finished* false)
(def ^:dynamic *robots* {})             ; Allow for multiple robots in multiple threads

(def direction [1 0 -1 0])
(def position [-2105 148])
(def robot-walking-speed 0.5)          ; 1.5 would be a better human walking speed.  Replace with learned average
(def robot-y-value 61)                 ; These constants need to come from the initialization


;;; Temporary manually generated coordinates from tghe Falcon map - will write code to automatically generate this.
(defn rotate
  "Take a sequence and left rotates it n steps. If n is negative,
  the sequence is rotated right. Executes in O(n) time."
  [n aseq]
  (let [c (count aseq)]
    (take c (drop (mod n c) (cycle aseq)))))

(defn rotate-while
  "Rotates a sequence left while (pred item) is true. Will return an
  unrotated sequence if (pred item) is never true. Executes in O(n) time."
  [pred aseq]
  (let [head (drop-while pred aseq)]
    (take (count aseq) (concat head aseq))))

(defn pnz
  "Determines whether or not n is positive, negative, or zero"
  [n]
  (cond
    (> n 0) 1
    (< n 0) -1
    :else 0))

(defrecord RLROBOT [robotid position_a direction_a pathways_a])

;;; +++ pathways will be automatically computed from the map +++
(def pathways [;; Entrance
               [[148 -2105] [148 -2097]]
               [[148 -2095] [148 -2093]]
               [[148 -2093] [154 -2093]]
               [[148 -2097] [148 -2095]] ; [Door]

               ;; North Corridor
               [[154 -2093] [154 -2087]]
               [[154 -2087] [154 -2080]]
               [[154 -2080] [154 -2075]]
               [[154 -2075] [154 -2071]]
               [[154 -2071] [154 -2058]]
               [[154 -2058] [154 -2054]]
               [[154 -2054] [154 -2042]]
               [[154 -2042] [154 -2039]]
               [[154 -2087] [152 -2087]] ; [Goto door]
               [[152 -2087] [150 -2087]] ; [Door]
               [[154 -2087] [155 -2087]] ; [Goto door]
               [[155 -2087] [157 -2087]] ; [Door]
               [[154 -2080] [152 -2080]] ; [Goto door]
               [[152 -2080] [150 -2080]] ; [Door]
               [[154 -2075] [152 -2075]] ; [Goto door]
               [[152 -2075] [150 -2075]] ; [Door]
               [[154 -2071] [155 -2071]] ; [Goto door]
               [[155 -2071] [157 -2071]] ; [Door]
               [[154 -2058] [152 -2058]] ; [Goto door]
               [[152 -2058] [150 -2058]] ; [Door]
               [[154 -2042] [152 -2042]] ; [Goto door]
               [[152 -2042] [150 -2042]] ; [Door]

               ;; West Corridor
               [[154 -2075] [161 -2075]]
               [[161 -2075] [170 -2075]]
               [[170 -2075] [181 -2075]]
               [[161 -2075] [161 -2074]] ; [Goto door]
               [[161 -2074] [161 -2072]] ; [Door]
               [[170 -2075] [170 -2074]] ; [Goto door]
               [[170 -2074] [170 -2072]] ; [Door]

               ;; Center Corridor
               [[154 -2054] [165 -2054]]
               [[165 -2054] [168 -2054]]
               [[168 -2054] [175 -2054]]
               [[175 -2054] [181 -2054]]
               [[165 -2054] [165 -2055]] ; [Goto door]
               [[165 -2055] [165 -2057]] ; [Door]
               [[168 -2054] [168 -2055]] ; [Goto door]
               [[168 -2055] [168 -2057]] ; [Door]
               [[175 -2054] [175 -2052]] ; [Goto door]
               [[175 -2052] [175 -2050]] ; [Door]

               ;; East Corridor
               [[154 -2039] [158 -2039]]
               [[158 -2039] [168 -2039]]
               [[168 -2039] [181 -2039]]
               [[158 -2039] [158 -2041]] ; [Goto door]
               [[158 -2041] [158 -2043]] ; [Door]
               [[168 -2039] [168 -2038]] ; [Goto door]
               [[168 -2038] [168 -2036]] ; [Door]

               ;; South Corridor
               [[181 -2108] [181 -2099]]
               [[181 -2099] [181 -2090]]
               [[181 -2090] [181 -2087]]
               [[181 -2087] [181 -2081]]
               [[181 -2081] [181 -2075]]
               [[181 -2075] [181 -2072]]
               [[181 -2072] [181 -2063]]
               [[181 -2063] [181 -2054]]
               [[181 -2054] [181 -2045]]
               [[181 -2045] [181 -2039]]
               [[181 -2039] [181 -2036]]
               [[181 -2108] [182 -2108]] ; [Goto door]
               [[182 -2108] [184 -2108]] ; [Door]
               [[181 -2099] [182 -2099]] ; [Goto door]
               [[182 -2099] [184 -2099]] ; [Door]
               [[181 -2090] [182 -2090]] ; [Goto door]
               [[182 -2090] [184 -2090]] ; [Door]
               [[181 -2087] [179 -2087]] ; [Goto door]
               [[179 -2087] [177 -2087]] ; [Door]
               [[181 -2081] [182 -2081]] ; [Goto door]
               [[182 -2081] [184 -2081]] ; [Door]
               [[181 -2072] [182 -2072]] ; [Goto door]
               [[182 -2072] [184 -2072]] ; [Door]
               [[181 -2063] [182 -2063]] ; [Goto door]
               [[182 -2063] [184 -2063]] ; [Door]
               [[181 -2054] [182 -2054]] ; [Goto door]
               [[182 -2054] [184 -2054]] ; [Door]
               [[181 -2045] [182 -2045]] ; [Goto door]
               [[182 -2045] [184 -2045]] ; [Door]
               [[181 -2036] [182 -2036]] ; [Goto door]
               [[182 -2036] [184 -2036]] ; [Door]
               [[181 -2036] [179 -2036]] ; [Goto door]
               [[179 -2036] [177 -2036]] ; (Door]
               ])

;; (defn mapmap-from-segmentsd
;;   [pathways]
;;   (let [mapmaps  (into [] (map (fn [[[fz fx] [tz tx]]]
;;                                  (println "fz,fx, tz,tx=" fz fx tz tx)
;;                                  (let [from [fx fz]
;;                                        to [tx tz]]
;;                                    (println "from, to=" from to)
;;                                    {from to, to from}))
;;                                pathways))]
;;     mapmaps))

(defn mapmap-from-segments
  [pathways]
  (let [mapmaps (apply merge-with into (map (fn [[[fz fx] [tz tx]]]
                                              (let [from [fx fz]
                                                    to [tx tz]]
                                                {from [to], to [from]}))
                                            pathways))]
    mapmaps))

(def themap (mapmap-from-segments pathways))

(defn get-lrbot
  [id]
  (get *robots* id))

(defn direction-from-coords
  [pos dir next]
  (if (> *debug-verbosity* 2) (println "pos=" pos "dir=" dir "next=" next))
  (let [[nx nz] next
        [dx dz _ _] dir
        [px pz] pos
        cx (pnz (- nx px))
        cz (pnz (- nz pz))]
    (let [result (cond (or (and (> dx 0) (> cx 0))
                           (and (< dx 0) (< cx 0))
                           (and (> dz 0) (> cz 0))
                           (and (< dz 0) (< cz 0)))
                       :forward

                       (or (and (< dx 0) (> cx 0))
                           (and (> dx 0) (< cx 0))
                           (and (< dz 0) (> cz 0))
                           (and (> dz 0) (< cz 0)))
                       :backward

                       (or (and (> dx 0) (> cz 0))
                           (and (< dx 0) (< cz 0))
                           (and (< dz 0) (> cx 0))
                           (and (> dz 0) (< cx 0)))
                       :right

                       (or (and (< dx 0) (> cz 0))
                           (and (> dx 0) (< cz 0))
                           (and (> dz 0) (> cx 0))
                           (and (< dz 0) (< cx 0)))
                       :left

                       :otherwise (do
                                    (if (> *debug-verbosity* 3)
                                      (println "Illegal move: pos, dir, next=" pos dir next))
                                    :illegal))]
      ;;(println "px, pz, nx, nz, cx, cz, dx, dz, result [" px pz "->" nx nz "] delta" cx cz "direction" dx dz result)
      result)))

(defn get-room-visited-state
  [id whereIam neartoportal]
  (if (not (spreads/is-a-door neartoportal))
    false
    (let [oso (vol/get-other-side-of neartoportal whereIam)]
      (cond
        (or (not oso) (not (vol/a-room? oso)))
        false

        (< (bs/get-belief-in-variable (global/RTobject-variable oso) :visited) 0.2)
        false

        :otherwise true))))

;;; (+ (* 16 (+ (* 16 (encode-switch neartoswitch new-pos-x new-pos-z)) ; 0 means close enough to act, 1 means light is "on"
;;;            (encode-victim new-pos-x new-pos-z)))       ; 0 means close enough to act, 0 means door is "open"


;;; <direction 2 bits, 01=right, 10=left, 11=forward, 00=don't see>
;;; <distance 1 bit, 0= close enough to open/close door, 1= not close enough to open door
;;; <room state behind door 1 bit 1=visited 0=unvisited>
;;; <door state 1 bit, 1=closed, 0=open>

;; -----------------------------------------
;; -------- Supporting Functions  ----------
;; -----------------------------------------
(defn unencode-door
  [dval]
  (let [direction (bit-and 0x3 (bit-shift-right dval 3))
        distance (bit-and 0x1 (bit-shift-right dval 2))
        roomstate (bit-and 0x1 (bit-shift-right dval 1))
        door (bit-and 0x1 dval)
        dirstr (case direction 0 "No door seen" 1 "right," 2 "left," 3 "ahead," "?")
        diststr (case distance 0 "close enough to touch" 1 "in sight" "?")
        roomstr (case roomstate 0 "unvisited" 1 "visited" "?")
        doorstr (case door 0 "open" 1 "closed" "?")]
    (if (> direction 0)
      (str "a(n)" doorstr "Door seen to a(n)" roomstr "room," dirstr diststr)
      "No door seen")))

(defn encode-door
  [id neartoportal rlbotdir whereIam new-pos-x new-pos-z]
  (if neartoportal
    (let [{dxl 'tl-x
           dyl 'tl-y
           dzl 'tl-z
           dxr 'br-x
           dyr 'br-y
           dzr 'br-z} (deref (:fields neartoportal))
          dx (Math/abs (- @dxl new-pos-x))
          dz (Math/abs (- @dyl new-pos-z))
          dist (Math/sqrt (+ (* dx dx) (* dz dz)))
          distance (if (<= dist 2) 0 4)   ; Third bit set if not close enough to triage
          _ (if (> *debug-verbosity* 1)
              (println "dxl=" @dxl "dyl=" @dyl "at x z (" new-pos-x new-pos-z ")"))
          direction (case (direction-from-coords [new-pos-x new-pos-z] @rlbotdir [@dxl @dyl])
                      :left (* 8 2) :forward (* 8 3) :backward (* 8 0) :right (* 8 1) 0)
          portal-var (global/RTobject-variable neartoportal)
          bel-closed-door (bs/get-belief-in-variable portal-var :closed)
          roomstate (if (and whereIam (get-room-visited-state id whereIam neartoportal)) 2 0)
          doorstate (if (> bel-closed-door 0.8) 1 0)]
      (+ direction distance roomstate doorstate))
    0))

;;; Reward and costs
(def enter-unvisited-room-reward 0.1)
(def cost-of-acting -0.1)           ; Perhaps we should scale this to account for distance
(def impossible-move-penalty -0.1)  ; Penalty for attempting a move that is not available

;; This function will return the observation format
(defn set-robot-position
  [id posn penalty]
  (let [rlbot (get *robots* id)]
  ;; If not rlbot, simply print out a message
  ;; Should we restart or take some resetting actions here?
    (if (not rlbot)
      (println "RLBOT " id "not found.")

      ;; if rlbot
      (let [posatom (.position_a rlbot)
            [new-pos-x new-pos-z] posn
            rlbotdir (.direction_a rlbot)

            ;; Setting up the testbed message for RITA UI
            message {:x           (first posn)
                     :y           60.0,             ;Value for Falcon +++
                     :z           (second posn)
                     :motion_x    0
                     :motion_y    0
                     :motion_z    0
                     :extity_type :robot
                     :playername  id
                     :yaw         0
                     :pitch       0
                     :life        10}
            tbm {:routing-key "observations.ui"
                 :data   message
                 :msg    {:trial_id      "rltraining"
                          :experiment_id "rltraining"
                          :sub_type      "state"
                          :source        "dmrl"
                          :version       "1.10"
                          :timestamp     "2020-06-12T00:04:28.893Z"} ;+++ put in a reasonable timestamp
                 :header {:message_type "observation"
                          :version      "1.10"
                          :timestamp    "2020-06-12T00:04:28.893Z"}} ;+++ put in a reasonable timestamp

            ;; Setting up data for calculation
            {whereIam        :whereIam
             victimswhereiam :victimswhereiam
             whatIcanSee     :whatIcanSee
             oldx            :oldx
             oldy            :oldy
             oldz            :oldz
             oldw            :oldw
             oldwhatIcanSee  :oldwhatIcanSee
             neartoportal    :neartoportal
             oldneartoportal :oldneartoportal
             neartoswitch    :neartoswitch
             oldneartoswitch :oldneartoswitch
             neartovictim    :neartovictim
             oldneartovictim :oldneartovictim} (rsc/new-player-position id tbm 4)]


          ;; --- Thao: What is the purpose of this piece of code?


        (when *visualization-enabled*
          (let [[old-pos-x old-pos-z] @posatom
                [new-pos-x new-pos-z] posn
                diff-x (- new-pos-x old-pos-x)
                diff-z (- new-pos-z old-pos-z)
                dist (Math/sqrt (+ (* diff-x diff-x) (* diff-z diff-z)))
                timetoarrive (/ dist (* 1000.0 robot-walking-speed))]; Time in milliseconds to arrive at destination
            (Thread/sleep timetoarrive)))

        (reset! posatom posn)

        ;; Get OBSERVATION data (returnval) for RL
        ;;  {:x <xvalue>, :z <zvalue>, :vision <a 14 bit integer as follows, in order, switch, victim, portal
        (let [[new-x new-z] posn
              vision (encode-door id neartoportal rlbotdir whereIam new-x new-z)
              reward (+ cost-of-acting  ; cost of acting
                        penalty
                        (if (and (vol/a-room? whereIam)    ; I am in a room
                                 (not (= oldw whereIam))   ; I wasn't here before (just entered)
                                 (= 0 (bit-and vision 2))) ;  2=bit position for visited
                          (do (bs/set-belief-in-variable (global/RTobject-variable whereIam) :unvisited 0.0)
                              (if (> *debug-verbosity* 1)
                                (println "Entered an unvisited room gets a reward of" enter-unvisited-room-reward))
                              enter-unvisited-room-reward)
                          0))

              returnval {:x new-x         ; Return x but we are not using it yet in RL
                         :z new-z         ; Return z but we are not using it yet in RL
                         :vision vision
                         :reward reward}]

          (if (> *debug-verbosity* 1)
            (println (unencode-door vision) "reward=" reward))

          ;; Return only the observation if render = false
          ;; Return both the observation and testbed message if render = true
          (if *visualization-enabled*
            [returnval tbm]
            returnval))))))


(defn get-robot-position
  [id]
  (let [rlbot (get *robots* id)]
    (if (not rlbot)
      (println "RLBOT " id "not found.")
      @(.position_a rlbot))))

(defn set-robot-direction
  [id dirn]
  (let [rlbot (get *robots* id)]
    (if (not rlbot)
      (println "RLBOT " id "not found.")
      (let [posatom (.direction_a rlbot)]
        (reset! posatom dirn)))))

(defn get-robot-pathways
  [id]
  (let [rlbot (get *robots* id)]
    (if (not rlbot)
      (println "RLBOT " id "not found.")
      @(.pathways_a rlbot))))

(defn get-robot-direction
  [id]
  (let [rlbot (get *robots* id)]
    (if (not rlbot)
      (println "RLBOT " id "not found.")
      @(.direction_a rlbot))))

(defn get-movement-options
  [id]
  (let [pos (get-robot-position id)
        dir (get-robot-direction id)
        rmap (get-robot-pathways id)
        reachable (get rmap pos)]
    (into {}
          (map (fn [next]
                 {(direction-from-coords pos dir next) next})
               reachable))))


;; ----------------------------------
;; -------- ROBOT ACTIONS  ----------
;; ----------------------------------
(defn turn-right
  [id]
  (set-robot-direction id (rotate -1 (get-robot-direction id))))

(defn turn-left
  [id]
  (set-robot-direction id (rotate 1 (get-robot-direction id))))

(defn turn-left-advance ; function 0
  [id]
  (let [possible? (get (get-movement-options id) :left)
        observation
        (if (not possible?)
          (do (let [obs (set-robot-position id (get-robot-position id) impossible-move-penalty)]
                (if (> *debug-verbosity* 3)
                  (println "Impossible left turn attempted"))
                obs))
          (do (turn-left id)
              (set-robot-position id possible? 0.0)))]
    [(get-robot-position id) (map first (get-movement-options id)) observation]))

(defn turn-right-advance ;function 1
  [id]
  (let [possible? (get (get-movement-options id) :right)
        observation
        (if (not possible?)
          (do (let [obs (set-robot-position id (get-robot-position id) impossible-move-penalty)]
                (if (> *debug-verbosity* 3)
                  (println "Impossible right turn attempted"))
                obs))
          (do (turn-right id)
              (set-robot-position id possible? 0.0)))]
    [(get-robot-position id) (map first (get-movement-options id)) observation]))

(defn turn-around-advance ; function 2
  [id]
  (let [possible? (get (get-movement-options id) :backward)
        observation
        (if (not possible?)
          (do (let [obs (set-robot-position id (get-robot-position id) impossible-move-penalty)]
                (if (> *debug-verbosity* 3)
                  (println "Impossible about turn attempted"))
                obs))
          (do (turn-left id)
              (turn-left id)
              (set-robot-position id possible? 0.0)))]
    [(get-robot-position id) (map first (get-movement-options id)) observation]))

(defn advance ; function 3
  [id]
  (let [possible? (get (get-movement-options id) :forward)
        observation
        (if (not possible?)
          (do (let [obs (set-robot-position id (get-robot-position id) impossible-move-penalty)]
                (if (> *debug-verbosity* 3)
                  (println "Impossible advance turn attempted"))
                obs))
          (do (set-robot-position id possible? 0.0)))]

    [(get-robot-position id) (map first (get-movement-options id)) observation]))

(defn initialize-mission
  []
  (obs/start-mission {:mission-state "START"
                      :version "1.00"
                      :trial-id "foo"
                      :experiment-id "bar"
                      :mission "Falcon"}))

;;;(get-movement-options  "agentBeliefState.robot1")
;;;(get-robot-position  "agentBeliefState.robot1")
;;;(get-robot-direction  "agentBeliefState.robot1")
;;;(pprint (get-robot-pathways  "agentBeliefState.robot1"))
;;; (test-loop 20)

;; (defn goto-door
;;   [id]
;;   (println "goto door"))

;; (defn open-door
;;   [id]
;;   (println "open door"))

;; (defn triage-victim
;;   [id]
;;   (pritnln "triaging victim"))

;; (defn open-door
;;   [id]
;;   (println "opening-door"))

;; (defn close-door
;;   [id]
;;   (println "closing door"))

(defn initialize-rlbot-if
  [id]
  (let [new-robot (RLROBOT. id (atom position) (atom direction) (atom themap))]
    (def ^:dynamic *robots* (merge *robots* {id new-robot}))
    (set-robot-position id position 0.0)))  ; Already at this position, but move here explicitly for the visualization.

;; --------------------------------------------------------------------------------
;; -------- Handles incoming messages & distributes task to task-manager ----------
;; --------------------------------------------------------------------------------
(defn currentTime
  "Returns the time in milliseconds since January 1, 1970"
  []
  (.getTime (new java.util.Date)))

(defn task-manager
  [ch exchange original-msg confirm-msg to-do]
  (println (str "Working on task. Please wait ... " (:function-name original-msg)))
  (println "Current-thread:" (pamela.tools.utils.util/getCurrentThreadName))

  ;; Do the task & send back results
  (try
    (do (let [output-msg
              ;; if the function-name is perform
              (if (= (:function-name original-msg) "perform")
                ;; result format:
                ;; If render = false: [[-2105 148] (:forward) {:x -2105, :z 148, :vision 0, :reward -0.2}]
                ;; If render = true: [[-2105 148] (:forward) [{:x -2105, :z 148, :vision 0, :reward -0.2} {tbm}]
                (do
                  (let [result (to-do)
                        obs-and-tbm (get result 2)]
                    (if *visualization-enabled*
                      (do
                        (def obs (get obs-and-tbm 0))
                        (def render-msg (get obs-and-tbm 1))
                        ;; Publish testbed-like message to RITA UI for rendering.... publish key = observations.ui
                        (rmq-plant/publishJSON ch exchange (str default-publisher-routing-key ".ui") render-msg))

                      (def obs obs-and-tbm))

                    (def vision (:vision obs))
                    (def reward (:reward obs))
                    (def output {:plant-id         (:plant-id original-msg)
                                 :id                (:id original-msg)
                                 :state            "finished"
                                 :function-name    (:function-name original-msg)
                                 :args             (:args original-msg)
                                 :timestamp        (currentTime)
                                 :status           "success"
                                 :observations     [{:field "state0"
                                                     :value vision}
                                                    {:field "reward"
                                                     :value reward}]})
                    output))

                ;; if the function-name is one of the following: reset, shutdown, render
                (do
                  (to-do)
                  (def output {:plant-id          (:plant-id original-msg)
                               :id                (:id original-msg)
                               :state             "finished"
                               :function-name     (:function-name original-msg)
                               :timestamp          (currentTime)
                               :status             "success"})
                  output))]
          (rmq-plant/publishJSON ch exchange default-publisher-routing-key output-msg)
          (println (str "Finished " (:function-name original-msg) " " (:args original-msg)))))
    (catch Exception e
      (.printStackTrace e)
      (str "Caught exception: " (.getMessage e))
      (rmq-plant/publishJSON ch exchange default-publisher-routing-key
                             {:plant-id          (:plant-id original-msg)
                              :id                (:id original-msg)
                              :state             "finished"
                              :function-name     (:function-name original-msg)
                              :timestamp          (currentTime)
                              :status             "failed"}))))

(defn enable-visualization
  [tv]
  (def ^:dynamic *visualization-enabled* tv))

(defn messageHandler
  [ch {:keys [ontent-type exchange] :as meta} ^bytes payload]
  ;; (println "Received message metadata" meta) for more details

  (let [json-msg        (String. payload "UTF-8")           ;; rmq message (json format)
        clojure-msg     (parse-string json-msg true)        ;; convert rmq message to clojure format
        plant-id        (:plant-id clojure-msg)
        fn-name         (:function-name clojure-msg)        ;; function-name includes: start, reset, shutdown, perform, render
        args            (:args clojure-msg)
        good-msg        {:plant-id          plant-id
                         :status            "success"
                         :state             "started"
                         :function-name     fn-name
                         :args              args
                         :timestamp         (currentTime)}
        bad-msg         {:plant-id            plant-id
                         :status              "fail"
                         :state               "ERROR: undefinded command"
                         :function-name       fn-name
                         :args                args ;; args currently only applies to perform: 0, 1, 2, 3
                         :timestamp           (currentTime)}]
    (case fn-name
      "start"  (task-manager ch exchange clojure-msg good-msg
                             (fn []
                               (initialize-mission)
                               (Thread/sleep 6000)
                               (initialize-rlbot-if  "agentBeliefState.robot1")))
      "reset"  (task-manager ch exchange clojure-msg good-msg
                             (fn []
                               (def ^:dynamic *rlbot-finished* true)
                               (Thread/sleep 10000)
                               (initialize-rlbot-if  "agentBeliefState.robot1")
                               (enable-visualization false)))
      "render" (task-manager ch exchange clojure-msg good-msg
                             (fn []
                               (enable-visualization true)
                               ;; publish a "start-trial" message to rita ui
                               (rmq-plant/publishJSON ch exchange (str default-publisher-routing-key ".ui") {:routing-key "observations.ui"
                                                                                                             :header {:message_type "event"}
                                                                                                             :msg {:sub_type "Event:startRender"}
                                                                                                             :data {:mission_state "Start"
                                                                                                                    :mission "Falcon"}})))
      "perform" (case args
                  0 (task-manager ch exchange clojure-msg good-msg
                                  (fn []  (turn-left-advance  "agentBeliefState.robot1")))

                  1 (task-manager ch exchange clojure-msg good-msg
                                  (fn []  (turn-right-advance  "agentBeliefState.robot1")))

                  2 (task-manager ch exchange clojure-msg good-msg
                                  (fn []  (turn-around-advance  "agentBeliefState.robot1")))

                  3 (task-manager ch exchange clojure-msg good-msg
                                  (fn []  (advance  "agentBeliefState.robot1"))))
      "shutdown" (task-manager ch exchange clojure-msg good-msg ;; not implemented yet
                               (fn []
                                 (def ^:dynamic *rlbot-finished* true)
                                 (Thread/sleep 10000)))
      (rmq-plant/publishJSON ch exchange default-publisher-routing-key bad-msg))))

;; -----------------------------------------------------------
;; --------- Internal testesting/debugging function ----------
;; -----------------------------------------------------------
(defn test-loop
  [n]
  (loop [i n
         visited #{(get-robot-position  "agentBeliefState.robot1")}]
    (let [options (into [] (get-movement-options  "agentBeliefState.robot1"))
          choiceset (remove (fn [x] (visited (second x))) options)
          choiceset (if (empty? choiceset) options choiceset)
          numoptions (count choiceset)]
      #_(println "i=" i "visited=" visited "numoptions=" numoptions "Robot position=" (get-robot-position  "agentBeliefState.robot1")
                 "Robot direction=" (get-robot-direction  "agentBeliefState.robot1")
                 "options" options)
      (when (not (or (<= i 0) (= numoptions 0)))
        (let [selected-move (rand-nth choiceset)]
          (println "Step (" (+ 1 (- n i)) ") select-option:" selected-move)
          (case (first selected-move)
            :forward (advance  "agentBeliefState.robot1")
            :backward (turn-around-advance  "agentBeliefState.robot1")
            :left (turn-left-advance  "agentBeliefState.robot1")
            :right (turn-right-advance  "agentBeliefState.robot1")
            :illegal (println "Bad move")
            (println "Error unknown case" (first selected-move)))
          (recur (- i 1) (conj visited (get-robot-position  "agentBeliefState.robot1"))))))))

(defn robot-learner-main-loop
  []
  (initialize-mission)
  (Thread/sleep 6000)
  (initialize-rlbot-if  "agentBeliefState.robot1")
  (while (not *rlbot-finished*)
    (test-loop 50)                      ; place holder +++ remove
    (def ^:dynamic *rlbot-finished* true) ; place holder +++ remove
    (Thread/sleep 1000)))


;; -----------------------------------------------------
;; ---------     ...WHERE IT ALL STARTS...    ----------
;; -----------------------------------------------------
(defn start-rlbot-thread
  []
  (let [th (Thread. (fn []
                      ;; Open RMQ connection (this is an object, check rmq-plant for more information)
                      (def rmq-instance (rmq-plant/createRMQ))
                      ;; Print the essential RMQ info (host, port, routing, binding)
                      ((:printMyself rmq-instance) rmq-instance)
                      ;; Subscribe to consumber binding keys: rita.* including rita.ui and rita.rl
                      ((:subscribe rmq-instance) (:channel rmq-instance) (:exchange-name rmq-instance) "" (:c-binding rmq-instance) messageHandler)

                      (println "start-rlbot-thread -- Done " (pamela.tools.utils.util/getCurrentThreadName)))
                    "RLBOT-Interface-Thread")]
    (def ^:dynamic *rlbot-thread* th)
    (.start th)))

;;; Fin
