;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.


(ns rita.prediction-generator.mdb
  "RITA mongo db helper ."
  (:require [monger.core :as mc]
            [monger.collection :as mcol]
            [monger.query :as mq]

            [pamela.tools.utils.util :as pt-utils]
    ;[clojure.tools.cli :as cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [clojure.pprint :refer :all]))
    ;[clojure.data.codec.base64 :as base64]
    ;[clojure.string :as string]
    ;[clojure.pprint :as pp :refer [pprint]]
    ;[me.raynes.fs :as fs]
    ;[clojure.tools.logging :as log]
    ;[environ.core :refer [env]]
    ;[mbroker.rabbitmq :as rmq]
    ;[clojure.java.shell :as shell]
    ;[pamela.cli :as pcli]
    ;[pamela.tpn :as tpn]
    ;[pamela.unparser :as pup]
    ;[rita.common.core :as rc :refer :all]
    ;; [rita.generative-planner.generative-planner :as amg :refer :all]
    ;; [rita.generative-planner.desirable-properties :as dp :refer :all]
    ;; [pamela.tools.belief-state-planner.runtimemodel :as rt :refer :all]
    ;; [pamela.tools.belief-state-planner.montecarloplanner :as bs]
    ;; [pamela.tools.belief-state-planner.ir-extraction :as irx]

    ;[clojure.java.io :as io]


  ;(:gen-class)


(defonce mdb-connection nil)
; database name
(defonce db-name "rita-db")
; table name
(defonce predictions-table-name "predictions")
(defonce probability-table-name "probability")

(defn set-db-name [name]
  (if name (def db-name name)))

(defn- make-connection!
  "Connection wrapper with caching connection"
  [host port]
  (println "make-connection!" host port)
  (let [opts {}
        opts (when host (merge opts {:host host}))
        opts (if port (merge opts {:port port})
                      (merge opts {:port 27017}))]
    (println "Mongo connections opts" opts)
    ;(def mdb-connection (mc/connect opts))
    (def mdb-connection (:conn (mc/connect-via-uri (str "mongodb://" host "/blah"))))))

(defn make-connection [& {:keys [host port]}]
  ;(println "make-connection" host port)
  (if (nil? mdb-connection)
    (make-connection! host port)
    (binding [*out* *err*]
      (println "Mongodb already connected !!"))))

(defn get-connection []
  (if-not mdb-connection
    (binding [*out* *err*]
      (println "Mongo db connection is nil"))
    mdb-connection))

(defn disconnect []
  (when (get-connection)
    (mc/disconnect (get-connection))
    (def mdb-connection nil)))

(defn get-db []
  (when (and (get-connection) db-name)
    (mc/get-db (get-connection) db-name)))

(defn get-predictions-table []
  predictions-table-name)

(defn get-probability-table []
  probability-table-name)

(defn clear-predictions []
  (mcol/remove (get-db) (get-predictions-table)))

(defn clear-probability []
  (mcol/remove (get-db) (get-probability-table)))

(defn get-predictions []
  (mcol/find-maps (get-db) (get-predictions-table)))

(defn get-predictions-by-mission-id [mission-id]
  (filter #(= mission-id (:mission-id %1))
       (mcol/find-maps (get-db) (get-predictions-table))))

(defn get-probability []
  (mcol/find-maps (get-db) (get-probability-table)))

(defn update-probability [vals]
  (let [db (get-db)]
    (when db
      (doseq [[k v] vals]
        (println k)
        (println v)
        (println "exists?\n" (mcol/find-map-by-id (get-db) (get-probability-table) k))
        (println)
        (if (nil? (mcol/find-map-by-id (get-db) (get-probability-table) k))
          (mcol/insert (get-db) (get-probability-table) (conj v {:_id k}))
          (mcol/update-by-id db (get-probability-table) k v))))))

(defn insert-one [document]
  ;(println "inserting")
  ;(pprint document)
  (let [db (get-db)]
    (when db
      (mcol/insert db (get-predictions-table) document))))

(defn update-one [document]
  ;(println "updating")
  ;(pprint document)
  (let [db (get-db)]
    (when db
      (mcol/update-by-id db (get-predictions-table) (:_id document) document))))

(defn insert-batch
  "Inserts given list of docs into the DB"
  [docs]
  ;(println "insert-batch" db collection docs)
  (let [db (get-db)]
    (when db
      (mcol/insert-batch db (get-predictions-table) docs))))

(defn count-predictions []
  (let [db (get-db)]
    (when db
      (mcol/count db (get-predictions-table)))))

(defn find-by-id [id]
  {:pre [(not (nil? id))]}
  (mcol/find-map-by-id (get-db) (get-predictions-table) id))

(defn get-data-from-file []
  (reduce (fn [res str-line]
            (println str-line)
            (conj res (json/read-str str-line :key-fn keyword)))
          [] (pt-utils/read-lines "../data/predictions.csv.1_col.csv")))
