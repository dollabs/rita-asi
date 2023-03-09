(ns init-encoded-map.core
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])
  (:gen-class))

(defn get-trialID-memberID
  "Function info"
  [elems type]
  (if (or (= type "Trial") (= type "Member")) 
    (Integer. (apply str (remove nil? (for [elem elems] 
                   (if (str/starts-with? elem type)
                     (get (str/split elem #"-") 1))))))
    (do
      (println(str "The type" type "does not exist"))
      ("n/a"))))  


(defn initial-info-filter
  "Function info"
  [list-files]
  (into [] (for [file-name list-files] 
            {:fileName file-name
                          :trialID (get-trialID-memberID(str/split file-name #"_") "Trial") 
                          :participantID (get-trialID-memberID(str/split file-name #"_") "Member") 
                          :plan [[:Lobby]]
                          :unvisitedAreas []})))

(defn -main 
    [& args]
    (if (nil? (first args))
       (do
            (println "No path initalized, using the default directory. . .")
            (def file-path "./resources/pretrial_maps"))
        (def file-path (first args)))

    (def file-info #{})

    (if (.isDirectory (io/file file-path))
        (do
            (def list-of-files (.list (io/file file-path)))
            (def aVector (initial-info-filter list-of-files))
            (spit "init-encoded-maps.clj" aVector))

        (println "No directory found")))
