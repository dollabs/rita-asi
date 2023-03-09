;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.spacepredicates
  "Space predicates."
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
            [rita.state-estimation.secoredata :as seglob :refer [dplev dont-repeat]]
            [rita.state-estimation.ras :as ras]
            [rita.state-estimation.testbed :as tb]
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
;;; space predicates

;;; +++ Use inheritance here and below for corridors
(defn in-a-room?
  [x]
  ;;(when (dplev :all) (println "in-a-room? " x))
  (if (global/RTobject? x)
    (some #{'FiveDoorRoom 'FourDoorRoom 'ThreeDoorRoom 'TwoDoorRoom 'Room
            'Room1Conn 'Room2Conn 'Room3Conn 'Room4Conn 'Room8Conn
            'Room9Conn 'Room14Conn 'Room7Conn 'Room5Conn 'Room11Conn}
          [(global/RTobject-type x)])))

(defn in-a-corridor?
  [x]
  (if (global/RTobject? x) ;+++ Should do this automatically based on class inheritance.
    (some #{;; TB2 Model
            'RightCorridorWE 'RightCorridorNS 'LeftCorridorWE 'LeftCorridorSN ;'CenterCorridor
            ;; Sparky model
            'NorthCorridorWE 'NorthCorridocSN'SouthCorridocWE 'SouthCorridorNS 'MainCorridor
            ;; Falcon Model
            'NorthCorridor 'WestCorridor 'CenterCorridor 'EastCorridor 'SouthCorridor
            ;; Saturn model
            'ConferenceCWest_CCW 'MainCWest_MCW 'ConferenceCNorth_CCN 'ConferenceRoomC_CRC 'RestroomC_RRC
            'StorageCNorth_SCN_1 'StorageCNorth_SCN_2 'StorageCNorth_SCN_3 'StorageCNorth_SCN_4
            'ConferenceCEast_CCE 'LimpingLambCNorth_LLCN 'MainCEast_MCE 'StorageCWest_SCW
            'EL_1 'EL_2 'EL_3 'EW 'LLC_1 'LLC_2 'LLC_3 'LLC_4 'LLC_5 'LLC_6 'LLC_7 'LLC_8
            'LOC_15 'LOC_16 'LOC_17 'LOC_18_1 'LOC_18_2 'LOC_18_3 'LOC_19_1 'LOC_19_2 'LOC_19_3 'LOC_20_1
            'LOC_20_2 'LOC_20_3 'LOC_20_4 'LOC_20_5 'LOC_20_6 'LOC_20_7 'LOC_20_8 'LOC_22 'LOC_24
            'LOC_26_1 'LOC_26_2 'LOC_26_3 'LOC_27_1 'LOC_27_2 'LOC_27_3 'LOC_27_4 'LOC_27_5 'LOC_27_6
            'LOC_27_7 'LOC_27_8 'LOC_28 'LOC_31 'LOC_34_1 'LOC_34_2 'LOC_34_3 'LOC_35_1 'LOC_35_2
            'LOC_35_3 'LOC_36_1 'LOC_36_2 'LOC_36_3 'LOC_36_4 'LOC_36_5 'LOC_36_6 'LOC_36_7 'LOC_36_8
            'LOC_38 'LOC_39 'LOC_48 'LOC_49 'LOC_50 'LOC_51 'LOC_52_1 'LOC_52_2
            'LOC_52_3 'LOC_52_4 'LOC_52_5 'LOC_52_6 'LOC_53_1 'LOC_53_2 'LOC_53_3 'LOC_54 'LOC_55
            'LOC_56 'LOC_57 'LOC_66 'LOC_67 'LOC_68 'LOC_69 'LOC_70 'LOC_71_1 'LOC_71_2 'LOC_71_3 'LOC_71_4
            'LOC_71_5 'LOC_71_6 'LOC_72 'LOC_73 'LOC_74_1 'LOC_74_2 'LOC_74_3 'LOC_75 'LOC_78 'LOC_79
            'LOC_80 'LOC_81_1 'LOC_81_2 'LOC_81_3 'LOC_81_4 'LOC_81_5 'LOC_81_6 'LOC_82 'LOC_83 'LOC_90
            'LOC_91 'LOC_100 'LOC_101 'LOC_104 'LOC_105 'LOC_106_1 'LOC_106_2 'LOC_106_3 'LOC_106_4
            'LOC_106_5 'LOC_106_6 'LOC_106_7 'LOC_106_8 'LOC_106_9 'LOC_108_1 'LOC_108_2 'LOC_108_3
            'LOC_109 'LOC_110 'LOC_111 'LOC_113 'LOC_114 'LOC_115 'LOC_116 'LOC_117 'LOC_118_1
            'LOC_118_2 'LOC_118_3 'LOC_118_4 'LOC_118_5 'LOC_118_6 'LOC_118_7 'LOC_118_8 'LOC_118_9
            'LOC_120 'LOC_121_1 'LOC_121_2 'LOC_121_3 'LOC_122 'LOC_123 'LOC_124
            'LOC_130 'LOC_131_1 'LOC_131_2 'LOC_131_3 'LOC_131_4 'LOC_131_5 'LOC_131_6 'LOC_131_7
            'LOC_131_8 'LOC_131_9 'LOC_132 'LOC_133 'LOC_134 'LOC_141 'LOC_142 'LOC_143_1 'LOC_143_2
            'LOC_143_3 'LOC_144_1 'LOC_144_2 'LOC_144_3 'LOC_145_1 'LOC_145_2 'LOC_145_3 'LOC_145_4
            'LOC_145_5 'LOC_145_6 'LOC_145_7 'LOC_145_8 'LOC_147 'LOC_148 'LOC_153_1
            'LOC_153_2 'LOC_153_3 'LOC_154_1 'LOC_154_2 'LOC_154_3 'LOC_154_4 'LOC_154_5 'LOC_154_6
            'LOC_154_7 'LOC_154_8 'LOC_155 'LOC_156 'LOC_157_1 'LOC_157_2 'LOC_157_3 'LOC_162_1 'LOC_162_2
            'LOC_162_3 'LOC_163_1 'LOC_163_2 'LOC_163_3 'LOC_163_4 'LOC_163_5 'LOC_163_6 'LOC_163_7 'LOC_163_8 'LOC_164
            }
          [(global/RTobject-type x)])))

;;; Do we need the doorless entrances too?

;; 'CorridorJoin 'DoorlessEntrance 'hole-in-the-wall
;; 'Door 'DoubleDoor 'Opening 'Extension

(defn is-a-door
  [object]
  (or
   ;; Real doors
   (some #{'Door}              [(global/RTobject-type object)])
   (some #{'DoubleDoor}        [(global/RTobject-type object)])
   ;; Doorless entrances
   (some #{'CorridorJoin}      [(global/RTobject-type object)])
   (some #{'DoorlessEntrance}  [(global/RTobject-type object)])
   (some #{'hole-in-the-wall}  [(global/RTobject-type object)])))

;;; +++ should initialize all doorless entrances to be "open"
;;; but could be closed by rubble!

(defn door-open?
  [adoor]
  (let [door-var        (global/RTobject-variable adoor)
        bel-door-closed (bs/get-belief-in-variable door-var :closed)]
    (and (is-a-door adoor) (<= bel-door-closed 0.2))))

;;; Fin
