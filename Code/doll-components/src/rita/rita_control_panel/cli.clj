;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.rita-control-panel.cli
  "RITA AttackModelGenerator main."
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
            [clojure.java.io :as io]
            [pamela.cli :as pcli]
            [pamela.tpn :as tpn]
            [pamela.unparser :as pup]
            [pamela.parser :as parser]
            [pamela.utils :as putils]
            [rita.common.core :as rc :refer :all]
            [rita.common.surveys :as surveys]
            [pamela.tools.plant.interface :as pi]
            [pamela.tools.plant.connection :as pc]
            ;; [pamela.tools.utils.rabbitmq :as rmq] ;; TODO: deal with this redundancy!
            [pamela.tools.utils.tpn-json :as tpn-json]
            [clojure.java.io :as io])
  (:gen-class)) ;; required for uberjar

;; TODO: Consider whether we should only publish data for only the subject(s) in the
;; current trial.  If so, we would subscribe to `testbed-message`s, and look for trial/start
;; messages for its declaration of the subject(s).
;; This would be lots of overhead, when we'd be interested in only ONE testbed message!
;; TODO: If we make this survey data publishing available to other ASIST components,
;;       we'll need to not only change the message communication to MQT (vs (RMQ), but we'll
;;       also want to change the form of the data.  E.g., for a given subject ID, publish each
;;       of the surveys as a JSON object containing the individual questions and aggregates.
(defn publish-survey-data [channel mission-id]
  (let [subjects (surveys/all-subject-ids)]
    (doseq [subject-id subjects]
      (let [survey-data (surveys/survey-data-for-subject subject-id)
            spatial-observations (conj
                                  (vec (map (fn [[question-id _] index]
                                              {:field (str "spatial-ability-question-" index)
                                               :value (get survey-data question-id)})
                                            (surveys/spatial-survey-question-ids)
                                            (range 1 (+ 1 (count (surveys/spatial-survey-question-ids))))))
                                  {:field "spatial-ability-aggregate-score"
                                   :value (surveys/spatial-survey-aggregate-score subject-id)})
            satisficing-observations (conj
                                  (vec (map (fn [[question-id _] index]
                                              {:field (str "satisficing-question-" index)
                                               :value (get survey-data question-id)})
                                            (surveys/satisficing-survey-question-ids)
                                            (range 1 (+ 1 (count (surveys/satisficing-survey-question-ids))))))
                                  {:field "satisficing-aggregate-score"
                                   :value (surveys/satisficing-survey-aggregate-score subject-id)})
            observations (conj
                          (into [] (concat spatial-observations satisficing-observations))
                          {:field "subject-id"
                           :value subject-id})]
        ;;(println observations)
        (publish-message channel mission-id "observations"
                         {:plant-id "plant"
                          :state :observations
                          :observations observations})))))

;;; This version does a function call to Pamela's build-model
(defn compile-pamela-string-to-ir-json2
  [pamela-string]
  (let [tofile (java.io.File/createTempFile "pamela-source" ".pamela")
        tifile (java.io.File/createTempFile "pamela-json" ".json")
        ]
    ;; Write the pamela text to a file
    (with-open [ostrm (java.io.OutputStreamWriter.(java.io.FileOutputStream. tofile) "UTF-8")]
      (.write ostrm pamela-string)
      (.write ostrm "\n"))
    ;; Compile the pamela file
    (pcli/build-model {:input [tofile]
                       :output tifile
                       :json-ir true})
    ;;The thing we publish is a stringified JSON (not a Clojure map)
    #_(json/read-str (slurp tifile))
    (slurp tifile)))

(defn pamela-string-to-ir-json [pamela-string]
  (compile-pamela-string-to-ir-json2 pamela-string))

;; This is an example handler used only to test RITA's inter-component plumbling
;; It should be replaced with somethig real
;; (defcondition test-message-observed [test-message] [ch]
;;   )


;; (defmain -main 'RITAControlPanel
;;   ;;Optionally add some initialization code here
;;   )

(def current-connection-info
  "The current RMQ connection information"
  (atom nil))

(defn shutdown []
  (rmq/close-connection (:connection @current-connection-info))
  (reset! current-connection-info {}))


(defn -main [& args]
  (let [parsed (cli/parse-opts args cli-options)
        opts (:options parsed)
        _ (pprint opts)
        help (:help opts)
        exchange (:exchange opts)
        opts-for-rmq (dissoc opts :model :dp) ;cleanup irrelevant opts
        conn-info (rmq/make-channel exchange opts-for-rmq)
        config 'RITAControlPanel
        ]

    (when (:errors parsed)
      (print-help (:summary parsed))
      (println (string/join \newline (:errors parsed)))
      (exit 0))

    (when help
      (print-help (:summary parsed))
      (exit 0))

    ;; (println "rabbitmq-keys-and-agents:")
    ;; (pprint (rabbitmq-keys-and-agents))

    (reset! current-connection-info conn-info)
    (reset! current-exchange exchange)
    (reset! current-app-id (str config))
    (println "Setting up subscriptions")
    (doseq [routing-key-symbol (subscriptions-for-component config)]
      (println "Subscribing to:" routing-key-symbol)
      (rmq/make-subscription (str routing-key-symbol) subscription-handler
                             (:channel conn-info) exchange))

    (let [routing-key "startup-rita"
          ;; appdir (application-directory)
          ;; _ (assert appdir "APPDIR is not set")
          mission-id (gensym "mission")
          ]
      (Thread/sleep 10000)
      #_(publish-message (:channel conn-info) mission-id routing-key
                       {})

      (Thread/sleep 2000)
      (publish-survey-data (:channel conn-info) mission-id)

      (Thread/sleep 2000)
      (let [pamela-filename (io/resource (str "public/" "test-environment.pamela"))
            _ (assert pamela-filename)
            pamela-model (slurp pamela-filename)
            model-in-ir-json (pamela-string-to-ir-json pamela-model)
            ]
        #_(publish-message (:channel conn-info) mission-id "mission-pamela"
                         {:mission-pamela pamela-model
                          :mission-ir-json model-in-ir-json})
        #_(publish-message (:channel conn-info) mission-id "mission-solver"
                         {:mission-solver {}}))

                                        ; Cleanup previous connection
      #_(when conn-info
          (rmq/close-connection (:connection conn-info)))

      (println "App State")
      (println "----------")
      (clojure.pprint/pprint conn-info)
      (println "----------"))))
