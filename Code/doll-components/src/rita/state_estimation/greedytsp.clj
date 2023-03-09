;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.greedytsp
  "Greedy TSP"
  (:require [clojure.tools.cli :as cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [clojure.data.codec.base64 :as base64]
            [clojure.string :as string]
            [clojure.pprint :as pp :refer [pprint]]
            [me.raynes.fs :as fs]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [mbroker.rabbitmq :as rmq]
            [clojure.java.shell :as shell]
            [clojure.data.xml :as xml]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [random-seed.core :refer :all]
            [rita.common.core :as rc :refer :all]
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            [rita.state-estimation.ras :as ras]
            [rita.state-estimation.spacepredicates :as spreads]
            [rita.state-estimation.victims :as victims]
            [rita.state-estimation.multhyp :as mphyp]
            [rita.state-estimation.planning :as plan]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
            [pamela.tools.belief-state-planner.montecarloplanner :as bs]
            [pamela.tools.belief-state-planner.ir-extraction :as irx]
            [pamela.tools.belief-state-planner.coredata :as global]
            [pamela.tools.belief-state-planner.evaluation :as eval]
            [pamela.tools.belief-state-planner.lvarimpl :as lvar]
            [pamela.tools.belief-state-planner.prop :as prop]
            [pamela.tools.belief-state-planner.imagine :as imag]
            [pamela.tools.belief-state-planner.vprops :as vp]
            [pamela.tools.belief-state-planner.dmcgpcore :as core])
  (:refer-clojure :exclude [rand rand-int rand-nth shuffle]) ; because of random-seed.core
  (:gen-class)) ;; required for uberjar

#_(in-ns 'rita.state-estimation.greedytsp)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Greedy TSP

(defn visited-room?
  [visited aroom]
  (not (empty? (clojure.set/intersection visited #{aroom}))))


(defn filter-rooms-visited
  [visited doors]
  ;;; UNFINISHED
  )

(defn greedy-tsp-plan
  [maps]
  (let [{namemap :namemap               ; Map from portal variable name to object name
         pcm     :pcm                   ; What portals are attainable in a single hop
         apsp    :apsp} maps            ; Indirect map can we get there in multiple hops?
        all-roomes *room-objects*
        starting-in (get-rtobject-called "Mission Room" *room-objects*) ;+++ get these mission specific
        target-door (get-rtobject-called "Mission Room Door" *portals*) ;+++ names out of the code!
        rooms-visited #{starting-in}
        plan-steps nil]

    ;; Loop starts when we are in a room and we are ready to leave it.
    (loop [inroom starting-in
           visited rooms-visited
           plan plan-steps]
      (when (dplev :planner :all)
        (println "**Planning step: in room:" (get-object-vname inroom))
        (println "visited:" visited)
        (println "plan:" plan))
      (let [doors (get-doors-from-room inroom)
            chosen-door (choose-best-door doors visited)
            ;; coordinates (get-coordinates-of-object chosen-door)
            door-leads-to (get-other-side-of chosen-door inroom)] ;; OK that's where we will be
        (if (a-room? door-leads-to)
          ;; We have gone from one room to another recurse process the room, if we haven't already
          ;; then leave it.  This is the case where one room has an inner door to another. On the
          ;; way out we necessarily go through the room that we processed on the way in. We don't
          ;; process it again.
          (recur door-leads-to
                 (clojure.set/union visited #{door-leads-to})
                 (concat plan
                         [[:goto-door chosen-door]
                          [:enter-into (get-object-vname door-leads-to)]]
                         (if (not (visited-room? visited door-leads-to))
                           [[:process-room (get-object-vname door-leads-to)]])))

          ;; Otherwise we must be in some connecting space, a corridor This is the normal case.
          ;; We must pick another room to go to, go to it, inter it, and process it before recursing.
          (let [candidate-doors (get-doors-from-door chosen-door door-leads-to maps) ; where can we go?
                good-candidate-doors (filter-rooms-visited visited candidate-doors)] ; no redo's
            (if (empty? good-candidate-doors)

              ;; We have been everywhere. find the shortest route back to the start
              ;; This is the end of the mission
              (let [path-home (find-path-from-to chosen-door target-door maps)
                    fullplan
                    (concat plan [[:enter-into (get-object-vname door-leads-to)]] path-home)]
                (when (dplev :planner :all)
                  (println "**Planning finished")
                  (println "Full plan:")
                  (pprint fullplan))
                fullplan)

              ;; Otherwise greedy find next room and recurse.  Pick the closest candidate door.
              (let [nearest-candidate-door (first (sort good-candidate-doors))
                    path-to-next-door (find-path-from-to chosen-door nearest-candidate-door maps)
                    target-door-leads-to (get-other-side-of nearest-candidate-door door-leads-to)]
                (recur target-door-leads-to
                       (clojure.set/union visited #{target-door-leads-to})
                       (concat plan
                               [[:goto-door chosen-door]
                                [:enter-into (get-object-vname door-leads-to)]]
                               path-to-next-door
                               [[:enter-into (get-object-vname target-door-leads-to)]
                                [:process-room (get-object-vname door-leads-to)]]))))))))))
