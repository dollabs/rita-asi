(ns cli
  "Q-Learning."
  (:require [clojure.tools.cli :as cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [clojure.data.codec.base64 :as base64]
            [clojure.string :as string]
            [clojure.pprint :as pp :refer [pprint]]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [pamela.tools.Qlearning.Qtables :as qtbl]
            [pamela.tools.Qlearning.DMQL :as dmql]
            [plantInterface.DPLinterface-with-binding :as DPL]
            [plantInterface.RITAinterface :as rita]
            [rmqConnection.RMQConnectionPlant :as rmq])
  (:gen-class)) ;; required for uberjar

"" "
   How to run from the terminal:
   1. cd to RITAinterface-v1
   2. boot -r src repl -n cli // cli is the namespace of the file
   3. (q-learner args) // args include: test-connection, qlearn
    Example: (q-learner 'test-connection' '-i hello')
   
   How to run SE simulator from 
    1. cd to doll-component
    2. boot -r src repl -n rita.state-estimation.rlbotif
    3. (start-rlbot-thread)
  
   How to run from boot build:
   1. cd to RITAinterface-v1
   2. boot build-jar
   3. cd to target/
   4. java -jar dmrl.jar arg1 arg2 arg3 // arg1 = the command (test-conenction, qlearn) // from arg2, its about all cli-options
    Example: java -jar dmrl.jar 'qlearn' -i 'hello'
" ""

(def cli-options [;;["-m" "--model pm" "pamela model of system" :default nil]
                  ;; Pamela related options (Which is not yet hooked in)
                  ["-o" "--output file" "output" :default "spamela.txt"]
                  ["-m" "--model ir" "Model IR" :default nil]
                  ["-r" "--root name" "Root pClass" :default "main"]

                  ;; RabbitMQ related options
                  ["-h" "--host rmqhost" "RMQ Host" :default "localhost"]
                  ["-p" "--port rmqport" "RMQ Port" :default 5672 :parse-fn #(Integer/parseInt %)]
                  ["-e" "--exchange name" "RMQ Exchange Name" :default "rita-v1"]

                  ;; These are learning related options - with reasonable defaults
                  ["-l" "--loadqtable edn" "Start from a prior Q-table" :default nil]
                  ["-i" "--if id" "IF ID" :default "Robot1"] ; The interface ID (plant - robot or simulator)
                  ["-n" "--episodes n" "Number of Episodes" :default 10 :parse-fn #(Integer/parseInt %)]
                  ["-S" "--statistics n" "Statistics saved after n Episodes" :default 2 :parse-fn #(Integer/parseInt %)]
                  ["-R" "--render n" "Render after n Episodes" :default 2 :parse-fn #(Integer/parseInt %)]
                  ["-B" "--backup n" "Backup frequency in episodes" :default 2 :parse-fn #(Integer/parseInt %)]
                  ["-a" "--alpha f" "Learning Rate" :default 0.1 :parse-fn #(Float/parseFloat %)]
                  ["-d" "--discount f" "Discount Rate" :default 0.95 :parse-fn #(Float/parseFloat %)]
                  ["-x" "--explore f" "Portion of episodes to explore" :default 0.5 :parse-fn #(Float/parseFloat %)]
                  ["-c" "--cycletime ms" "Cycle time in milliseconds" :default 250 :parse-fn #(Integer/parseInt %)]
                  ["-q" "--min-q n" "Minimum Q value" :default -1.0  :parse-fn #(Float/parseFloat %)]
                  ["-u" "--max-q n" "Maximum Q value" :default 0.0   :parse-fn #(Float/parseFloat %)]
                  ["-s" "--statedivision n" "Discretization of each state dimension" :default 32 :parse-fn #(Integer/parseInt %)]

                  ["-g" "--ritaworld gw" "Name of the Rita World" :default "Rita-Simulator"]
                  ["-z" "--epsilon fr" "Starting value for epsilon exploration 1 >= fr >= 0" :default 1.0 :parse-fn #(Float/parseFloat %)]
                  ["-=" "--mode n" "Select a special mode [0=normal, 1=Monte-Carlo, others to come]" :default 1  :parse-fn #(Integer/parseInt %)]

                  ;; Debugging options
                  ["-v" "--verbose level" "Verbose mode" :default "0"]

                  ;; Help
                  ["-?" "--help"]])

(defn q-learner
  "DOLL Reinforcement Q-Learner"
  [& args]

  (println args)
  ;; (println cli-options)

  (let [parsed (cli/parse-opts args cli-options)
        {:keys [options arguments error summary]} parsed
        {:keys [help version verbose test-connection qlearn]} options
        cmd (first arguments)
        verbosity (read-string (get-in parsed [:options :verbose]))
        _ (DPL/set-verbosity verbosity)
        _ (if (> verbosity 1) (println parsed))

        model (get-in parsed [:options :model])
        outfile (get-in parsed [:options :output])

        ex-name (get-in parsed [:options :exchange])
        host (get-in parsed [:options :host])
        ;; _ (if (> verbosity 0) (println ["host = " host]))
        exch (get-in parsed [:options :exchange])
        ifid (get-in parsed [:options :if])       ; Interface ID
        neps (get-in parsed [:options :episodes]) ; Number of episodes
        rend (get-in parsed [:options :render])   ; Number of episodes before rendering
        stat (get-in parsed [:options :statistics]) ; Number of episodes before statistics
        back (get-in parsed [:options :backup])   ; Number of episodes before saving Q-table
        loaq (get-in parsed [:options :loadqtable]) ; Restart learning from a prior Q table
        alph (get-in parsed [:options :alpha])    ; Learning rate
        disc (get-in parsed [:options :discount]) ; Discount rate
        epsi (get-in parsed [:options :epsilon])  ; Epsilon starting value
        minq (get-in parsed [:options :min-q])    ; Minimum initial Q value
        maxq (get-in parsed [:options :max-q])    ; Maximum initial Q value
        expl (get-in parsed [:options :explore])  ; fraction of episodes for which exploration takes place
        ssdi (get-in parsed [:options :statedivision]) ; State space discretization for each dimension
        cycl (get-in parsed [:options :cycletime]); Cycletime in milliseconds
        gwld (get-in parsed [:options :ritaworld]) ; Name of the RITA world to instantiate
        mode (get-in parsed [:options :mode])      ; Mode 0=normal, 1=Monte-Carlo, etc.

    ;;trfn (get-in parsed [:options :tracefile])
        frfi (get-in parsed [:options :fromfile])
        port (get-in parsed [:options :port])
        _ (if (> verbosity 0) (println ["port = " port]))
        help (get-in parsed [:options :help])
        _ (if (> verbosity 0) (println ["help = " help]))
        root (symbol (get-in parsed [:options :root]))
        _ (if (> verbosity 0) (println "DOLL Reinforcement Q-Learner" (:options parsed)))]


    ;; RabbitMQ
    (def routing-key "rita.rl") ;; publishing
    (def binding-key "observations") ;; listening
    (def plantifid (keyword ifid))
    (def world-name gwld)
    (def host host)
    (def port port)

    ;; 1. Connect to rmq to start listening
    ;; [host port ex-name plantifid binding-key]
    (def rmq-ch (DPL/rabbitMQ-connect host port ex-name plantifid binding-key))
    (println "RabbitMQ connection Established, plantifid=" plantifid "ex-name=" ex-name) ;; the robot object 

    (cond (>= (count arguments) 1)
          (case (keyword (first arguments))

            ;; Test the connection 
            :test-connection
            ;; (let [rita-plant-interface (rita/make-rita-interface world-name routing-key (:channel rmq-instance) ex-name plantifid)]
            (let [rita-if (rita/make-rita-interface world-name routing-key binding-key rmq-ch ex-name plantifid)]
               ;;; 2. Initialized the world (environment)
              ((:initialize-world rita-if) rita-if)
              (Thread/sleep 1000)

              ;; ((:reset rita-if) rita-if)
              ;; (Thread/sleep 1000)

              ;; ((:shutdown rita-if) rita-if)
              ;; (Thread/sleep 1000)

              ((:perform rita-if) rita-if 0 1000)
              ((:perform rita-if) rita-if 1 1000)
              ((:perform rita-if) rita-if 2 1000)
              ((:perform rita-if) rita-if 3 1000))

            :qlearn
            (let [_ (println (format "*** Starting the Q learner with %s (%d episodes, mode=%d, epsilon=%f explore=%f) ***%n"
                                     gwld neps mode epsi expl))
                  rita-if (rita/make-rita-interface world-name routing-key binding-key rmq-ch ex-name plantifid)]

              ;;; 2. Initialized the world (environment)
              ((:initialize-world rita-if) rita-if)
              (Thread/sleep 100)

               ;;; 3. Get numbs, numacts
              (let [numobs (DPL/get-field-value (:plantid rita-if) :numobs) ;; observation dimension 1
                    numacts (DPL/get-field-value (:plantid rita-if) :numacts)] ;; action space 4
                (println (format "*** Observation Dimension = %d Actions = %d" numobs numacts))

                ;; (print numobs ssdi numacts minq maxq (rita/win-size numobs ssdi) (+ minq (/ (- maxq minq) 2.0)) (rita/get-obs-low numobs))

                ;;; 4. Get/Initialized q-table
                (let [initial-q-table
                      (if loaq
                        ;; Load old new q-table
                        (let [prior-q-table (qtbl/read-q-table loaq)
                              {episodes :episodes} prior-q-table]
                          (println "Restarting from a prior q-table: " loaq "episode=" episodes)
                          prior-q-table)

                        ;; Create a brand new q-table
                        ;; will work on minq maxq
                        ;; [1, 1, 4]
                        (let [q-value (+ minq (/ (- maxq minq) 2.0))
                              obslow (rita/get-obs-low numobs)
                              disc-os-win-size (rita/win-size numobs ssdi)
                              episode 0]
                          (println "creating a new q-table")

                          ;; (println (str "disc-os-win-size: " disc-os-win-size))
                          ;; (println (str "Space discretization for each dimension: " ssdi))
                          (qtbl/make-java-fixed-sized-q-table-constant numobs ssdi numacts q-value obslow disc-os-win-size episode)))

                      ;; 5. Create the learner    
                      learner (dmql/initialize-learner cycl 200 mode rend stat back alph disc
                                                       epsi neps expl ssdi numobs numacts
                                                       initial-q-table rita-if)]
                  ;; (pprint "--- Initial q-table:")
                  ;; (pprint (:storage initial-q-table))

                 ;; 6. Start training... 
                  (dmql/train learner)
                  (println "Training completed.")
                  (System/exit 0))))))))

(defn  -main
  {:added "0.1.0"}
  [& args]
  (apply q-learner args)
  nil)
