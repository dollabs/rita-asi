(ns rita.common.config
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]))

(defonce config {:agent-name "Rita_Agent"
                 :rita-version "rita-unversioned"})

(defn read-config-from-resource []
  (when config
    (println "Overwriting previous config")
    (pprint config))
  (let [fname (io/resource (str "public/" "config.json"))]
    (def config (with-open [rdr (clojure.java.io/reader fname)]
                  (json/read rdr
                             :key-fn #(keyword %)))))
  (println "Read Config")
  (pprint config))

(defn get-rita-version []
  (:rita-version config))

(defn get-agent-name []
  (:agent-name config))

