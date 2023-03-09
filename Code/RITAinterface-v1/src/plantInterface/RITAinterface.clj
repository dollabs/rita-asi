(ns plantInterface.RITAinterface
  "GYM Interface"
  (:require [clojure.string :as string]
            [plantInterface.DPLinterface-with-binding :as DPL])
  (:import [plantInterface.DPLinterface_with_binding dplinterface])
  (:gen-class))

;; The value we will get is integer between 0 --> 31
;; Convert binary to decimal... 
;; https://www.rapidtables.com/convert/number/binary-to-decimal.html

;; Switch 
;; 0000    
;; State 1 bit
;; direction 2 bits
;; distance 1 bit 

;; Victim
;; 00000
;; State 1 bit 
;; direction 2 bits 
;; distance 1 bit 
;; color 1 bit 

;; ;; Door
;; 00000 
;; State 1 bit
;; direction 2 bits
;; distance 1 bit
;; Room 1 bit
;; 
;;;; COMMUNICATION LANGUAGES with RITA ENVIRONMENT:
;;;; 1. initialize-simulator "start-world" 
;;;; 2. reset "reset"
;;;; 3. shutdown "shutdown"
;;;; 4. perform  "perform" with params
;;;; 5. render   "render" 

;---------------------------------------
;     The PLANT INTERFACE FUNCTIONS     
;---------------------------------------
; what is in :rita? should rita be replaced by plantid?
; observations
; numacts
; numobs
; high value / low value
; reward
; done
; 
(defn initialize-simulator
  "Start the simulator by sending out rmq message: create-environment"
  [self]
  (DPL/bp-call self (:plantid self) "start" [])
  (DPL/updatefieldvalue (:plantid self) :numacts 4)
  (DPL/updatefieldvalue (:plantid self) :numobs  1)
  (DPL/updatefieldvalue (:plantid self) :high0  32) ;; 2^5 - 1 ;; array length or value?
  (DPL/updatefieldvalue (:plantid self) :low0  0)
  (DPL/updatefieldvalue (:plantid self) :done false)
  (DPL/updatefieldvalue (:plantid self) :state0 0)) ;; 

(defn reset
  "Reset the simulator for the new episode by sending out rmq message: reset"
  [self]
  (DPL/bp-call  self (:plantid self) "reset" []))

(defn shutdown
  "Shutdown the simulator by sending out rmq message: shutdown"
  [self]
  (DPL/bp-call self (:plantid self) "shutdown" []))

(defn perform
  "Execute the selected action by sending out rmq message: perform."
  [self action cycletime]
  (let [apromise (promise)]
    (case action
      0 "turn-left-advance"
      1 "turn-right-advance"
      2 "turn-around-advance"
      3 "advance")

    (DPL/await-plant-message (:plantid self) apromise)
    (DPL/bp-call self (:plantid self) "perform" action)
    (if (> cycletime 0) (Thread/sleep cycletime))
    (deref apromise)

    (println "REWARD" (DPL/get-field-value :Robot1 :reward))))

;; Render the simulator UI
(defn render
  "Render the simulator by sending out rmq message: render"
  [self]
  (DPL/bp-call self (:plantid self) "render" []))

;; For now, if reward is >= 0, set done to true and send back true
(defn goal-achieved
  [self new-state reward episode-done]
  (cond (>= reward 0.0)
        (do
          (DPL/updatefieldvalue (:plantid self) :done true)
          (println "GOAL-ACHIEVED!")
          true)))

(defn get-discrete-state
  [learner state]
  (let [{q-table :q-table
         discretization :discretization} learner
        {obslow :obslow
         disc-os-win-size :disc-os-win-size} (deref q-table)]
    ;;(println "state=" state "low=" obslow "win=" disc-os-win-size)
    (let [discstate (vec
                     (doall
                      (map (fn [state low winsize]
                             (max 0
                                  (min (int (/ (- state low) winsize))
                                       (- discretization 1))))
                           state obslow disc-os-win-size)))]
       ;;(println "discstate=" discstate)
      discstate)))

(defn get-current-state
  [self numobs]
  (if (not (number? numobs))
    (do (println "numobs (" numobs ")is not a number.  This usually means that you need to restart the plant.")
        (System/exit 0)))
  (case numobs
    1 [(DPL/get-field-value (:plantid self) :state0)]
    2 [(DPL/get-field-value (:plantid self) :state0) (DPL/get-field-value (:plantid self) :state1)]
    3 [(DPL/get-field-value (:plantid self) :state0) (DPL/get-field-value (:plantid self) :state1) (DPL/get-field-value (:plantid self) :state2)]
    (do (println (format "Wrong number of observations (%d), must be between 1 and 3." numobs))
        (System/exit 0))))

;;;;;;;; Need change
(defn get-obs-high
  [numobs]
  (case numobs
    1 [(DPL/get-field-value :Robot1 :high0)]
    2 [(DPL/get-field-value :Robot1 :high0) (DPL/get-field-value :Robot1 :high1)]
    3 [(DPL/get-field-value :Robot1 :high0) (DPL/get-field-value :Robot1 :high1) (DPL/get-field-value :Robot1 :high2)]
    (do (println (format "Wrong number of observations (%d), must be between 1 and 3." numobs))
        (System/exit 0))))


;;;;;;;; Need change


(defn get-obs-low
  [numobs]
  (case numobs
    1 [(DPL/get-field-value :Robot1 :low0)]
    2 [(DPL/get-field-value :Robot1 :low0) (DPL/get-field-value :Robot1 :low1)]
    3 [(DPL/get-field-value :Robot1 :low0) (DPL/get-field-value :Robot1 :low1) (DPL/get-field-value :Robot1 :low2)]
    (do (println (format "Wrong number of observations (%d), must be between 1 and 3." numobs))
        (System/exit 0))))

;;;;;;;; Need change
(defn win-size
  "Compute the vector of window sizes for each state variable according to the discretization factor."
  [numobs ssdi]
  (vec (map (fn [high low] (/ (- high low) ssdi)) (get-obs-high numobs) (get-obs-low numobs))))

(defn make-rita-interface
  [world-name routing binding channel ex-name plantID]
  (let [interface (dplinterface.                   ; dpl/make-dpl-interface
                   world-name                                   ; :world-parameters 
                   routing                                      ; :routing
                   binding
                   channel                                      ; :channel
                   ex-name                                      ; :exchange
                   plantID                                      ; :plantid

                   (fn [self]                                    ; :initialize-world
                     (initialize-simulator self))

                   (fn [self]                           ; :shutdown
                     (shutdown self))

                   (fn [self action cycletime]                  ; :perform
                     (perform self action cycletime))

                   (fn [self]                           ; :reset
                     (reset self))

                   (fn [self]                                       ; :render
                     (render self))

                   (fn [self new-state reward episode-done]          ; :goal-achieved
                     (goal-achieved self new-state reward episode-done))

                   (fn [learner state]                          ; :get-discrete-state
                     (get-discrete-state learner state))

                   (fn [self obj field]                               ; :get-field-value
                     (DPL/get-field-value obj field))

                   (fn [self obj field val]                           ; :set-field-value
                     (DPL/updatefieldvalue obj field val))

                   (fn [self numobs]                                  ; :get-current-state
                     (get-current-state self numobs)))]
    interface))






