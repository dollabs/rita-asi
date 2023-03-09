;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(ns rita.state-estimation.secoredata
  "RITA State Estimation Core Data."
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
            [pamela.tools.belief-state-planner.coredata :as global]
;            [rita.common.core :as rc :refer :all]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [rand rand-int rand-nth])
  (:gen-class)) ;; required for uberjar

#_(in-ns 'rita.state-estimation.secoredata)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Control of runtime printouts

(def ^:dynamic *debug-level* #{:demo :error :warn :io :interventions  :chat :story}) ; If you want everything add ":all" for apsp :apsp  :prediction :speech

(defn dplev
  [& x]
  (some *debug-level* x))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Reportouts of statistics for analysis

(def ^:dynamic *last-epoch-reported* -1)

(defn reset-last-epoch-reported
  []
  (def ^:dynamic *last-epoch-reported* -1))

(defn get-last-epoch-reported
  []
  *last-epoch-reported*)

(defn set-last-epoch-reported
  [epoch]
  (def ^:dynamic *last-epoch-reported* epoch))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Map name (study 2)

(def ^:dynamic *estimated-map* "Saturn")

(defn set-estimated-map-name
  [mapname]
  (def ^:dynamic *estimated-map* mapname))

(defn get-estimated-map-name
  []
  *estimated-map*)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; For statistical learning

(def ^:dynamic *stat-record* nil)                                ; This is the record of the currently running trial statistics
(def ^:dynamic *default-learned-model-path* "../data/model.lpm") ; This is where we look for learned model by default (offline)
(def ^:dynamic *loaded-learned-model* nil)                       ; This is the loaded learned model, or nil if none.
(def ^:dynamic *learned-participant-model* "../data/default.lpm");
(def ^:dynamic *rita-temporal-data* nil)                         ;
(def ^:dynamic *player-name-map* nil)                            ; Map between player name and participant_id
(def ^:dynamic *player-callsign-map* nil)                        ; Map from callsign to participant-id
(def ^:dynamic *teammembers* [])
(def ^:dynamic *team-score-at* [0 0])
(def ^:dynamic *target-score-sd* [386 78])

(defn verify-data-directory-exists []
  (let [data-dir "../data"]
    (when (and (not (fs/directory? data-dir))
               (dplev :all :error))
      (println "Error: Data directory (" data-dir ") doesn't exist.")
      (println "       You are likely running RITA from the wrong directory."))))

(def ^:dynamic *current-role-strategy* :allroles)

(def ^:dynamic numplayers 0)
(def ^:dynamci maxplayers 6)
(def ^:dynamic playermap {:Ed "agentBeliefState.observer",
                          :Rita "agentBeliefState.rita",
                          :Robot1 "agentBeliefState.robot1"})

;;; Moved from rita-se-core

(def ^:dynamic *current-ms-time* 0)
(def ^:dynamic *mission-started* false)
(def ^:dynamic *mission-terminated* nil)
(def ^:dynamic *mission-started-time* nil)
(def ^:dynamic *mission-ended-time* nil)
(def ^:dynamic *new-plan-request* [])
(def ^:dynamic *last-beeped-room* nil)
(def ^:dynamic *auto-periodic-replan-frequency* nil)
(def ^:dynamic *se-predictions* {})
(def ^:dynamic *se-successful-predictions* {})
(def ^:dynamic *rooms-entered* [])
(def ^:dynamic *mission-ended-message-sent* false)

;;; For participant performance
(def ^:dynamic *participant-strength* {})
(def ^:dynamic *participant-performance-history* {})
(def ^:dynamic *participant-strength-file* (str "../runtime-learned-models/" "expt" ".known-participants.edn"))
(def ^:dynamic *training-data-file* (str "../data/" "training.data.edn"))
(def ^:dynamic *participant-first-trial-map* {})

;;; Study-3 version
(def ^:dynamic *team-strength-data-base* {})

(defn reset-player-strength-data
  []
  (def ^:dynamic *team-strength-data-base* {}))

(defn get-player-strength-data
  []
  *team-strength-data-base*)

(defn set-player-strength-data
  [newdata]
  (def ^:dynamic *team-strength-data-base* newdata))

(def ^:dynamic *building-model-apsp-pathname* "sparky")

;;; Keep track of messages received.
(def ^:dynamic *message-counts* {})
(def ^:dynamic *message-occurrence-order* [])
(def ^:dynamic *total-messages-received* 0)

(def ^:dynamic *msgs-for-rmq-and-mqtt* [])

;;; Queue of messages for both RMQ and MQTT
(defn get-messages-for-rmq-and-mqtt
  []
  *msgs-for-rmq-and-mqtt*)

(defn add-message-for-rmq-and-mqtt
  [newmsg]
  (def ^:dynamic *msgs-for-rmq-and-mqtt* (conj *msgs-for-rmq-and-mqtt* newmsg))
  (println "messages queued up for mqtt-message"))

(defn reset-messages-for-rmq-and-mqtt
  []
  (def ^:dynamic *msgs-for-rmq-and-mqtt* []))

(defn msgs-for-rmq-and-mqtt?
  []
  (not (empty? *msgs-for-rmq-and-mqtt*)))

(defn get-and-reset-msgs-for-rmq-and-mqtt
  []
  (if (msgs-for-rmq-and-mqtt?)
    (let [msgs (get-messages-for-rmq-and-mqtt)]
      (reset-messages-for-rmq-and-mqtt)
      msgs)))

;;; Count of total number of messages received
(defn reset-total-messages-received
  []
  (def ^:dynamic *total-messages-received* 0))

(defn message-received
  []
  (def ^:dynamic *total-messages-received* (+ *total-messages-received* 1)))

(defn get-total-messages-received
  []
  *total-messages-received*)

;;; Count the number of messages of type/subtype received

(defn get-message-count
  []
  *message-counts*)

(defn reset-message-count
  []
  (def ^:dynamic *message-counts* {}))

(defn set-message-count
  [newcount]
  (def ^:dynamic *message-counts* newcount))

;;; Make a list of the message types received in the order of their first occurrence.
(defn get-message-occurrence-order
  []
  *message-occurrence-order*)

(defn reset-message-occurrence-order
  []
  (def ^:dynamic *message-occurrence-order* []))

(defn add-message-occurrence
  [newitem]
  (def ^:dynamic *message-occurrence-order* (conj *message-occurrence-order* newitem)))

(def ^:dynamic *debugsave* nil)

(defn debug-save
  [val]
  (def ^:dynamic *debugsave* val))

(defn debug-get
  []
  *debugsave*)

;;; APSP

(defn get-building-model-apsp-pathname
  []
  *building-model-apsp-pathname*)

(defn set-building-model-apsp-pathname
  [bmodnam]
  (def ^:dynamic *building-model-apsp-pathname* bmodnam))

(def ^:dynamic *room-apsp* nil)

(defn get-room-apsp
  []
  *room-apsp*)

(defn set-room-apsp
  [new-room-apsp]
  (def ^:dynamic *room-apsp* new-room-apsp))

(defn get-participant-first-trial-map
  []
  *participant-first-trial-map*)

(defn set-participant-first-trial-map
  [ftm]
  *participant-first-trial-map*)

(defn participant-first-trial?
  [pid]
  (get *participant-first-trial-map* pid true))

(defn all-participants-first-trial?
  []
  (every? not (vals *participant-first-trial-map*)))

(defn all-participants-non-first-trial?
  []
  (every? identity (vals *participant-first-trial-map*)))

(defn mixed-participant-history?
  []
  (and (not (all-participants-first-trial?))
       (not (all-participants-non-first-trial?))))

(defn set-last-beeped-room
  [nuval]
  (def ^:dynamic *last-beeped-room* nuval))

(defn set-mission-started
  [nuval]
  (def ^:dynamic *mission-started* nuval))

(defn get-mission-started
  []
  *mission-started*)

(defn set-mission-started-time
  [nuval]
  (def ^:dynamic *mission-started-time* nuval))

(defn set-mission-ended-message-sent
  [nuval]
  (def ^:dynamic *mission-ended-message-sent* nuval))

(defn get-mission-terminated
  []
  *mission-terminated*)

(defn set-mission-terminated
  [nuval]
  (def ^:dynamic *mission-terminated* nuval))

(defn get-mission-ended-time
  []
  *mission-ended-time*)

(defn set-mission-ended-time
  [nuval]
  (def ^:dynamic *mission-ended-time* nuval))

(defn mission-in-progress
  []
  (or (not *mission-started*) *mission-terminated*))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Last MS time observed in a message

(def ^:dynamic *last-observed-ms-time* 0)

(defn reset-last-ms-time
  []
  (def ^:dynamic *last-observed-ms-time* 0))

(defn get-last-ms-time
  []
  *last-observed-ms-time*)

(defn update-last-ms-time
  [latest]
  (def ^:dynamic *last-observed-ms-time* latest))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Leadership votes

(def ^:dynamic *leadership-votes* {})

(defn reset-leadership-votes
  []
  (def ^:dynamic *leadership-votes* {}))

(defn get-leadership-votes
  []
  *leadership-votes*)

(defn set-leadership-votes
  [lvs]
  (def ^:dynamic *leadership-votes* lvs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Cognitive load votes

(def ^:dynamic *cogload-votes* {})

(defn reset-cogload-votes
  []
  (def ^:dynamic *cogload-votes* {}))

(defn get-cogload-votes
  []
  *cogload-votes*)

(defn set-cogload-votes
  [lvs]
  (def ^:dynamic *cogload-votes* lvs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Participant Strength

(defn reset-player-performance-histories
  []
  (def ^:dynamic *participant-performance-history* {}))

(defn get-player-performance-histories
  []
  *participant-performance-history*)

(defn set-player-performance-histories
  [pfh]
  (def ^:dynamic *participant-performance-history* pfh))

(defn get-player-performance-history
  [pid & [default]]
  (get *participant-performance-history* pid (if default (first default) [])))

(defn get-participant-strength-data
  []
  *participant-strength*)

(defn set-participant-strength-data
  [psd]
  (def ^:dynamic *participant-strength* psd))

(defn set-participant-strength
  [pid strength]
  (set-participant-strength-data
   (merge (get-participant-strength-data) {(keyword pid) strength})))

;;  (def ^:dynamic *participant-strength*
;;    (merge *participant-strength* {pid strength})))

(defn get-participant-strength
  [participant-id & [default]]
  (get (get-participant-strength-data)
       (keyword participant-id)
       (if default (first default) 0.0))) ; Assume that unknown participant is average

(defn get-participant-strength-file
  []
  *participant-strength-file*)

(defn set-participant-strength-file
  [psf]
  (def ^:dynamic *participant-strength-file* psf))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Intervention count

(def ^:dynamic *interventions-given* 0)

(defn get-interventions-given
  []
  *interventions-given*)

(defn set-interventions-given
  [n]
  (def ^:dynamic *interventions-given* n))

(defn reset-interventions-given
  []
  (set-interventions-given 0))

(defn inc-interventions-given
  []
  (def ^:dynamic *interventions-given* (+ *interventions-given* 1)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Training data

(def ^:dynamic *training-data* {})
(def ^:dynamic *mean-data* {})
(def ^:dynamic *sd-data* {})

(defn reset-training-data
  []
  (def ^:dynamic *training-data* {})
  (def ^:dynamic *mean-data* {})
  (def ^:dynamic *sd-data* {}))

(defn get-training-data-data
  []
  *training-data*)

(defn set-training-data-data
  [tdd]
  (def ^:dynamic *training-data* tdd))

(defn get-learned-sd-data
  []
  *sd-data*)

(defn set-learned-sd-data
  [lsd]
  (def ^:dynamic *sd-data* lsd))

(defn get-learned-mean-data
  []
  *mean-data*)

(defn set-learned-mean-data
  [lmd]
  (def ^:dynamic *mean-data* lmd))

(defn get-training-data-file
  []
  *training-data-file*)

(defn set-training-data-file
  [tdf]
  (def ^:dynamic *training-data-file* tdf))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Planning

(defn get-auto-periodic-replan-frequency
  []
  *auto-periodic-replan-frequency*)

(defn set-auto-periodic-replan-frequency
  [pf]
  (def ^:dynamic *auto-periodic-replan-frequency* pf))

(defn get-new-plan-request
  []
  *new-plan-request*)

(defn set-new-plan-request
  [npr]
  (def ^:dynamic *new-plan-request* npr))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Romes entered


(defn room-entered?
  [aroom]
  (some #{aroom} *rooms-entered*))

(defn register-room-entered
  [aroom]
  (if (not (room-entered? aroom))
    (def ^:dynamic *rooms-entered* (conj *rooms-entered* aroom))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *trial-id* nil)
(def ^:dynamic *experiment-id* nil)

(defn set-trial-id
  [tid]
  (def ^:dynamic *trial-id* tid))

(defn get-trial-id
  []
  *trial-id*)

(defn set-experiment-id!
  [tid]
  (def ^:dynamic *experiment-id* tid))

(defn get-experiment-id
  []
  *experiment-id*)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Rita MS time

(def clocklock (Object.))

(defn rita-ms-time
  []
  (locking clocklock
    *current-ms-time*))

(defn set-rita-ms-time
  [nutime]
  (locking clocklock
    (def ^:dynamic *current-ms-time* nutime)
    nutime))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Room beeps

(defn get-last-beeped-room
  []
  *last-beeped-room*)

(defn set-last-beeped-room
  [val]
  (def ^:dynamic *last-beeped-room* val))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Predictions

(defn get-se-predictions
  []
  *se-predictions*)

(defn set-se-predictions
  [predictions]
  (def ^:dynamic *se-predictions* predictions))

(defn get-se-successful-predictions
  []
  *se-successful-predictions*)

(defn set-se-successful-predictions
  [predictions]
  (def ^:dynamic *se-successful-predictions* predictions))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Control of AC observations that are being observed

(def ^:dynamic *acwarnings-given* {})

(defn warn-ignoring-message-one-time
  [acid]
  (when (not (get *acwarnings-given* acid))
    (println "Message from " acid "received.  We are ignoring data from this AC")
    (def ^:dynamic *acwarnings-given* (merge *acwarnings-given* {acid :warned})))
  nil)

;; these are the default values for the study-3 runs

(def ^:dynamic *ac-observations-being-observed* {})

(defn reset-ac-default-startup-state
  []
  (def ^:dynamic *ac-observations-being-observed*
    {"ac_aptima_ta3_measures"      false,
     "ac_cmu_ta2_beard"            true,
     "ac_cmu_ta2_ted"              true,
     "ac_cornell_ta2_teamtrust"    false,
     "ac_cornell_ta2_asi-facework" false,
     "ac_gallup_ta2_gelp"          true,
     "ac_cmufms_ta2_cognitive"     false,
     "ac_rutgers_ta2_utility"      false,
     "ac_rutgers_utility_ta2"      false,
     "ac_ucf_ta2_playerprofiler"   true,
     "speech_analyzer_agent"       false,
     "ac_ihmc_ta2"                 false}))

;; "ac_cmu_ta2_beard"
;; "ac_cmu_ta2_ted"
;; "ac_cmufms_ta2_cognitive"
;; "ac_cornell_ta2_teamtrust"
;; "ac_gallup_ta2_gelp"
;; "ac_ihmc_ta2"
;; "ac_rutgers_ta2_utility"
;; "ac_rutgers_utility_ta2"
;; "ac_ucf_ta2_playerprofiler"
;; "speech_analyzer_agent"

(defn set-ac-usage
  [usagemap]
  (def ^:dynamic *ac-observations-being-observed* usagemap))

(defn observing-ac
  [acmessagename]
  (let [observing (get *ac-observations-being-observed* acmessagename :not-found)]
    (if (= observing :not-found)
      (if (dplev :all :error :warn)
        (println "ERROR: received AC message " acmessagename "(not in" (keys *ac-observations-being-observed*) ")"))
      observing)))

(defn get-ac-observing-list
  []
  *ac-observations-being-observed*)

(defn print-ac-usage
  []
  (println "AC usage table, we are using the following ACs")
  (pprint (get-ac-observing-list))
  (let [ac-usage (get-ac-observing-list)]
    (doseq [[acname using?] ac-usage]
      (println "ac-name=" acname "using?=" using?)
      (if using? (println acname)))))

(defn reset-players
  []
  (def ^:dynamic numplayers 0)
  (def ^:dynamci maxplayers 6)
  (def ^:dynamic playermap {:Ed "agentBeliefState.observer",
                            :Rita "agentBeliefState.rita",
                            :Robot1 "agentBeliefState.robot1"}))

(defn get-player-object-names
  []
  (let [pnames (vals playermap)
        realplayernames (remove #{(get playermap :Ed)
                                  (get playermap :Rita)
                                  (get playermap :Robot1)}
                                pnames)]
    #_(println "pnames=" (pr-str pnames) "rpnames=" (pr-str realplayernames))
    realplayernames))

(defn get-object-name-from-object-id
  [objid]
  (some #(if (= (val %) objid) (key %)) playermap))

(def m7-ground-truth-map [])
(def m7-ground-truth-map-this-trial [])

(defn set-m7-ground-truth-prompt
  [data]
  (def m7-ground-truth-map data))

(defn select-trial-m7-ground-truth-prompt
  [trial]
  (def m7-ground-truth-map-this-trial
    (into []
          (filter
           (fn [entry] (= (:Trial entry) trial))
           m7-ground-truth-map)))
  (println (count m7-ground-truth-map-this-trial) "prompts found for this trial (" (pr-str trial) ")"))

(def next-m7-prompt false)

(defn get-next-m7-ground-truth-prompt
  [em & [force?]]
  (or (and (not force?) next-m7-prompt (second next-m7-prompt))
      (let [next-prompt (some (fn [gtp]
                                #_(let [setime (get gtp (keyword "Start Elapsed Time"))]
                                  (pprint gtp)
                                  (println "Start Elapsed Time=" (pr-str setime)))
                                (if (> (Integer/parseInt (get gtp (keyword "Start Elapsed Time"))) em) gtp))
                              m7-ground-truth-map-this-trial)
            next-prompt-start-time (if next-prompt (get next-prompt (keyword "Start Elapsed Time")))]
        (if next-prompt (println "Next m7 prompt:" (pr-str next-prompt)))
        (def next-m7-prompt (if (and next-prompt next-prompt-start-time) [(Integer/parseInt next-prompt-start-time) next-prompt]))
        next-prompt)))

(defn set-next-m7-prompt-used!
  []
  (def next-m7-prompt false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *occupiable-space-objects* [])

(defn get-occupiable-space-objects
  []
  *occupiable-space-objects*)

(defn set-occupiable-space-objects
  [nos]
  (def ^:dynamic *occupiable-space-objects* nos))


(def ^:dynamic *victims-in-occupiable-spaces* {})

(defn get-victims-in-occupiable-spaces
  []
  *victims-in-occupiable-spaces*)

(defn set-victims-in-occupiable-spaces
  [vos]
  (def ^:dynamic *victims-in-occupiable-spaces* vos))

(defn reset-victims-in-occupiable-spaces
  []
  (def ^:dynamic *victims-in-occupiable-spaces* {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Victim map

(def victim-map  {})

(defn get-victim-map
  []
  victim-map)

(defn set-victim-map
  [vm]
  (def victim-map  vm))

(defn reset-victim-map
  []
  (def victim-map  {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Don't repeat

(def ^:dynamic *dont-repeat* #{})

(defn get-dont-repeat
  []
  *dont-repeat*)

(defn set-dont-repeat
  [nv]
  (def ^:dynamic *dont-repeat* nv))

(defn dont-repeat
  [& values]
  (if (not (get (get-dont-repeat) values))
    (set-dont-repeat
     (conj (get-dont-repeat) values))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Counters

(def victims-triaged 0)
(def critical-victims-triaged 0)
(def victims-evacuated 0)
(def critical-victims-evacuated 0)

(defn reset-counters
  []
  (def victims-triaged 0)
  (def critical-victims-triaged 0)
  (def victims-evacuated 0)
  (def critical-victims-evacuated 0))

(defn get-victims-triaged
  []
  victims-triaged)

(defn get-critical-victims-triaged
  []
  critical-victims-triaged)

(defn get-victims-evacuated
  []
  victims-evacuated)

(defn get-critical-victims-evacuated
  []
  critical-victims-evacuated)

(defn set-victims-triaged
  [x]
  (def victims-triaged x))

(defn set-critical-victims-triaged
  [x]
  (def critical-victims-triaged x))

(defn set-victims-evacuated
  [x]
  (def victims-evacuated x))

(defn set-critical-victims-evacuated
  [x]
  (def critical-victims-evacuated x))

(defn inc-victims-triaged
  []
  (def victims-triaged (+ victims-triaged 1)))

(defn inc-critical-victims-triaged
  []
  (def critical-victims-triaged (+ critical-victims-triaged 1)))

(defn inc-victims-evacuated
  []
  (def victims-evacuated (+ victims-evacuated 1)))

(defn inc-critical-victims-evacuated
  []
  (def critical-victims-evacuated (+ critical-victims-evacuated 1)))


(defn waiting-to-be-evacuated
  []
  (- (+ victims-triaged critical-victims-triaged)
     (+ victims-evacuated critical-victims-evacuated)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; last room tracking

(def ^:dynamic *last-room-by-participant* {})

(defn reset-last-room
  []
  (def ^:dynamic *last-room-by-participant* {}))

(defn set-last-room!
  [pid aroom]
  (def ^:dynamic *last-room-by-participant* (merge *last-room-by-participant* {pid aroom})))

(defn get-last-room
  [pid]
  (get *last-room-by-participant* pid))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Markers

(def ^:dynamic *placed-markers* [])     ; We don't remove them when they have been removed

(defn reset-placed-markers!
  []
  (def ^:dynamic *placed-markers* []))

(defn get-placed-markers
  []
  *placed-markers*)

(defn set-placed-markers!
  [markers]
  (def ^:dynamic *placed-markers* markers))

(def ^:dynamic *removed-markers* [])

(defn reset-removed-markers!
  []
  (def ^:dynamic *placed-markers* []))

(defn get-removed-markers
  []
  *placed-markers*)

(defn set-removed-markers!
  [markers]
  (def ^:dynamic *placed-markers* markers))

(def ^:dynamic *considered-markers* ())



(defn reset-considered-markers
  []
  (def ^:dynamic *considered-markers* ()))

(defn get-considered-markers
  []
  *considered-markers*)

(defn add-considered-marker!
  [amarker]
  (def ^:dynamic *considered-markers* (conj *considered-markers* amarker)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Header time

(def header-time 1234567)

(defn set-header-time
  [ht]
  (def header-time ht))

(defn get-header-time
  []
  header-time)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Last time (that an intervention was made to a person)

(def ^:dynamic *last-time-addressed* {})

(defn reset-last-time-addressed
  []
  (def ^:dynamic *last-time-addressed* {}))

(defn get-last-time-addressed
  []
  *last-time-addressed*)

(defn set-last-time-addressed
  [nuval]
  (def ^:dynamic *last-time-addressed* nuval))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Strike mode

(def ^:dynamic *strike-mode* false)

(defn get-strike-mode
  []
  *strike-mode*)

(defn set-strike-mode
  [sm]
  (def ^:dynamic *strike-mode* sm))

(defn reset-strike-mode
  []
  (set-strike-mode false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Events that may lead to interventions

(def ^:dynamic *event-log* ())
(def ^:dynamic *event-id-number* 0)
(def ^:dynamic *events-handled* [])

(defn reset-event-log
  []
  (def ^:dynamic *event-log* ())
  (def ^:dynamic *event-id-number* ())
  (def ^:dynamic *events-handled* []))

(defn get-event-log-events
  []
  *event-log*)

(defn set-event-log-events
  [new]
  (def ^:dynamic *event-log* new))

(defn get-event-id-number
  []
  *event-id-number*)

(defn set-event-id-number
  [new]
  (def ^:dynamic *event-id-number* new))

(defn get-events-handled
  []
  *events-handled*)

(defn set-events-handled
  [new]
  (def ^:dynamic *events-handled* new))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Predictions

(def ^:dynamic *predictions* ())

(defn get-predictions
  []
  *predictions*)

(defn set-predictions
  [preds]
  (def ^:dynamic *predictions* preds))


(def ^:dynamic *successful-predictions* ())

(defn set-successful-predictions
  [nuval]
  (def ^:dynamic *successful-predictions* nuval))

(defn get-successful-predictions
  []
  *successful-predictions*)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Stories

(def instantiated-stories [])

(defn get-instantiated-stories
  []
  instantiated-stories)

(defn set-instantiated-stories
  [stories]
  (def instantiated-stories stories))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Roles

(def ^:dynamic *current-role-assignments* {})

(defn reset-role-assignments
  []
  (def ^:dynamic *current-role-assignments* {}))

(defn get-role-assignments
  []
  *current-role-assignments*)

(defn get-roles
  [pid]
  (get *current-role-assignments* pid))

(defn set-roles
  [roles]
  (def ^:dynamic *current-role-assignments* roles))

(defn get-role
  [pid]
  (first (get *current-role-assignments* pid)))

(defn has-role?
  [pid arole]
  (= (first (get *current-role-assignments* pid)) arole))

(defn num-roles
  []
  (count (keys *current-role-assignments*)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Unique IDs

(def uidnum nil)

(defn init-uidnum []
  (set-random-seed! 666)
  (def uidnum (int (+ 10000.0 (* 10000.0 (rand))))))

(init-uidnum)

(defn uidgen []
  (let [uid (str "se" uidnum)]
    (def uidnum (+ uidnum 1))
    uid))

;;; (uidgen)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Roles Assigned

(def roles-assigned {})

(defn get-roles-assigned
  []
  roles-assigned)

(defn reset-roles-assigned
  []
  (def roles-assigned {}))

(defn assign-role!
  [pid role]
  (def roles-assigned (merge roles-assigned {pid role})))

(defn get-assigned-role
  [pid]
  (get roles-assigned pid))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Most recent message

(def ^:dynamic *last-received-message* nil)

(defn set-last-received-message
  [lrm]
  (def ^:dynamic *last-received-message* lrm))

(defn get-last-received-message
  []
  *last-received-message*)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Players

(defn number-of-players
  []
  numplayers)

(defn get-player-object-name
  [playername]
  (let [pon (get playermap (keyword playername) nil)]
    (if (and (not pon) (dplev :all :error :warn))
      (do
        #_(println "ERROR: Failed to find an object for\""
                 playername
                 ", possibilities are the following:")
        #_(pprint playermap)
        nil)
      pon)))

(defn add-player
  [playername]
  (let [kwplayername (keyword playername)
        newplayernumber (+ numplayers 1)
        newid (str "agentBeliefState.participant" newplayernumber)]
    (def ^:dynamic numplayers newplayernumber)
    (if (> newplayernumber maxplayers)
      (do
        (println "*** ERROR more than" maxplayers "players detected, only" maxplayers "currently supported")
        #_(if (= (str playername ":GREEN_ASIST"))
          (+ nil nil))))
    (println "*** New player" playername "added with id" newid)
    (def ^:dynamic playermap (merge playermap {kwplayername newid}))
    newid))

(defn set-role-strategy
  [rstrat]
  (def ^:dynamic *current-role-strategy* rstrat))

(defn get-role-strategy
  []
  *current-role-strategy*)

(def trial-number nil)

(defn set-trial-number!
  [trialno]
  (def trial-number trialno))

(defn get-trial-number
  []
  trial-number)

(def trialID nil)

(defn set-trialID!
  [trialno]
  (def trialID trialno))

(defn get-trialID
  []
  trialID)

(def mission nil)

(defn set-mission!
  [mname]
  (def mission mname))

(defn get-mission
  []
  mission)

(defn translate-mission-name
  [mname]
  (case mname
    "Saturn_A" "SaturnA"
    "Saturn_B" "SaturnB"
    mname))

(defn set-player-name-map!
  [amap]
  (def ^:dynamic *player-name-map* amap))

(defn get-player-name-map
  []
  *player-name-map*)

(defn set-player-callsign-map!
  [amap]
  (def ^:dynamic *player-callsign-map* amap))

(defn get-player-callsign-map
  []
  *player-callsign-map*)

(def ^:dynamic *player-callsign-map* {})

(defn set-pid-callsign-map!
  [pidtocallsign]
  (def ^:dynamic *pid-to-callsign-map* pidtocallsign))

(defn get-pid-to-callsign
  []
  *pid-to-callsign-map*)

(defn pid2callsign
  [pid]
  (get *pid-to-callsign-map* pid pid))

(defn set-team-members!
  [avec]
  (def ^:dynamic *teammembers* avec))

(defn get-team-members
  []
  *teammembers*)

(defn strfromsymbol
  [sym]
  (cond (string? sym) (clojure.string/trim sym)
        (keyword? sym) (clojure.string/trim (subs (str sym) 1))
        (symbol? sym) (clojure.string/trim (str sym))
        :otherwise sym))

(defn get-participant-id-from-player-name
  [pname]
  (or (get *player-name-map* (strfromsymbol pname)) pname))                      ; for backward compatibility

(defn get-participant-id-from-call-sign
  [pname]
  (let [result (get *player-callsign-map* (strfromsymbol pname))]
    (if (not result)
      (do (when (dplev :error :all)
                (println "ERROR: failed to find call sign" (strfromsymbol pname) "in call sign map")
                (pprint *player-callsign-map*))
          (first *teammembers*))
      result)))

(defn get-participant-id-from-data
  [data]
  (or (:participant_id data)
      (:participantid data)
      (let [playername (:playername data)]
        (if playername
          (get-participant-id-from-player-name playername)
          "No participant name found"))))

;;; Statlearn data

(defn set-stat-record!
  [record]
  (def ^:dynamic *stat-record* record))

(defn set-rita-temporal-data!
  [rtd]
  (def ^:dynamic *rita-temporal-data*))

(defn set-learned-participant-model!
  [lpm]
  (def ^:dynamic *learned-participant-model* lpm))

(defn get-learned-model [] *loaded-learned-model*)

(defn set-learned-model!
  [numodel final]
  (when (or (not *loaded-learned-model*) final)
    (def ^:dynamic *loaded-learned-model* numodel)
    (when (dplev :all)
      (println "Learned model loaded:" (global/prs numodel)))))

(defn learned-model-path
  []
  (if (empty? *loaded-learned-model*) *default-learned-model-path*))

(defn set-learned-model-path
  [lmpath]
  (def ^:dynamic *provided-learned-model-path* lmpath))

(defn get-experiment-mission
  []
  (if (not *stat-record*)
    (if (dplev :warn :all)
      (println "Warning: *stat-record* not set in get-experiment-mission")))
  (and *stat-record* (:experiment-id (.meta-data *stat-record*))))

(defn get-trial-id
  []
  (if (not *stat-record*)
    (if (dplev :warn :all)
      (println "Warning: *stat-record* not set in get-trial-id")))
  (and *stat-record* (:trial-id (.meta-data *stat-record*))))

(defn set-lpm-pathname
  [pathname]
  (def ^:dynamic *learned-participant-model* pathname))

(defn set-rtd-pathname
  [pathname]
  (def ^:dynamic *rita-temporal-data* pathname))

(defn set-current-score-at!
  [teamscore tsecs]
  (def ^:dynamic *team-score-at* [teamscore tsecs]))

(defn get-current-score-at
  []
  *team-score-at*)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Loaded model

(def ^:dynamic *loaded-model-name* nil)

(defn get-loaded-model-name
  []
  *loaded-model-name*)

(defn set-loaded-model-name
  [lmn]
  (def ^:dynamic *loaded-model-name* lmn))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Beliefs, temporary

(def ^:dynamic *belief-standin* {})

(defn set-belief-state!
  [inwhat beliefmap]
  (def ^:dynamic *belief-standin* (merge *belief-standin* {inwhat beliefmap})))

(defn get-belief-state
  [inwhat]
  (get *belief-standin* inwhat))

(defn reset-belief-state
  []
  (def ^:dynamic *belief-standin* {}))

(defn update-belief!
  [inwhat bywhom belief]
  (let [prior-beliefs (get-belief-state inwhat)
        new-beliefs (merge prior-beliefs {bywhom belief})]
    (def ^:dynamic *belief-standin* (merge *belief-standin* {inwhat new-beliefs}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; MQTT

(def ^:dynamic *mqtt-connection* nil)

(defn set-mqtt-connection
  [mqttc]
  (def ^:dynamic *mqtt-connection* mqttc))

(defn get-mqtt-connection
  []
  *mqtt-connection*)



;;; Fin
