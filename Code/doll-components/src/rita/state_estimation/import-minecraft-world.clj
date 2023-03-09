;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.import-minecraft-world
  "RITA Import Minecraft World."
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
            [rita.common.core :as rc :refer :all]
            ;[pamela.cli :as pcli]
            ;[pamela.tpn :as tpn]
            ;[pamela.unparser :as pup]
            ;[pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
            ;[pamela.tools.belief-state-planner.montecarloplanner :as bs]
            ;[pamela.tools.belief-state-planner.ir-extraction :as irx]
            [clojure.java.io :as io])
  (:gen-class)) ;; required for uberjar

#_(in-ns 'rita.state-estimation.import-minecraft-world)

;;; Removes air, converts coordinates into numbers and calculates the minimum and maximum
;;; of each dimension
(defn import-world
  [json-map]
  (let [pairs (into [] json-map)]
    (loop [data (filter (fn [item] (not (= (second item) "air"))) pairs)
           maxx -10000 minx 100000 maxy -10000 miny 100000 maxz -10000 minz 100000
           retained {}]
      (let [[key val] (first data)]
        (if (not (empty? data))
          (let [coordinates (into [] (map read-string (clojure.string/split key #",")))
                [x z y] coordinates]
            (recur (rest data)
                   (Math/max maxx x) (Math/min minx x)
                   (Math/max maxy y) (Math/min miny y)
                   (Math/max maxz z) (Math/min minz z)
                   (conj retained {coordinates (keyword val)})))
          {:minx minx :maxx maxx :miny miny :maxy maxy :minz minz :maxz maxz :data retained})))))

(defn import-world-from-file
  [fn]
  (let [json (and fn (.exists (io/file fn)) (json/read-str (slurp fn)))]
    (if (not json)
      (println "File not found: " fn)
      (import-world json))))

(defn world-dimensions
  [world]
  [[(:minx world) (:maxx world)]
   [(:minz world) (:maxz world)]
   [(:miny world) (:maxy world)]])

(defn world-size
  [world]
  [(+ 1 (- (:maxx world) (:minx world)))
   (+ 1 (- (:maxz world) (:minz world)))
   (+ 1 (- (:maxy world) (:miny world)))])

(defn world-object-types
  [world]
  (set (map (fn [[dims val]] val) (:data world))))

;;; The order of coordinates is [x z y] we organize storage as [x y z]
;;; Sorting puts things ordered by x and y, which is useful for grouping.
(defn compute-index
  [world objcoords]
  (let [[xsize zsize ysize] (world-size world)
        mins (map first (world-dimensions world))
        adjcoords (map - objcoords mins)
        [x z y] adjcoords]
    (+ z (* y zsize) (* x zsize ysize))))

(defn compute-mda-index
  [world objcoords]
  (let [[xsize zsize ysize] (world-size world)
        mins (map first (world-dimensions world))
        adjcoords (map - objcoords mins)]
    ;;(println "size= [" xsize zsize ysize "] adjcoords=" adjcoords)
    adjcoords))

(defn get-world-objects-of-type
  [world type]
  (let [cf (fn [x y] (> (compute-index world x) (compute-index world y)))]
    (sort-by first cf (filter (fn [item] (= (second item) type)) (:data world)))))

(defn distance-between-voxels
  [world v0 v1]
  ;(println "v0=" v0 "v1=" v1)
  (let [[x0 z0 y0] (first v0)
        [x1 z1 y1] (first v1)
        dist (Math/sqrt (+ (* (- x1 x0) (- x1 x0))
                           (* (- z1 z0) (- z1 z0))
                           (* (- y1 y0) (- y1 y0))))]
    dist))

(defn door-finder-aux
  [world voxels]
  (loop [doorparts [(first voxels)]
         unusedparts (rest voxels)]
    (if (or (empty? unusedparts)
            (>= (distance-between-voxels world (last doorparts) (first unusedparts)) 2))
      [(map first doorparts) unusedparts]
      (recur (conj doorparts (first unusedparts)) (rest unusedparts)))))

(defn door-finder
  [world]
  (let [doorvoxels (get-world-objects-of-type world :wooden_door)]
    (if (not (empty? doorvoxels))
      (loop [parts []
             unused doorvoxels]
        (if (not (empty? unused))
          (let [[part remaining] (door-finder-aux world unused)]
            (recur (conj parts part) remaining))
          parts)))))

(def type-char-map
  {
   :anvil \a
   :bookshelf \B
   :cauldron \O
   :chest \c
   :clay \C
   :cobblestone \4
   :cobblestone_wall \W
   :crafting_table \t
   :dispenser \d
   :dropper \5
   :end_portal_frame \E
   :fire \F
   :flower_pot \P
   :furnace \f
   :glass_pane \|
   :gravel \g
   :heavy_weighted_pressure_plate \H
   :hopper \7
   :iron_bars \I
   :iron_block \i
   :ladder \L
   :lever \l
   :lit_redstone_lamp \8
   :monster_egg \e
   :nether_brick \b
   :nether_brick_stairs \N
   :oak_stairs \O
   :piston_head \h
   :quartz_block \Q
   :quartz_stairs \q
   :redstone_block \R
   :redstone_torch \r
   :redstone_wire \W
   :snow \o
   :stained_hardened_clay \S
   :sticky_piston \k
   :stone_button \3
   :stone_slab \s
   :tripwire_hook \6
   :unlit_redstone_torch \u
   :unpowered_repeater \2
   :wall_sign \w
   :wooden_button \b
   :wooden_door \D
   :wool \9
   })

(def ^:dynamic *objects-of-interest* {})

(defn find-objects-of-interest
  [world]
  (let [found-ooi (into {} (map (fn [[kw nme]]
                                  (println "looking for objects of type" kw "(" nme ")")
                                  {kw (get-world-objects-of-type world kw)})
                                objects-of-interest))]
    (def ^:dynamic *objects-of-interest* found-ooi)))

(defn print-object-of-interest-as-pamela-constructors[]
  (doseq [[kw soo] *objects-of-interest*]
    (let [pclassname (str "minecraft-object-" (name kw))]
      (println (str "(defpclass " pclassname " [iblx ibly iblz itrx itry itrz vname] :inherit [RectangularVolume])"))))
  (println)
  (println "(defpclass minecraft-objects []")
  (println "  :fields {")
  (doseq [[kw soo] *objects-of-interest*]
    (let [constructor-name (str "minecraft-object-" (name kw))]
      (println "           ;; Minecraft objects of type " kw)
      (doseq [[[x y z] otn] soo]
        (println (str "           " (name (gensym (name kw)))) (str "(" constructor-name) x y z x y z (str "\"" (name kw) "\")")))))
  (println "          })"))

(defn make-minecraft-array
  [world]
  (let [dims (world-size world)
        array (apply make-array Character/TYPE dims)]
    (dotimes [x (nth dims 0)]
      (dotimes [z (nth dims 1)]
        (dotimes [y (nth dims 2)]
          (aset-char array x z y \ ))))
    (doseq [voxel (:data world)]
      (let [[x z y] (compute-mda-index world (first voxel))
            val (get type-char-map (second voxel))]
        (aset-char array x z y val)))
    array))

(defn print-world
  [world mca]
  (let [[sx sz sy] (world-size world)
        [dx dz dy] (world-dimensions world)]
    (println "There are" sz "levels from" (first dz) "to" (second dz)
             "startx (left)=" (first dx) "starty (bottom)=" (first dy))
    (println)
    (dotimes [level sz]
      (println "Level " (+ level (first dz)))
      (println)
      (dotimes [yline sy]
        (let [y (- (- sy 1) yline)]
          (dotimes [x sx]
            ;;(print \ )
            (print (aget mca x level y)))
          (println))))))

(defn print-world-wide
  [world mca]
  (let [[sx sz sy] (world-size world)
        [dx dz dy] (world-dimensions world)]
    (println "There are" sz "levels from" (first dz) "to" (second dz)
             "startx (left)=" (first dx) "starty (bottom)=" (first dy))
    (println)
    (dotimes [level sz]
      (println "Level " (+ level (first dz)))
      (println)
      (dotimes [yline sy]
        (let [y (- (- sy 1) yline)]
          (dotimes [x sx]
            (if (= \S (aget mca x level y))
              (do (print \[)
                  ;;(print (aget mca x level y))
                  (print \]))
              (do (print \ ) (print \ ))))
          (println))))))


;;; Fin

;; local testing - delete later +++
;; (def world (import-world-from-file  "/Users/paulr/checkouts/bitbucket/asist_rita/Code/data/TB2-blocks_in_building.json"))
;;; (def w-dimensions (world-dimensions world))
;;; (def w-size (world-size world))
;;; (def w-types (world-object-types world))
;;; (def w-doors (get-world-objects-of-type world :wooden_door))
;;; (def doors (door-finder world))
;;; (def mca (make-minecraft-array world))
;;; (print-world world mca)
;;; (find-objects-of-interest world)
;;; (print-object-of-interest-as-pamela-constructors)
