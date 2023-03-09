;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.volumes
  "Manipulation of volumes and spaces"
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

#_(in-ns 'rita.state-estimation.volumes)

;;;(def ^:dynamic *debug-level* #{:demo :interventions :unhandled}) ; :all
;;;(defn dplev
;;;  [& x]
;;;  (some *debug-level* x))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Volumes
(def ^:dynamic *volumes* (atom {:rooms (atom [])
                                :doors (atom [])
                                :corridors (atom [])
                                :objects (atom [])}))


;;; Do we need to change this? +++ pr for the new map?

(def objects-of-interest
   {:Minecraft-object-anvil "victim-A" #_"anvil" ; TB0.2 and TB0/3
    ;;:bookshelf "bookshelf"
    ;;;:Minecraft-object-cauldron "victim-C" #_"cauldron"  ; TB0.2 and TB0/3
    ;;;:Minecraft-object-chest "chest"
    ;;;:Minecraft-object-crafting_table "table"
    :Minecraft-object-dispenser "dispenser"
    :Minecraft-object-flower_pot "flower pot"
    ;;;:Minecraft-object-furnace "furnace"
    :Minecraft-object-ladder "ladder"
    :Minecraft-object-lever "switch"
    ;; :nether_brick_stairs "stairs"
    ;; :oak_stairs "stairs"
    ;; :quartz_stairs "stairs"
    :Minecraft-object-stone_button "button"
    :Minecraft-object-wall_sign "wall sign"
    :Minecraft-object-wooden_button "button"
    :block_victim_1 "victim-G" #_"gold_block"  ; TB0.4 TB0.5 Yellow victim
    :block_victim_2 "victim-Y" #_"prismarine"  ; TB0/4 TB0.5 Green victim
    :Minecraft-object-gold_block "victim-Y" #_"gold_block"  ; TB0.4 TB0.5 Yellow victim
    :Minecraft-object-prismarine "victim-G" #_"prismarine"  ; TB0/4 TB0.5 Green victim
    ;; :Minecraft-object-wool "victim-W" #_"wool"
    })  ; TB0.2 and TB0/3

;;; Voxel types found in the Saturn building

;;; Door types
;;; "acacia_door"
;;; "birch_door"
;;; "birch_fence_gate"
;;; "dark_oak_door"
;;; "spruce_door"
;;; "wooden_door"

;;; Everything else
;;; "birch_fence"
;;; "bedrock"
;;; "birch_stairs"
;;; "bone_block"
;;; "bookshelf"
;;; "brewing_stand"
;;; "brick_block"
;;; "cauldron"
;;; "chest"
;;; "cobblestone"
;;; "crafting_table"
;;; "dark_oak_fence"
;;; "dark_oak_stairs"
;;; "double_stone_slab"
;;; "fire"
;;; "flower_pot"
;;; "flowing_water"
;;; "furnace"
;;; "glass"
;;; "glass_pane"
;;; "hardened_clay"
;;; "heavy_weighted_pressure_plate"
;;; "iron_block"
;;; "jukebox"
;;; "lapis_ore"
;;; "lever"
;;; "lever"
;;; "log"
;;; "pink_glazed_terracotta"
;;; "planks"
;;; "quartz_block"
;;; "quartz_stairs"
;;; "sea_lantern"
;;; "spruce_fence"
;;; "spruce_stairs"
;;; "stained_glass"
;;; "stained_glass_pane"
;;; "stained_hardened_clay"
;;; "stone"
;;; "stone_brick_stairs"
;;; "stone_pressure_plate"
;;; "stone_slab"
;;; "stonebrick"
;;; "trapped_chest"
;;; "wall_banner"
;;; "wall_sign"
;;; "web"
;;; "wooden_slab"
;;; "wool"

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Get all volume objects, build nested object structure, and compute
;;; distanced to and between volumes.


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Temporary neighbor information (to be calculated automatically

(def left-side-peripheral-then-center   ; goal = RoomJ
  [["/Falcon.StagingArea" "/Falcon.Lobby"]
   ["/Falcon.Lobby" "/Falcon.BreakR"]
   ["/Falcon.BreakR" "/Falcon.ExecS1"]
   ["/Falcon.ExecS1" "/Falcon.ExecS2"]
   ["/Falcon.ExecS2" "/Falcon.CSNorth"]
   ["/Falcon.CSNorth" "/Falcon.Terrace"]
   ["/Falcon.Terrace" "/Falcon.Room101"]
   ["/Falcon.Room101" "/Falcon.Room102"]
   ["/Falcon.Room102" "/Falcon.Room103"]
   ["/Falcon.Room103" "/Falcon.Room104"]
   ["/Falcon.Room104" "/Falcon.Room105"]
   ["/Falcon.Room105" "/Falcon.Room106"]
   ["/Falcon.Room106" "/Falcon.Room107"]
   ["/Falcon.Room107" "/Falcon.Room108"]
   ["/Falcon.Room108" "/Falcon.Room109"]
   ["/Falcon.Room109" "/Falcon.Room110"]
   ["/Falcon.Room110" "/Falcon.Room111"]
   ["/Falcon.Room111" "/Falcon.Cfarm"]
   ["/Falcon.Cfarm" "/Falcon.WomRR"]
   ["/Falcon.WomRR" "/Falcon.MenRR"]
   ["/Falcon.MenRR" "/Falcon.SCR2"]
   ["/Falcon.SCR2", "/Falcon.SCR1"]
   ["/Falcon.SCR1" "/Falcon.MCR"]
   ["/Falcon.MCR" "/Falcon.RoomJ"]])

(def right-side-peripheral-then-center   ; goal = SCR2
  [["/Falcon.StagingArea" "/Falcon.Lobby"]
   ["/Falcon.Lobby" "/Falcon.Cfarm"]
   ["/Falcon.Cfarm" "/Falcon.Room111"]
   ["/Falcon.Room111" "/Falcon.Room110"]
   ["/Falcon.Room110" "/Falcon.Room109"]
   ["/Falcon.Room109" "/Falcon.Room108"]
   ["/Falcon.Room108" "/Falcon.Room107"]
   ["/Falcon.Room107" "/Falcon.Room106"]
   ["/Falcon.Room106" "/Falcon.Room105"]
   ["/Falcon.Room105" "/Falcon.Room104"]
   ["/Falcon.Room104" "/Falcon.Room103"]
   ["/Falcon.Room103" "/Falcon.Room102"]
   ["/Falcon.Room102" "/Falcon.Room101"]
   ["/Falcon.Room101" "/Falcon.CSNorth"]
   ["/Falcon.CSNorth" "/Falcon.Terrace"]
   ["/Falcon.Terrace" "/Falcon.ExecS2"]
   ["/Falcon.ExecS2" "/Falcon.ExecS1"]
   ["/Falcon.ExecS1" "/Falcon.BreakR"]
   ["/Falcon.BreakR" "/Falcon.RoomJ"]
   ["/Falcon.RoomJ" "/Falcon.MCR"]
   ["/Falcon.MCR" "/Falcon.MenRR"]
   ["/Falcon.MenRR" "/Falcon.WomRR"]
   ["/Falcon.WomRR" "/Falcon.SCR1"]
   ["/Falcon.SCR1" "/Falcon.SCR2"]])


(defn ingest-path-as-apsp
  [path]
  ;;; NYI
  nil)

(defn register-propositions
  [amap prop]
  (doseq [[from to] amap]
    (let [fromobj (first (eval/find-objects-of-name from))
          toobj  (first (eval/find-objects-of-name to))]
      (cond (nil? fromobj)
            (when (dplev :error :all) (println "ERROR, object not found:" from))

            (nil? toobj)
            (when (dplev :error :all) (println "ERROR, object not found:" to))

            :otherwise
            (do (bs/add-binary-proposition prop from to)
                (when (dplev :all) (println "(" prop from to ")")))))))

(defn establish-paths
  []
  (register-propositions left-side-peripheral-then-center :left-path-neighbor)
  (register-propositions right-side-peripheral-then-center :right-path-neighbor))

;;; solid 3D objects
(def ^:dynamic *solid-objects* [])

(defn get-all-solid-objects
  []
  (let [solid-object-classnames
        (map (fn [[kw val]] (symbol (name kw)))
             objects-of-interest)
        all-solid-objects (rt/find-objects-of-types solid-object-classnames)]
    (when (dplev :io :all) (println (count all-solid-objects) "solid objects found."))
    (def ^:dynamic *solid-objects* all-solid-objects)))

;;; empty space that may be filled with other 3D objects and in particular
;;; can be occupied by a human sized minecraft entity.
;;; ***+++ Thao +++***

(defn get-all-occupiable-spaces
  []
  (let [occupiable-space-classnames
        [;; Connections:
         'CorridorJoin 'DoorlessEntrance 'hole-in-the-wall
         'Door 'DoubleDoor 'Opening 'Extension

         ;; Rooms
         'FiveDoorRoom 'FourDoorRoom 'ThreeDoorRoom 'TwoDoorRoom 'Room
         'Room1Conn 'Room2Conn 'Room3Conn 'Room4Conn 'Room8Conn
         'Room9Conn 'Room14Conn 'Room7Conn 'Room5Conn 'Room11Conn

         ;; Treatmens
         'Treatment1Conn 'Treatment2Conn

         ;; Corridors:
         ;; a. Sparky Corridors
         'NorthCorridorWE 'NorthCorridocSN
         'SouthCorridocWE 'SouthCorridorNS
         'MainCorridor

         ;; b. Falcon Corridors
         'NorthCorridor 'WestCorridor
         'CenterCorridor 'EastCorridor
         'SouthCorridor

         ;; c. Saturn Corridors
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
         ]
        all-occupiable-spaces (rt/find-objects-of-types
                               occupiable-space-classnames)]
    (when (dplev :io :all) (println (count all-occupiable-spaces) "occupiable spaces found." ))
    #_(when (dplev :all)
        (println "all occupiable space:")
        (pprint all-occupiable-spaces))
    (seglob/set-occupiable-space-objects all-occupiable-spaces)))

(def ^:dynamic *corridors* [])

 ;;; ***+++ Thao +++***
(defn get-all-corridors
  []
  (let [corridor-classnames
        [;; Sparky Corridors
         'NorthCorridorWE 'NorthCorridocSN
         'SouthCorridocWE 'SouthCorridorNS
         'MainCorridor

         ;; Falcon Corridors
         'NorthCorridor 'WestCorridor
         'CenterCorridor 'EastCorridor
         'SouthCorridor

         ;; Saturn Corridors
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
         ]
        all-corridor-spaces (rt/find-objects-of-types corridor-classnames)]
    (when (dplev :io :all) (println (count all-corridor-spaces) "corridors found."))
    (def ^:dynamic *corridors* all-corridor-spaces)))

(def ^:dynamic *room-objects* [])

;;; ***+++ Thao +++***
(defn get-all-rooms
  []
  (let [room-classnames
        ['FiveDoorRoom 'FourDoorRoom 'ThreeDoorRoom 'TwoDoorRoom 'Room
         'Room1Conn 'Room2Conn 'Room3Conn 'Room4Conn 'Room8Conn
         'Room9Conn 'Room14Conn 'Room7Conn 'Room5Conn 'Room11Conn
         'Treatment1Conn 'Treatment2Conn]
        all-room-spaces (rt/find-objects-of-types room-classnames)]
    (when (dplev :io :all) (println (count all-room-spaces) "room spaces found."))
    (def ^:dynamic *room-objects* all-room-spaces)))

;;; objects the occupy 3D space
(def ^:dynamic *volume-objects* [])

;;; ***+++ Thao +++***
(defn get-all-volume-objects
  []
  (let [space-classnames
        [;; Connections
         'CorridorJoin 'DoorlessEntrance 'hole-in-the-wall
         'Door 'DoubleDoor 'Opening 'Extension

         ;; Rooms
         'FiveDoorRoom 'FourDoorRoom 'ThreeDoorRoom 'TwoDoorRoom 'Room
         'Room1Conn 'Room2Conn 'Room3Conn 'Room4Conn 'Room8Conn
         'Room9Conn 'Room14Conn 'Room7Conn 'Room5Conn 'Room11Conn

         ;; Treatmens
         'Treatment1Conn 'Treatment2Conn

         ;; Sparky corridors
         'NorthCorridorWE 'NorthCorridocSN
         'SouthCorridocWE 'SouthCorridorNS
         'MainCorridor

         ;; Falcon Corridors
         'NorthCorridor 'WestCorridor
         'CenterCorridor 'EastCorridor
         'SouthCorridor

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
         ]
        object-classnames (map (fn [[kw val]]
                                  (symbol (name kw)))
                                objects-of-interest)
        all-volume-classnames (concat space-classnames object-classnames)
        all-volume-objects (rt/find-objects-of-types all-volume-classnames)]
    (when (dplev :io :all) (println (count all-volume-objects) "volume objects found."))
    (def ^:dynamic *volume-objects* all-volume-objects)))

;;; light switches
(def ^:dynamic *switches* [])

(defn get-all-switches
  []
  (let [switch-classnames ['Minecraft-object-lever]
        all-switches (rt/find-objects-of-types switch-classnames)]
    (when (dplev :io :all) (println (count all-switches) "switches found."))
    (def ^:dynamic *switches* all-switches)))

;;; lights
(def ^:dynamic *lights* [])

(defn get-all-lights
  []
  (let [light-classnames ['Light]
        all-lights (rt/find-objects-of-types light-classnames)]
    (when (dplev :io :all) (println (count all-lights) "lights found."))
    (def ^:dynamic *lights* all-lights)))

;;; portals between spaces (doors and corridor joins between spaces
(def ^:dynamic *portals* [])

;;; ***+++ Thao +++***
(defn get-all-portals
  []
  (let [portal-classnames ['CorridorJoin 'DoorlessEntrance 'Door 'DoubleDoor 'hole-in-the-wall
                           'Door 'DoubleDoor 'Opening 'Extension]
        all-portals (rt/find-objects-of-types portal-classnames)]
    (when (dplev :io :all) (println (count all-portals) "portals found."))
    (def ^:dynamic *portals* all-portals)))

;;; participants
(def ^:dynamic *participants* [])

(defn get-all-participants
  []
  (let [participant-classnames ['Participant]
        all-participants (rt/find-objects-of-types participant-classnames)]
    (when (dplev :io :all) (println (count all-participants) "participants found."))
    (def ^:dynamic *participants* all-participants)))

;;; victims
(def ^:dynamic *victims* [])

(defn get-all-victims
  []
  (let [victim-classnames [;'Minecraft-object-wool
                           ;'Minecraft-object-anvil
                           ;'Minecraft-object-cauldron
                           'Minecraft-object-prismarine
                           'Minecraft-object-gold_block
                           'block_victim_1
                           'block_victim_1b
                           'block_victim_2]
        all-victims (rt/find-objects-of-types victim-classnames)]
    (when (dplev :io :all) (println (count all-victims) "victims found."))
    (def ^:dynamic *victims* all-victims)))

(defn tl-coordinates
  [obj]
  (let [x (eval/deref-field ['tl-x] obj :normal)
        y (eval/deref-field ['tl-y] obj :normal)
        z (eval/deref-field ['tl-z] obj :normal)]
    [x y z]))

(defn br-coordinates
  [obj]
  (let [x (eval/deref-field ['br-x] obj :normal)
        y (eval/deref-field ['br-y] obj :normal)
        z (eval/deref-field ['br-z] obj :normal)]
    [x y z]))

(defn is-contained-within
  "True if obj1 is contained within obj2"
  [obj1 obj2]
  (let [t1 (:type obj1)
        t2 (:type obj2)
        x1tl (eval/deref-field ['tl-x] obj1 :normal)
        y1tl (eval/deref-field ['tl-y] obj1 :normal)
        z1tl (eval/deref-field ['tl-z] obj1 :normal)
        x1br (eval/deref-field ['br-x] obj1 :normal)
        y1br (eval/deref-field ['br-y] obj1 :normal)
        z1br (eval/deref-field ['br-z] obj1 :normal)
        x2tl (eval/deref-field ['tl-x] obj2 :normal)
        y2tl (eval/deref-field ['tl-y] obj2 :normal)
        z2tl (eval/deref-field ['tl-z] obj2 :normal)
        x2br (eval/deref-field ['br-x] obj2 :normal)
        y2br (eval/deref-field ['br-y] obj2 :normal)
        z2br (eval/deref-field ['br-z] obj2 :normal)]
    (if (or (nil? x1tl) (nil? y1tl) (nil? z1tl) (nil? x2tl) (nil? y2tl) (nil? z2tl)
            (nil? x1br) (nil? y1br) (nil? z1br) (nil? x2br) (nil? y2br) (nil? z2br))
      (when (dplev :error :all) (println "Something went wrong with obj1=" obj1 "obj2=" obj2))
      (let [result (and
                    (>= x1tl x2tl)(>= y1tl y2tl)(>= z1tl z2tl)
                    (<= x1br x2br)(<= y1br y2br)(<= z1br z2br))]
        result))))

(defn find-victims-in-rooms
  []
  (let [vomap (into {}
                    (remove
                     nil?
                     (map (fn [obj2]
                            (let [clist
                                  (map (fn [obj1]
                                         (if (and (not (= obj1 obj2)) (is-contained-within obj1 obj2))
                                           obj1))
                                       *victims*)
                                  wonils (remove nil? clist)]
                              (if (not (empty? wonils))
                                { obj2 (into #{} wonils) })))
                          (seglob/get-occupiable-space-objects))))]
    ;; (when (dplev :all) (println "victims/space map=" vomap))
    (seglob/set-victims-in-occupiable-spaces vomap)
    (let [placed-victims (into #{} (apply concat (map (fn [[aspace victims]] victims) vomap)))
          allvictims (into #{} *victims*)
          unplaced (clojure.set/difference allvictims placed-victims)]
      (if (not (empty? unplaced))
        (do
          (when (dplev :warn :all) (println "No home found for these" (count unplaced) "victims:")
          (doseq [avictim (into [] unplaced)]
            (pprint avictim))))))
    (seglob/get-victims-in-occupiable-spaces)))

(defn is-contained-at-least-partially-in-volume
  "True if obj1 is at least partially contained within obj2"
  [obj1 obj2]
  (let [t1 (:type obj1)
        t2 (:type obj2)
        x1tl (eval/deref-field ['tl-x] obj1 :normal)
        y1tl (eval/deref-field ['tl-y] obj1 :normal)
        z1tl (eval/deref-field ['tl-z] obj1 :normal)
        x1br (eval/deref-field ['br-x] obj1 :normal)
        y1br (eval/deref-field ['br-y] obj1 :normal)
        z1br (eval/deref-field ['br-z] obj1 :normal)
        x2tl (eval/deref-field ['tl-x] obj2 :normal)
        y2tl (eval/deref-field ['tl-y] obj2 :normal)
        z2tl (eval/deref-field ['tl-z] obj2 :normal)
        x2br (eval/deref-field ['br-x] obj2 :normal)
        y2br (eval/deref-field ['br-y] obj2 :normal)
        z2br (eval/deref-field ['br-z] obj2 :normal)]
    (if (or (nil? x1tl) (nil? y1tl) (nil? z1tl) (nil? x2tl) (nil? y2tl) (nil? z2tl)
            (nil? x1br) (nil? y1br) (nil? z1br) (nil? x2br) (nil? y2br) (nil? z2br))
      (when (dplev :error :all) (println "Something went wrong with obj1=" obj1 "obj2=" obj2))
      (let [result (or
                    (and (>= x1tl x2tl)(>= y1tl y2tl)(>= z1tl z2tl))
                    (and (<= x1br x2br)(<= y1br y2br)(<= z1br z2br)))]
        result))))

(defn victims-in-volume
  [avolume]
  (if avolume (remove nil? (map (fn [v] (and (is-contained-within v avolume) v)) *victims*))))

;;; Given my coordinates,
(defn where-am-I
  [x y z]
  (and (not (empty? (seglob/get-occupiable-space-objects)))
       (some (fn [avol]
               (ras/coordinates-in-volume x y z avol)
               #_(ras/coordinates-at-least-partially-in-volume x y z avol))
             (seglob/get-occupiable-space-objects))))

(defn in-a-portal?
  [x y z]
  (and (not (empty?  *portals*))
       (some (fn [avol]
               (ras/coordinates-in-volume x y z avol))
             *portals*)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Path distances

;;; Portals link occupiable spaces - usually two - always two at present
;;; For each portal, calculate a list of occupiable spaces that it connects to
;;; Produce amap of portals to occupiable space connections.
;;; The distance between one occupiable space and another to which it is
;;; directly connected is zero
;;; The distance from one space to another is the straight line distance + maneuvering distance
;;; between one door and onother if they share the same space (a room or a corridor).
;;; To get into a room it is necessary to go to a door that that room occupies.
;;; If player is in that room, that distance is the distance between that player and the door.
;;; The distance between one room and another that share a door is zero.
;;; The distance between two rooms that have rooms on a shared corridoor is the distance betw<een those doors.

(def maneuvering-room 2)                ; Estimate of the distance covered entering and leaving a portal

(defn distance-between-portals          ; Needs to be less generous and return INF where appropriate +++
  [p1 p2]
  (if (= p1 p2)
    0
    (let [p1-fields @(global/RTobject-fields p1)
          p2-fields @(global/RTobject-fields p2)
          posP1x (/ (+ @(get p1-fields 'tl-x) @(get p1-fields 'br-x)) 2.0)
          posP1y (/ (+ @(get p1-fields 'tl-y) @(get p1-fields 'br-y)) 2.0)
          posP2x (/ (+ @(get p2-fields 'tl-x) @(get p2-fields 'br-x)) 2.0)
          posP2y (/ (+ @(get p2-fields 'tl-y) @(get p2-fields 'br-y)) 2.0)
          distance (ras/straight-line-distance posP1x posP1y 0 posP2x posP2y 0)]
      (+ distance maneuvering-room))))

(defn distance-to-portals
  [x y z portal]
    (let [p1-fields @(global/RTobject-fields portal)
          posP1x (/ (+ @(get p1-fields 'tl-x) @(get p1-fields 'br-x)) 2.0)
          posP1y (/ (+ @(get p1-fields 'tl-y) @(get p1-fields 'br-y)) 2.0)
          distance (ras/straight-line-distance posP1x posP1y 0 x y 0)]
      distance))


(defn shortest-distance
  [apsp x y]
  (let [{namemap :namemap
         pnames :pnames
         cmap :cmap
         portal-connects-map :pcm
         apsp-graph :apsp} apsp
        indexx (.indexOf pnames x)
        indexy (.indexOf pnames y)]
    (if (and (>= indexx 0) (>= indexy 0))
      (let [dist (nth (nth apsp-graph indexx) indexy)]
        dist)
      (do
        (if (< indexx 0) (when (dplev :error :all) (println "x not found in pnames" x pnames)))
        (if (< indexy 0) (when (dplev :error :all) (println "y not found in pnames" y pnames)))
        nil))))

(def ^:dynamic *space-to-doors-map* {})

(defn compute-space-to-door-map
  [spaces portals]
  "Computes a map from space objects to portal names"
  (let [pnameset (into #{} (map global/RTobject-variable portals))
        smap (into {}
                   (remove nil?
                           (map (fn [aspace]
                                  (let [sname  (global/RTobject-variable aspace)
                                        sprops (bs/find-binary-propositions-matching
                                                #{sname} nil #{:connects-with} nil nil nil)
                                        convia (into #{} (map (fn [p] (:object p)) sprops))
                                        portals (clojure.set/intersection pnameset convia)]
                                    (if (not (empty? portals))
                                      {aspace (clojure.set/intersection pnameset convia)})))
                                spaces)))]
      (def ^:dynamic *space-to-doors-map* smap)
      (when (dplev :apsp :all)
        (println "Here is the space-door map:")
        (pprint smap))
      smap))

(defn compute-connectivity-map-for-rooms
  [rooms smap papsp]
  (let [;;rnameset (into #{} (map global/RTobject-variable rooms))
        dists
        (into
         {}
         (remove nil?
                 (map
                  (fn [roomX]
                    (let [roomxname (global/RTobject-variable roomX)
                          distsX
                          (into
                           {}
                           (remove
                            nil?
                            (map
                             (fn [roomY]
                               (let [roomyname (global/RTobject-variable roomY)]
                                 (if (= roomX roomY)
                                   {roomyname 0.0} ; trivial case
                                   (let [portalsX (get smap roomX)
                                         portalsY (get smap roomY)
                                         xvaly
                                         (remove
                                          nil?
                                          (map (fn [apx]
                                                 (let [yvals
                                                       (remove
                                                        nil?
                                                        (map (fn [apy]
                                                               (let [dist (shortest-distance papsp apx apy)]
                                                                 (if (not (ras/INF? dist)) dist)))
                                                             portalsY))]
                                                   (if (not (empty? yvals)) (apply min yvals))))
                                               portalsX))
                                         distPxPy (if (not (empty? xvaly)) (+ 4 (apply min xvaly)))] ;+++ 4
                                     (if distPxPy {roomyname distPxPy})))))
                             rooms)))]
                      (if distsX {roomxname distsX})))
                  rooms)))]
    (when (not (empty? dists))
      (when (dplev :apsp :all)
        (println "connectivity-map-for-rooms")
        (pprint dists))
      dists)))

(defn compute-rita-format-learned-room-apsp
  "Convert probabilities into description lengths which we treat as distances"
  [rvo-model]
  (into {}
        (map
         (fn [[[fromroom] destinations]]
           {fromroom
            (into {}
                  (map
                   (fn [[toroom prob]]
                     {toroom (ras/description-length prob)})
                   destinations))})
         rvo-model)))

(defn compute-learned-map-for-rooms ;+++***+++ need to update
  "Build RITA format room apsp datastructure for the running mission"
  []
  (let [emission (seglob/get-experiment-mission)
        role "Search_Specialist"
        loadedlm (seglob/get-learned-model)
        rvo-all-models (first (get loadedlm :room-visit-order)) ; first=bigram second=trigram
        rvo-model (get rvo-all-models [role emission])
        rf-apsp (compute-rita-format-learned-room-apsp rvo-model)]
    ;; (when (dplev :all) (println "*************"))
    ;; (when (dplev :all) (println "emission=" emission))
    ;; (when (dplev :all) (println "loadedlm=" loadedlm))
    ;; (when (dplev :all) (println "all-models" rvo-all-models))
    ;; (when (dplev :all) (println "rvo-model=" rvo-model))
    (when (dplev :all)
      (println "Room apsp for learned hypothesis")
      (pprint rf-apsp))
    rf-apsp))

(defn compute-space-to-space-map
  [spaces portals]
  "Computes a map from space objects via portal to space"
  (let [pnameset (into #{} (map global/RTobject-variable portals))
        ssmap (into {}
                   (remove nil?
                           (map (fn [aspace]
                                  (let [sname  (global/RTobject-variable aspace)
                                        sprops (bs/find-binary-propositions-matching
                                                #{sname} nil #{:connects-with} nil nil nil)
                                        convia (into #{} (map (fn [p] (:object p)) sprops))
                                        portals (clojure.set/intersection pnameset convia)]
                                    ;; +++ unfinished get other side of portals +++
                                    (if (not (empty? portals))
                                      {aspace (clojure.set/intersection pnameset convia)})))
                                spaces)))]
      (def ^:dynamic *space-to-space-map* ssmap)
      ;;(when (dplev :all)
      ;;  (println "Here is the space-space map:")
      ;;  (pprint smap))
      ssmap))

;;; (def smap (compute-space-to-door-map  (seglob/get-occupiable-space-objects) *portals*))

(def ^:dynamic *cached-cmap* {})
;;; (pprint *cached-cmap*)

(defn compute-connectivity-map-from-portals
  [[portals distance-function]]
  "Computes a map from portal objects to a set of spaces names that they connect."
  (let [cmap (into {}
                   (map (fn [aportal]
                          (let [pname  (global/RTobject-variable aportal)
                                cprops (bs/find-binary-propositions-matching
                                        #{pname} nil #{:connects-with} nil nil nil)
                                conto (into #{} (map (fn [p] (:object p)) cprops))]
                            {aportal conto}))
                        portals))]
    ;;(when (dplev :all) (println "Here is the connectivity map:") (pprint cmap))
    (def ^:dynamic *cached-cmap* cmap)
    [cmap distance-function]))

(defn compute-distances-between-portals
  [[cmap distance-function]]
  ;; For every portal, compute the distance from that portal to every other portal with
  ;; a direct connection
  (let [portal-connects-map
        (into {}
              (map (fn [[aportal cset]]
                     (let [connections
                           (into {}
                                 (remove nil?
                                         (map (fn [[aportal2 cset2]]
                                                (let [shared (clojure.set/intersection cset cset2)]
                                                  (if (not (empty? shared))
                                                    {(global/RTobject-variable aportal2)
                                                     (distance-function aportal aportal2)}))) ; distance-between-portals
                                              cmap)))]
                       (if (not (empty? connections))
                         {(global/RTobject-variable aportal) connections})))
                   cmap))]
    ;;(when (dplev :all) (println "portal-connects-map:") (pprint portal-connects-map))
    [cmap portal-connects-map]))

(defn get-object-vname
  [artobj]
  (if artobj
    (let [obj-fields @(global/RTobject-fields artobj)
          name-field (get obj-fields 'v-name)
          _ (if (not (= (type name-field) clojure.lang.Atom))
              (when (dplev :error :all) (println "ERROR: missing field" 'v-name "in: " artobj "fields=" obj-fields)))
          vname  (if (= (type name-field) clojure.lang.Atom) @name-field (str "Missing-field_v-name"))]
      vname)
    "Unknown"))

(defn compute-connectivity-matrix
  [[cmap portal-connects-map]]
  ;; Create a connectivity matrix as input to apsp
  (let [pnames (map first portal-connects-map)
        vnames (map (fn [pome] (get-object-vname (first pome))) cmap)
        namemap (into {} (map (fn [pname vname] {pname vname}) pnames vnames))
        ;;_ (when (dplev :all) (println "name map is:" namemap))
        cmap4apsp (into []
                        (map
                         (fn [aFromPname]
                           (let [fromMap (get portal-connects-map aFromPname)]
                             (into []
                                   (map
                                    (fn [aToPname]
                                      (get fromMap aToPname (ras/get-INF)))
                                    pnames))))
                         pnames))]
    ;;(when (dplev :all) (println "pnames=" pnames "cmap4apsp:") (printGraph cmap4apsp))
    [namemap pnames cmap portal-connects-map cmap4apsp]))

(defn compute-apsp
  [[namemap pnames cmap portal-connects-map cmap4apsp]]
  (let [apsp-graph (ras/apsp cmap4apsp)]
    (when (dplev :apsp :all)
      (println "APSP graph:")
      (ras/printGraph apsp-graph))
    {:namemap namemap
     :pnames pnames
     :cmap cmap
     :pcm portal-connects-map
     :apsp apsp-graph}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Portal APSP


(def ^:dynamic *portal-apsp* nil)

(defn compute-connectivity-map
  [portals distance-function]
  (-> [portals distance-function]
      compute-connectivity-map-from-portals
      compute-distances-between-portals
      compute-connectivity-matrix
      compute-apsp))

;;; (def pcmap (compute-connectivity-map *portals* distance-between-portals))
;;; (def pnames (map first pcmap))
;;; (def num-pnames (count pnames))

(defn rand-mc-select
  [pairs]
  (loop [choices pairs
         val (rand 1.0)]
    (let [choiceval (second (first choices))
          otherchoices (rest choices)]
      (if (or (empty? otherchoices) (<= val choiceval))
            (first choices)
            (recur otherchoices (- val choiceval))))))

;;; Squared distances Gravity
(defn mcrandom-prefer-small-distance
  [candidates]
  (let [max-dist (float (second (apply max-key second candidates)))
        mindist  (+ 0.1 (second (apply min-key second candidates)))
        distpref (map (fn [[place dist]] (let [sdist (/ mindist dist)] [place (* sdist sdist)])) candidates)
        totaldistpref (apply + (map second distpref))
        probcands (map (fn [[place distpref]] [place (/ distpref totaldistpref)]) distpref)
        choice (rand-mc-select probcands)]
    ;; (when (dplev :all) (println "max-dist=" max-dist "min-dist=" mindist "totaldistpref=" totaldistpref))
    ;; (when (dplev :all) (println "candidates=" candidates))
    ;; (when (dplev :all) (println "probcands=" probcands))
    ;; (when (dplev :all) (println "choice=" choice))
    choice))

;;; (mcrandom-prefer-small-distance [[:A 1] [:B 2] [:C 3] [:D 4] [:E 5]])

(def ^:dynamic *candidate-selection-algorithm* :closest)
(def known-choice-methods [:closest :mcclose])

(defn set-unvisited-room-near-mc-choice-method
  [amethod]
  (if (some #{amethod} known-choice-methods)
    (def ^:dynamic *candidate-selection-algorithm* amethod)
    (when (dplev :error :all)
      (println "ERROR: Unknown choice method" amethod "specified for set-unvisited-room-near-mc-choice-method"))))

(defn select-candidate
  [candidates]
  (case *candidate-selection-algorithm*
    :closest (apply min-key second candidates)
    :mcclose (mcrandom-prefer-small-distance candidates)
    (first candidates)))                ; Best not to drop into the default.  This is a lousy strategy!

(def test-invert-apsp-map {"foo" {"bar" 1, "baz" 2},
                           "baz" {"bar" 3, "quux" 4},
                           "quux" {"bar" 3, "foo" 2}})

;;; (ras/merge-data-by-head [[1 2 3] [1 3 5] [1 5 7] [2 3 4]] list)
;;; (invert-apsp-map test-invert-apsp-map {"asd" 100000 "baz" 100000})


(defn room-object-names
  []
  (map #(str (global/RTobject-variable %)) *room-objects*))

(defn gap-filler-room-map
  []
  (let [allrooms (room-object-names)
        defaults (into {} (map (fn [name] [name 100000]) allrooms))] ; +++ large distance
    defaults))

(defn noninvert-apsp-gap-fill
  [amap gap-fillers]
  (let [triples (apply concat (map (fn [[from tomap]]
                                     (map (fn [[to distance]] [from to distance])
                                          tomap)) amap))
        remerged (ras/merge-data-by-head
                  triples
                  #(conj gap-fillers (into {} (map (fn [x] [(nth x 1) (nth x 2)]) %))))]
    remerged))

(defn invert-apsp-map
  [amap gap-fillers]
  (let [triples (apply concat (map (fn [[from tomap]]
                                     (map (fn [[to distance]] [to from distance])
                                          tomap)) amap))
        remerged (ras/merge-data-by-head
                  triples
                  #(conj gap-fillers (into {} (map (fn [x] [(nth x 1) (nth x 2)]) %))))]
    remerged))

(def ^:dynamic *debug-apsp* nil)
(def ^:dynamic *debug-room* nil)

;; (def *set-keys* (into #{} (keys *debug-apsp*)))
;; (def *set-rooms* (into #{} (room-object-names)))
;; (clojure.set/difference  *set-rooms* *set-keys*)
;; seglob/*stat-record*

(defn get-unvisited-room-near
  [aroomname direction & [visitedset]]
  (let [gfm (gap-filler-room-map)
        hypothesis-apsp (if (= direction :forward) ;+++ push this back earlier
                          (noninvert-apsp-gap-fill (seglob/get-room-apsp) gfm)
                          (invert-apsp-map (seglob/get-room-apsp) gfm))
        ;;_ (when (dplev :all) (println "hypothesis-apsp="))
        ;;_ (when (dplev :all) (pprint hypothesis-apsp))
        _ (if (empty? hypothesis-apsp) (when (dplev :error :all) (println "ERROR: hypothesis-apsp is empty")))
        _ (def ^:dynamic *debug-apsp* hypothesis-apsp)
        _ (def ^:dynamic *debug-room* aroomname)
        allrdpairs (get hypothesis-apsp aroomname)
        _ (if (empty? allrdpairs) (when (dplev :error :all) (println "ERROR: allrdpairs is empty")))
        start "/Falcon.StagingArea" ;+++ temporary hard wired knowledge
        lobby "/Falcon.Lobby"
        rdpairs (case direction
                  :forward (cond (= aroomname start) {lobby 0.0}
                                 (= aroomname lobby) allrdpairs
                                 :otherwise (dissoc allrdpairs start))
                  :backward (cond (= aroomname lobby) {start 0.0}
                                 (= aroomname lobby) allrdpairs
                                 :otherwise allrdpairs))]
    ;; (when (dplev :all) (println "In get-unvisited-room-near with aroomname=" (global/prs aroomname) "rdpairs=" (global/prs rdpairs)))
    (if (empty? rdpairs)
      (do (when (dplev :all) (println "In get-unvisited-room-near: room-apsp failure room=" (global/prs aroomname) "room-apsp=" (global/prs (seglob/get-room-apsp))))
          (/ 1 0))
      (let [candidates
            (remove
             (fn [[room dist]]
               (or
                (= room aroomname) ; cant' go to the starting room, we are there already
                (>= (bs/get-belief-in-variable room :visited) 0.8) ; already been there really
                (= (eval/get-object-value room) :visited)          ; already been there in my mind
                (and visitedset (some #{room} visitedset))))
             rdpairs)
            ;; _ (if (empty? candidates) (when (dplev :all) (println "All candidates (" rdpairs ") visited")))
            ;; _ (when (dplev :all) (println "In get-unvisited-room-near candidates=" candidates "aroomname=" aroomname "rdpairs=" rdpairs))
            closest (if (not (empty? candidates)) (select-candidate candidates))]
        closest))))

(vp/def-virtual-proposition :nearby-unvisited-wrt-distance
  [prop arg1 arg2] ;; arg1 = whereIam arg2= nearby-room
  (let [result
        (cond
          (and (not (nil? arg1)) (not (nil? arg2)))
          (if (< (bs/get-belief-in-variable arg2 :visited) 0.2) ;+++
            [{:ptype prop, :subject arg1 :object arg2 :type :binary}]
            [])

          (and (not (nil? arg1)) (nil? arg2))
          (let [unvisited (first (get-unvisited-room-near arg1 :backward))]
            (if (not (empty? unvisited))
              [{:ptype prop, :subject arg1, :object unvisited, :type :binary}]
              []))

          (and (nil? arg1) (not (nil? arg2)))
          (let [unvisited (first (get-unvisited-room-near arg2 :backward))]
            (if (not (empty? unvisited))
              [{:ptype prop, :subject unvisited, :object arg2, :type :binary}]
              []))

          :otherwise
          (do (when (dplev :error :all) (println "Unhandled case in :nearby-unvisited-wrt-distance virtual-proposition [" prop arg1 arg2 "]"))
              []))]
    ;;(when (dplev :all) (println "vprop" prop arg1 arg2 "=" result))
    result))

(def ^:dynamic *lookahead-steps* 100)   ; make this settable+++

;;; If arg1 is specified and not arg2 arg2 is bound to the room reached
;;; after N steps.  If both are specified we check that it is true
;;; if arg2 is specified and not arg1, we find where we came from to be
;;; at arg2, and if nother are specified, we fail.  For now, only the arg1
;;; specified arg2 not specified, is implemented+++

(defn set-target-plan-length
  [n]
  (def ^:dynamic *lookahead-steps* n))

(vp/def-virtual-proposition :lookahead-steps
  [prop arg1 arg2] ; arg1 = whereIam arg2= nth-visit-room
  ;;(N=max for the moment, but have parameter to control look-ahead, later.
  ;; (vp/invoke-virtual-proposition :nearby-unvisited-wrt-distance lvar)
  (let [result
        (cond
          (and (not (nil? arg1)) (not (nil? arg2)))
          (if (< (bs/get-belief-in-variable arg2 :visited) 0.2) ;+++
            [{:ptype prop, :subject arg1 :object arg2 :type :binary}]
            [])

          (and (not (nil? arg1)) (nil? arg2))
          (loop [steps *lookahead-steps*
                 visited #{}
                 at arg1]
            (if (<= steps 0) ; we have reached our destination
              [{:ptype prop, :subject arg1 :object at :type :binary}]
              (let [next (binding [*candidate-selection-algorithm* :closest]
                           (first (get-unvisited-room-near at :forward visited)))]
                ;; (when (dplev :all) (println "look-ahead from" at "to" next))
                (if (nil? next) ; there is nothing beyond 'at'
                  [{:ptype prop, :subject arg1 :object at :type :binary}]
                  (recur (- steps 1) (conj visited at) next)))))

          (and (nil? arg1) (not (nil? arg2)))
          (do (when (dplev :error :all) (println "Unhandled case in :lookahead-steps virtual-proposition [" prop arg1 arg2 "]"))
              [])

          :otherwise
          (do (when (dplev :error :all) (println "Unhandled case in :lookahead-steps virtual-proposition [" prop arg1 arg2 "]"))
              []))]
    ;;;(when (dplev :all) (println "vprop" prop arg1 arg2 "=" result))
    result))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Compute greedy algorithm room order search

(defn get-rtobject-called
  [objectname listofobjects]
  (some #(if (= (get-object-vname %) objectname) %) listofobjects))

;;; Don't choose a door that leads to a room that we have already visited
(defn choose-best-door
  [doors visited]
  (if (= (count doors) 1)
    (first (into [] doors))             ; if we have no choice, go through that door
    (first (into [] doors)))) ;+++ need to implement choose best door (using visited)

(defn get-doors-from-room
  [inroom]
  "Returns the set of door names that connect to a room (object)."
  (get *space-to-doors-map* inroom))

(defn find-path-from-to
  [from-door to-door maps]
  (let [{namemap :namemap               ; Map from portal variable name to object name
         cmap    :cmap                  ; Map from portal objects to space names (one each side of the portal).
         pcm     :pcm                   ; What portals are attainable in a single hop
         apsp    :apsp} maps]           ; Indirect map can we get there in multiple hops?
;;; UNFINISHED
  ))

(defn get-doors-from-door
  [from-door in-space maps]
  "Returns all doors accessible from the given door when we are in 'in-space'."
  (let [{namemap :namemap               ; Map from portal variable name to object name
         cmap    :cmap                  ; Map from portal objects to space names.
         pcm     :pcm                   ; What portals are attainable in a single hop
         apsp    :apsp} maps]           ; Indirect map can we get there in multiple hops?
;;; UNFINISHED
    ))

(defn get-other-side-of
  [aportal inspace]
  "What space is on the other side of the portal if we are in 'inspace'"
  (let [cmap *cached-cmap*
        other-side-set (clojure.set/difference (get cmap aportal)
                                               #{(global/RTobject-variable inspace)})
       ;; _ (when (dplev :all) (println "get-other-side-of inspace=" (global/RTobject-variable inspace)))
        ;;_ (when (dplev :all) (println "pre difference" (get cmap aportal)))
        ;;_ (when (dplev :all) (println "other-side-set=" other-side-set))

        vname (first (into [] other-side-set))
        spaceobj (first (eval/find-objects-of-name vname))]
    ;;(when (dplev :all) (println "get-other-side-of " aportal inspace other-side-set vname) (pprint spaceobj))
    spaceobj))


(defn a-room?
  [aspace]
  "Returns true if the object (a space) is a room (and not a corridor)"
  (some (set *room-objects*) #{aspace}))

(defn a-portal?
  [aspace]
  "Returns true if the object (a space) is a portal"
  (some (set *portals*) #{aspace}))

(defn a-corridor?
  [aspace]
  "Returns true if the object is a corridor"
  (some (set *corridors*) #{aspace}))

;;; Find the objects on both sides of a portal
(defn get-both-sides-of-portal
  [aportal]
  (if (empty? (get *cached-cmap* aportal))
    (when (dplev :error :all) (println "Oops: In get-both-sides-of-portal with portal=" aportal "cmap=" *cached-cmap*)))
  (map (fn [x] (first (eval/find-objects-of-name x))) (get *cached-cmap* aportal)))

(defn get-rooms-from-portal
  [aportal]
  (remove (fn [x] (not (a-room? x))) (get-both-sides-of-portal aportal)))

(defn get-unvisited-rooms-from-portal
  [aportal]
  (remove (fn [r]
            (let [var (global/RTobject-variable r)]
              (>= (bs/get-belief-in-variable var :visited) 0.8))) ; +++
          (get-rooms-from-portal aportal)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; In a well illuminated space a person can see anything in the space
;;; that they are in.  In the dark, they can only see things that are relativly
;;; close.  The distance.  We may get more precision from the testbed, later.

(defn what-can-I-see
  [x y z whereIam maxdistance]
  (if whereIam
    (let [myvar (global/RTobject-variable whereIam)
          props (bs/find-binary-propositions-matching nil nil #{:is-contained-within} nil #{myvar} nil)
          contents (map (fn [p] (:subject p)) props)
          ;;_ (when (dplev :all) (println "in what-can-I-see" whereIam "found objects:" contents))
          objects (and contents
                       (remove nil?
                               (map (fn [vname]
                                      (let [object (first (eval/find-objects-of-name vname))]
                                        (if object
                                          (let [{v-name 'v-name
                                                 br-x   'br-x
                                                 br-y   'br-y
                                                 br-z   'br-z
                                                 tl-x   'tl-x
                                                 tl-y   'tl-y
                                                 tl-z   'tl-z} @(global/RTobject-fields object)]
                                            (cond
                                              (or (nil? x)    (nil? y)    (nil? z)
                                                  (nil? tl-x) (nil? tl-y) (nil? tl-z))
                                                (when (dplev :error :all) (println "ERROR in what-can-I-see" object x y z tl-x tl-y tl-z))

                                              (and maxdistance
                                                   (> (ras/straight-line-distance x y z @tl-x @tl-y @tl-z)
                                                      maxdistance))
                                              nil

                                              :otherwise
                                              [@v-name  object]))
                                          (when (dplev :error :all) (println "ERROR Object" vname "not found in what-can-I-see")))))
                                    contents)))]
      #_(when (dplev :all) (pprint objects))
      objects)))

(defn get-lever-from-coordinates
  [x y z]
  (let [switch (some (fn [aswitch]
                       (let [{sxl 'tl-x
                              syl 'tl-y
                              sz  'tl-z} (deref (:fields aswitch))]
                         (and (= x @sxl)
                              (= y @syl)
                              (= z @sz)
                              aswitch)))
                     *switches*)]
    (if (nil? switch)
      (when (dplev :warn :all) (println "Lever not found at" [x y z] "should we add it to the model, dynamic add?"))
      switch)))

(defn get-victim-from-coordinates
  [x y z]
  (let [victim (some (fn [avictim]
                       (let [{vxl 'tl-x
                              vyl 'tl-y
                              vz  'tl-z} (deref (:fields avictim))]
                         (and (= x @vxl)
                              (= y @vyl)
                              (= z @vz)
                              avictim)))
                     *victims*)]
    (if (nil? victim)
      (when (dplev :warn :all) (println "Victim not found at" [x y z] "should we add it to the model, dynamic add?"))
      victim)))

(defn get-door-from-coordinates
  [x y z]
  (let [door (some (fn [adoor]
                     (let [{dxl 'tl-x
                            dyl 'tl-y
                            dzl 'tl-z
                            dxr 'br-x
                            dyr 'br-y
                            dzr 'br-z} (deref (:fields adoor))]
                       (and (>= x @dxl) (<= x @dxr)
                            (>= y @dyl) (<= y @dyr)
                            (>= z @dzl) (<= z @dzr)
                            adoor)))
                   *portals*)]
    (if (nil? door)
      (when (dplev :warn :all) (println "Door not found at" [x y z] "should we add it to the model, dynamic add?"))
      door)))

(defn get-player-from-name
  [playername]
  (first *participants*) ; +++ need to work on this when we have more than one!
  playername)

(defn close-to-a-portal
   [x y z & [closeness]]
   (if (not (and (number? x) (number? y) (number? z)))
     (when (dplev :all) (println "In close-to-a-portal with xyz=" x y z)))
  (some (fn [aportal]
          (let [{pxl 'tl-x pxr 'br-x pyl 'tl-y pyr 'br-y pz  'tl-z} (deref (:fields aportal))]
            (and (>= (or closeness 2.0)
                     (ras/straight-line-distance
                      (/ (+ @pxl @pxr 1) 2.0) (/ (+ @pyl @pyr 1) 2.0) @pz
                      x y z))
                 aportal)))
        *portals*))

(defn close-to-a-switch
  [x y z & [closeness]]
  (some (fn [aswitch]
          (let [{pxl 'tl-x pxr 'br-x pyl 'tl-y pyr 'br-y pz  'tl-z} (deref (:fields aswitch))]
            (and (>= (or closeness 2.0)
                     (ras/straight-line-distance
                      (/ (+ @pxl @pxr 1) 2.0) (/ (+ @pyl @pyr 1) 2.0) z
                      x y z)) ; assume switch at reachable height
                 aswitch)))
        *switches*))

(defn close-to-a-victim
  [x y z & [closeness]]
  (some (fn [avictim]
          (let [{pxl 'tl-x pxr 'br-x pyl 'tl-y pyr 'br-y pz  'tl-z} (deref (:fields avictim))]
            (and (>= (or closeness 2.0)
                     (ras/straight-line-distance
                      (/ (+ @pxl @pxr 1) 2.0) (/ (+ @pyl @pyr 1) 2.0) z
                      x y z))
                 avictim)))
        *victims*))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; stablishing beliefs about the world.

(defn we-believe-all-doors-closed
  [belief]
  (doseq [aportal *portals*]
    (if (or (some #{'Door} [(global/RTobject-type aportal)])
            (some #{'DoubleDoor} [(global/RTobject-type aportal)]))
      (bs/set-belief-in-variable (global/RTobject-variable aportal) :closed belief))))

(defn we-believe-all-victims-awaiting-triage
  [belief]
  (doseq [avictim *victims*]
    (bs/set-belief-in-variable (global/RTobject-variable avictim) :awaiting-triage belief)))

(defn we-believe-all-rooms-unvisited
  [belief]
  (doseq [aroom *room-objects*]
    (if (a-room? aroom)
      (bs/set-belief-in-variable (global/RTobject-variable aroom) :unvisited belief))))

;;; This is the case up to TB  version 0.5
(defn we-believe-all-lights-off
  [belief]
  (doseq [a-switch *switches*]
    (bs/set-belief-in-variable (global/RTobject-variable a-switch) :off belief))
  (doseq [a-light *lights*]
    (bs/set-belief-in-variable (global/RTobject-variable a-light) :dark belief)))

;;; This is the case for Falcon TB version 1.00 upwards
(defn we-believe-all-lights-on
  [belief]
  (doseq [a-switch *switches*]
    (bs/set-belief-in-variable (global/RTobject-variable a-switch) :off belief))
  (doseq [a-light *lights*]
    (bs/set-belief-in-variable (global/RTobject-variable a-light) :dark belief)))

(defn establish-rita-propositions
  []
  ;; Add proposition types
  (bs/def-proposition-type :is-in)      ; Participant is in a space
  (bs/def-proposition-type :was-in)     ; Participant is in a space
  (bs/def-proposition-type :is-next-to) ; Participant is next to an object
  (bs/def-proposition-type :can-see))   ; Participant can see an object

(def ^:dynamic *last-room-visited* nil)

(defn set-last-room-visited
  [aroom]
  (def ^:dynamic *last-room-visited* aroom))

(defn get-last-room-visited
  []
  *last-room-visited*)

(defn get-from-room
  [whereIam x y z]
  (let [in-a-room (and (global/RTobject? whereIam) (a-room? whereIam))
        in-a-corridor (and (global/RTobject? whereIam) (a-corridor? whereIam))
        in-a-portal (let [portal (in-a-portal? x y z)]
                      (if (not (empty? portal)) (first (into () (get-rooms-from-portal portal)))))]
    (cond in-a-room      whereIam
          in-a-portal    in-a-portal
          in-a-corridor (get-last-room-visited)
          :otherwise nil)))

;;; the player object has a slot called 'p-location' that indicates where the plan should start
;;; and it must be a room.

(defn set-from-room
  [player fromroom]
  (when (dplev :all) (println "***** In set-from-room with player=" player "fromroom=" (global/RTobject-variable fromroom)))
  (if (and player fromroom (a-room? fromroom))
    (set-field-value! player 'p-location (global/RTobject-variable fromroom))
    (when (dplev :error :all) (println "ERROR ***** In set-from-room with player=" player "fromroom=" fromroom))))


(defn remove-all-victims
  []
  (let [greenvictims (eval/find-objects-of-type 'Minecraft-object-prismarine)
        goldvictims  (eval/find-objects-of-type 'Minecraft-object-gold_block)]
    (when (dplev :all) (println "Removing" (count greenvictims) "green victims"))
    (doseq [gv greenvictims] (global/remove-object gv))
    (when (dplev :all) (println "Removing" (count goldvictims) "gold victims"))
    (doseq [gv goldvictims] (global/remove-object gv))))


(defn establish-volumes
  []
  (get-all-portals)
  #_(when (dplev :all) (println "********The portals are:*******(" x y z "]"))
  #_(pprint *portals*)
  (compute-connectivity-map-from-portals [*portals* distance-between-portals])
  (get-all-rooms)
  (get-all-switches)
  (get-all-lights)
  #_(when (dplev :all) (println "********The switches are:*******(" x y z "]") (pprint *switches*))
  (get-all-victims)
  (get-all-solid-objects)
  (get-all-occupiable-spaces)
  (get-all-corridors)
  (get-all-volume-objects)
  (get-all-participants)
  (find-victims-in-rooms)
  (if (dplev :all :apsp)
    (println "about to compute connectivity map"))
  (def ^:dynamic *portal-apsp* (compute-connectivity-map *portals* distance-between-portals))
  (when (dplev :apsp :all)
    (println "portal-apsp:")
    (pprint (prop/prop-readable-form *portal-apsp*)))
  (compute-space-to-door-map (seglob/get-occupiable-space-objects) *portals*)

  (let [learned  (mphyp/add-plan-hypothesis "learned")
        opportun (mphyp/add-plan-hypothesis "opportunistic")
        ;righty (mphyp/add-plan-hypothesis "righty")
        ;lefty (mphyp/add-plan-hypothesis "lefty")
        learned-room-apsp (compute-learned-map-for-rooms)
        opportun-room-apsp (compute-connectivity-map-for-rooms
                            *room-objects* *space-to-doors-map* *portal-apsp*)]
    (seglob/set-room-apsp opportun-room-apsp)
    (mphyp/set-plan-hypo-apsp-model! learned learned-room-apsp)
    (mphyp/set-plan-hypo-apsp-model! opportun opportun-room-apsp))

  ;;(when (dplev :all) (println (count *volume-objects*) "Space occupying objects found:") (pprint *volume-objects*))
  (doseq [obj1 *solid-objects*]
    (doseq [obj2 (seglob/get-occupiable-space-objects)]
      (if (and (not (= obj1 obj2)) (is-contained-within obj1 obj2))
        (do
          (if (some (set *switches*) #{obj1})
            (when (dplev :all) (println ";;; Switch at" (tl-coordinates obj1) "is in" (get-object-vname obj2))))
          (bs/add-binary-proposition
           :is-contained-within (rt/.variable obj1) (rt/.variable obj2))))))
  (def ^:dynamic *rita-initialized* true))

;;; (establish-volumes)

;; {"app-id":"TestbedBusInterface",
;;  "mission-id":"edcfbabe-3da6-4896-bdd3-3f419132d352",
;;  "routing-key":"testbed-message",
;;  "testbed-message":{"data":{"mission_victim_list":
;;                             [{"block_type":"block_victim_1", ; 15 of block_victim_proximity; 10 of block_victim_1; 10 of block_victim_1b
;;                               "y":60.0,
;;                               "z":0.0,
;;                               "unique_id":1.0,
;;                               "room_name":"B8",
;;                               "x":-2205.0},
;;                              {"block_type":"block_victim_proximity",
;;                               "y":60.0,
;;                               "z":2.0,
;;                               "unique_id":3.0,
;;                               "room_name":"B8",
;;                               "x":-2205.0},
;;                              {"block_type":"block_victim_1b",
;;                               "y":60.0,
;;                               "z":52.0,
;;                               "unique_id":33.0,
;;                               "room_name":"D1",
;;                               "x":-2160.0},
;;                               ...],
;;                             "mission":"Saturn_A_Rubble",
;;                             "elapsed_milliseconds":7.0,
;;                             "mission_timer":"17 : 3"},
;;                     "header":{"timestamp":"2022-03-30T01:49:39.325Z",
;;                               "version":"1.1",
;;                               "message_type":"groundtruth"},
;;                     "msg":{"sub_type":"Mission:VictimList",
;;                            "replay_id":"a16920a1-b98a-4285-b749-b32d42656342",
;;                            "trial_id":"edcfbabe-3da6-4896-bdd3-3f419132d352",
;;                            "source":"simulator",
;;                            "replay_parent_id":"aec81ecb-7fdb-4b4b-9bf7-6ebab112021a",
;;                            "experiment_id":"1551846d-efe9-46cb-bffb-0c0f97c64689",
;;                            "replay_parent_type":"REPLAY",
;;                            "version":"0.6",
;;                            "timestamp":"2022-03-30T01:49:39.325Z"}},
;;  "timestamp":1650378692241,
;;  "received-routing-key":"testbed-message",
;;  "exchange":"rita"}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Handling ground truth messages from the test bed.

(defn add-victim-map-entry
  [vid name el1 el2 distdiff vtype vobj]
  (let [cvm (seglob/get-victim-map)]
    (seglob/set-victim-map
     (merge cvm {vid {:unique_id vid
                      :vtype vtype
                      :evacuation-locations [el1 el2]
                      :distance-difference distdiff}}))))

(defn dist-from-to-object
  [x y z obj]
  (let [pxl (eval/deref-field ['tl-x] obj :normal)
        pxr (eval/deref-field ['br-x] obj :normal)
        pyl (eval/deref-field ['tl-y] obj :normal)
        pyr (eval/deref-field ['br-y] obj :normal)
        pzl (eval/deref-field ['tl-z] obj :normal)
        pzr (eval/deref-field ['br-z] obj :normal)
        ox (/ (+ pxl pxr) 2)
        oy (/ (+ pyl pyr) 2)
        oz (/ (+ pzl pzr) 2)]
    (ras/straight-line-distance x y z ox oy oz)))

;;; Given two evacuation areas and a position returns [closest furthest distdif]
(defn order-evac-areas
  [x y z [oa1 oa2]]
  (let [dist-to-first (dist-from-to-object x y z oa1)
        dist-to-second (dist-from-to-object x y z oa2)
        distdif (- dist-to-first dist-to-second)]
    (if (< distdif 0)
      [oa1 oa2 (- distdif)]
      [oa2 oa1 distdif])))

(defn find-victim-evacuation-details
  [x y z blocktype]
  ;; +++ Note, hardwired names of evacuation areas in the Saturn map +++
  (let [Abrasion   [(first (eval/find-objects-of-name "/Saturn.TAAN"))
                    (first (eval/find-objects-of-name "/Saturn.TAAS"))]
        BoneDamage [(first (eval/find-objects-of-name "/Saturn.TABN"))
                    (first (eval/find-objects-of-name "/Saturn.TABS"))]
        Critical   [(first (eval/find-objects-of-name "/Saturn.TACN"))
                    (first (eval/find-objects-of-name "/Saturn.TACS"))]
        all-evacuation-areas (concat Abrasion BoneDamage Critical)]
    (cond (and (some nil? Abrasion) (dplev :all :error))
          (println "ERROR: Abrasion evacuation areas not found" Abrasion)

          (and (some nil? BoneDamage) (dplev :all :error))
          (println "ERROR: Abrasion evacuation areas not found" BoneDamage)

          (and (some nil? Critical) (dplev :all :error))
          (println "ERROR: Critical evacuation areas not found" Critical)

          (= blocktype "block_victim_1")
          (order-evac-areas x y z Abrasion)

          (= blocktype "block_victim_1b")
          (order-evac-areas x y z BoneDamage)

          (= blocktype "block_victim_proximity")
          (order-evac-areas x y z Critical)

          :otherwise
          (do
            (when (dplev :all :error)
              (println "ERROR: unknown victim type encountered" blocktype))
            (order-evac-areas x y z Critical)))))

(defn rita-handle-victim-list-message
  [tbm tb-version]
  (let [{trial-id :trial_id
         experiment-id :experiment_id
         source :source
         version :version
         timestamp :timestamp} (:msg tbm)
        {mission_timer :mission_timer
         mission :mission
         mission-victim-vec :mission_victim_list} (:data tbm)
        rootobj (first (eval/find-objects-of-type (symbol (seglob/get-loaded-model-name))))]
    (when (dplev :io :all) (println "Received victim list, mission=" mission "with" (count mission-victim-vec) "victims"))

    ;; We have an explicit victim list, so we delete all existing victims, instantiate new victims and then recalculate volumes to get volume
    ;; memberships correct.
    (remove-all-victims)
    (bs/remove-propositions-matching nil nil #{:is-contained-within} nil nil nil) ; Removal all :is-contained-within propositions.

    (doseq [{x :x,
             y :y,
             z :z,
             blocktype :block_type,
             room-name :room_name,
             unique_id :unique_id} mission-victim-vec]
      (when (dplev :all :io)
        (println "Processing new victim" unique_id "at: [" x y z "] of type" blocktype "in" room-name)
        #_(println "blocktype is a "
                 (cond (string? blocktype) "string" (symbol? blocktype) "symbol" (keyword? blocktype) "keywoord" :otherwise "???")))

      ;; Study 2
      (cond (or (and (= x -2222.0) (= y 60.0) (= z -5.0))
                (and (= x -2217.0) (= y 60.0) (= z -1.0)))
            (seglob/set-estimated-map-name "SaturnA")

            (or (and (= x -2212.0) (= y 60.0) (= z -3.0))
                (and (= x -2297.0) (= y 60.0) (= z -1.0)))
            (seglob/set-estimated-map-name "SaturnB"))

      (let [nv-type (if (= blocktype 'block_victim_1) "prismarine" "gold_block")     ;+++ make sure that the block assignment is correct!
            victim-name (str "victim-at-" x "-" y "-" z "-" blocktype "-in-" room-name "-with-id-" unique_id)
            field (if (or (= (symbol blocktype) 'block_victim_1)(= (symbol blocktype) 'block_victim_1b))
                    ['dummy-block-victim-1]  ; Normal victims of type A or B (maybe we should make a dummy-block-victim-1b) +++
                    ['dummy-block-victim-2]) ; Critical victims
            object-to-clone (eval/deref-field field rootobj :normal)
            ;;_ (when (dplev :all) (println "Field=" field "Object-to-clone="))
            ;;_ (pprint object-to-clone)
            newvictim (if object-to-clone (rt/clone-object victim-name object-to-clone))
            [el1 el2 distdiff] (find-victim-evacuation-details x y z blocktype)]

        (add-victim-map-entry unique_id victim-name el1 el2 distdiff blocktype newvictim)

        (when (dplev :all)
          (println "Cloning a new victim:")
          (pprint newvictim))
        (when newvictim    ; fill fields of the cloned object
          (reset! (rt/get-field-atom newvictim 'tl-x) x) (reset! (rt/get-field-atom newvictim 'br-x) x)
          (reset! (rt/get-field-atom newvictim 'tl-z) y) (reset! (rt/get-field-atom newvictim 'br-z) y)
          (reset! (rt/get-field-atom newvictim 'tl-y) z) (reset! (rt/get-field-atom newvictim 'br-y) z)
          (reset! (rt/get-field-atom newvictim 'v-name) (clojure.string/replace victim-name #"-" " "))
          (when (= (symbol blocktype) 'block_victim_1b)
            (reset! (rt/get-field-atom newvictim 'model-state) :good-b)) ; Maybe replace :good and :good-b with  :normal and :normal-b
          (when (dplev :all) (println "Added new victim" victim-name "blocktype=" blocktype "in room" room-name))
          (bs/set-belief-in-variable victim-name :awaiting-triage 1.0)
          (when (dplev :victims :all) (pprint newvictim)))))
    (establish-volumes)               ; recalculate volume occupancy
    (doseq [ahypothesis (mphyp/get-plan-hypotheses)] ; force an initial plan to be published for all hypotheses
      (let [id (mphyp/get-plan-hypo-id ahypothesis)]
        (when (dplev :planner :all) (println "Requesting new plan for" id))
        (plan/enqueue-new-plan-request-reason id)))
    nil))

(def ^:dynamic *rita-initialized* false)

;;; Fin
