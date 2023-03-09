;; Copyright © 2020 Dynamic Object Language Labs Inc.
;; DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
;; Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

(def project 'dollabs/rita)
(def version "0.0.1-SNAPSHOT")
(def description "RITA")
(def project-url "https://dcerys@bitbucket.org/dollincgit/asist_rita.git")

(def rita-modules (map
                      (fn [module-symbol]
                        (list module-symbol
                              (symbol (str "rita." module-symbol ".cli"))
                              (str module-symbol ".jar")))
                      '(generative-planner
                        temporal-planner
                        mission-dispatcher
                        prediction-generator
                        mission-tracker
                        action-planning
                        state-estimation
                        testbed-bus-interface ;;this will likely be Python-based
                        rita-control-panel
                        rmq-validator
                        experiment-control
                        rita-player
                        )))


(def mains (set (map second rita-modules)))

(defn check-dup-versions [deps]
  (let [sorted (reduce (fn [res [symb ver]]
                         ;(println symb ver)
                         ;(println res)
                         (if (contains? res symb)
                           (update-in res [symb] (fn [oldv]
                                                   (conj oldv ver)))
                           (update-in res [symb] (fn [_]
                                                   (conj [] ver)))
                           )
                         )
                       (sorted-map) deps)
        _ (doseq [[symb vals] sorted]
            #_(println vals "all equal?" (apply = vals))
            (if-not (apply = vals) (println "Inconsistent versions found:" symb (into [] (sort vals)))))
        ]
    ;(pprint sorted)

    ))

(def deps-list '[[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/data.json "0.2.6"]
                 [clojure-future-spec "1.9.0"] ;;Clojure spec support (for Clojure 1.8)
                 ;; logging - Comment these out for now
                 ;;[org.slf4j/slf4j-nop       "1.7.30"]
                 [com.taoensso/timbre       "4.10.0"]
                 ;;[org.slf4j/slf4j-api       "1.7.30"]
                 [com.fzakaria/slf4j-timbre "0.3.19"]
                 [org.clojure/tools.logging "1.0.0"]
                 ;; utilities
                 [environ "1.1.0"]
                 [avenir "0.2.2"]
                 [me.raynes/fs "1.4.6"]
                 [camel-snake-kebab "0.4.0"]
                 [metosin/scjsv "0.4.1"]
                 ;[com.novemberain/monger    "3.1.0"]
                 [com.novemberain/langohr "3.3.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.csv "0.1.4"]
                 ;; [danlentz/clj-uuid "0.1.9"]
                 ;; This Pamela dependency doesn't work  - not yet defined
                 ;[dollabs/pamela             "0.6.3-SNAPSHOT"]
                 ;; testing
                 ;; [adzerk/boot-test          "1.1.2" :scope "test"]
                 ;; [criterium                 "0.4.4" :scope "test"]
                 [clojure.java-time "0.3.2"]

                 ; pamela deps
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/data.json "0.2.6"]
                 ;; logging
                 ;[org.slf4j/slf4j-api "1.7.21"]
                 ;[org.clojure/tools.logging "0.3.1"]
                 ;; utilities
                 [environ "1.1.0"]
                 [instaparse "1.4.7"]
                 [avenir "0.2.2"]
                 [me.raynes/fs "1.4.6"]
                 [camel-snake-kebab "0.4.0"]
                 [dollabs/plan-schema "0.3.8"]

                 ;; Random-seed
                 [random-seed "1.0.0"]

                 ; plan-schema deps
                 [environ "1.1.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [prismatic/schema "1.1.5"]
                 [org.clojure/data.json "0.2.6"]
                 [avenir "0.2.2"]
                 [me.raynes/fs "1.4.6"]

                 ; pamela-tools deps
                 [org.clojure/clojure "1.8.0"]
                 ;[onetom/boot-lein-generate "RELEASE" :scope "test"]
                 [adzerk/boot-test "RELEASE" :scope "test"]

                 [com.novemberain/langohr "3.3.0"]
                 [net.mikera/core.matrix "0.50.0"]
                 [org.apache.commons/commons-math3 "3.6.1"]
                 [incanter "1.5.5"]
                 [me.raynes/fs "1.4.6"]
                 [net.mikera/imagez "0.12.0"]
                 ;Loading src/pamela_tools/utils/mongo/db.clj...
                 ;CompilerException java.lang.RuntimeException: Unable to find static field: ACKNOWLEDGED in class com.mongodb.WriteConcern, compiling:(monger/core.clj:59:1)
                 ; Monger 3.1.0 does not load in my repl but compiles fine!
                 [com.novemberain/monger "3.0.2"]
                 [org.zeroturnaround/zt-exec "1.10"]
                 [org.zeroturnaround/zt-process-killer "1.4"]
                 [com.hierynomus/sshj "0.17.2"]
                 [ruiyun/tools.timer "1.0.1"]
                 [automat "0.2.0"]

                 ;[org.clojure/data.csv "0.1.3"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.5"]
                 [nano-id "1.0.0"]
                 [cheshire "5.10.0"]
                 [clojurewerkz/machine_head "1.0.0"]
                 ])
(check-dup-versions deps-list)

(set-env!
  :source-paths #{"src" "pamela-tools/src" "pamela/src" "plan-schema/src" "BSP/src"}
  :resource-paths #{"resources" "pamela/resources"}
  ; Needed for pamela-tools
  :dependencies deps-list)

;; (require
;;   '[adzerk.boot-test :refer [test]])

(task-options!
  pom {:project     project
       :version     version
       :description description
       :url         project-url
       :scm         {:url project-url}
       :license     {"Apache-2.0" "http://opensource.org/licenses/Apache-2.0"}}
  aot {:namespace   mains}
  ;; jar {:main        main}
  ;; test {:namespaces #{'testing.rita.main
  ;;                     }}
  )

;; (deftask run
;;   "Run the project (build from source)."
;;   [A args ARG [str] "preface each app arg with -A"]
;;   (require [main :as 'app])
;;   (with-post-wrap [_]
;;     (apply (resolve 'app/-main) args)))

(deftask build-jar []
  (comp
   (sift)
   (aot)
   (uber)
   (let [[_ main-ns jar-filename] (nth rita-modules 0)]
     (comp
      (jar :file jar-filename :main main-ns)
      (target :dir #{"target"})
      (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
   (let [[_ main-ns jar-filename] (nth rita-modules 1)]
     (comp
      (jar :file jar-filename :main main-ns)
      (target :dir #{"target"} :no-clean true)
      (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
   (let [[_ main-ns jar-filename] (nth rita-modules 2)]
     (comp
      (jar :file jar-filename :main main-ns)
      (target :dir #{"target"} :no-clean true)
      (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
   (let [[_ main-ns jar-filename] (nth rita-modules 3)]
     (comp
      (jar :file jar-filename :main main-ns)
      (target :dir #{"target"} :no-clean true)
      (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
   (let [[_ main-ns jar-filename] (nth rita-modules 4)]
     (comp
      (jar :file jar-filename :main main-ns)
      (target :dir #{"target"} :no-clean true)
      (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
   (let [[_ main-ns jar-filename] (nth rita-modules 5)]
     (comp
      (jar :file jar-filename :main main-ns)
      (target :dir #{"target"} :no-clean true)
      (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
   (let [[_ main-ns jar-filename] (nth rita-modules 6)]
     (comp
      (jar :file jar-filename :main main-ns)
      (target :dir #{"target"} :no-clean true)
      (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
   ;; (let [[_ main-ns jar-filename] (nth rita-modules 7)]
   ;;   (comp
   ;;    (jar :file jar-filename :main main-ns)
   ;;    (target :dir #{"target"} :no-clean true)
   ;;    (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
   ;; (let [[_ main-ns jar-filename] (nth rita-modules 8)]
   ;;   (comp
   ;;    (jar :file jar-filename :main main-ns)
   ;;    (target :dir #{"target"} :no-clean true)
   ;;    (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
   (let [[_ main-ns jar-filename] (nth rita-modules 9)]
     (comp
      (jar :file jar-filename :main main-ns)
      (target :dir #{"target"} :no-clean true)
      (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
   (let [[_ main-ns jar-filename] (nth rita-modules 10)]
     (comp
      (jar :file jar-filename :main main-ns)
      (target :dir #{"target"} :no-clean true)))
   (let [[_ main-ns jar-filename] (nth rita-modules 11)]
     (comp
       (jar :file jar-filename :main main-ns)
       (target :dir #{"target"} :no-clean true)))
   )
  )

; To build specific components
; We create target directory to keep all uber jar files. :no-clean true preserves jar files
; created from individual commands below.
(deftask build-gp []
         (comp (sift) (aot) (uber) (let [[_ main-ns jar-filename] (nth rita-modules 0)]
                                     (println main-ns jar-filename)
                                     (comp
                                       (jar :file jar-filename :main main-ns)
                                       (target :dir #{"target"} :no-clean true)
                                       (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))))

(deftask build-tp []
         (comp (sift) (aot) (uber) (let [[_ main-ns jar-filename] (nth rita-modules 1)]
                                     (println main-ns jar-filename)
                                     (comp
                                       (jar :file jar-filename :main main-ns)
                                       (target :dir #{"target"} :no-clean true)
                                       (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))))

(deftask build-md []
         (comp (sift) (aot) (uber) (let [[_ main-ns jar-filename] (nth rita-modules 2)]
                                     (println main-ns jar-filename)
                                     (comp
                                       (jar :file jar-filename :main main-ns)
                                       (target :dir #{"target"} :no-clean true)
                                       (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))))

(deftask build-pg []
         (comp (sift) (aot) (uber) (let [[_ main-ns jar-filename] (nth rita-modules 3)]
                                     (println main-ns jar-filename)
                                     (comp
                                       (jar :file jar-filename :main main-ns)
                                       (target :dir #{"target"} :no-clean true)
                                       (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))))

(deftask build-mt []
         (comp (sift) (aot) (uber) (let [[_ main-ns jar-filename] (nth rita-modules 4)]
                                     (println main-ns jar-filename)
                                     (comp
                                       (jar :file jar-filename :main main-ns)
                                       (target :dir #{"target"} :no-clean true)
                                       (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))))

(deftask build-se []
         (comp
           (sift)
           (aot)
           (uber)
           (let [[_ main-ns jar-filename] (nth rita-modules 6)]
             (comp
               (jar :file jar-filename :main main-ns)
                                       (target :dir #{"target"} :no-clean true)
               (sift :include [(re-pattern (str "^" jar-filename "$"))] :invert true)))
           ))

(deftask build-cp []
         (comp (sift) (aot) (uber) (let [[_ main-ns jar-filename] (nth rita-modules 8)]
         (comp
                                       (jar :file jar-filename :main main-ns)
                                       (target :dir #{"target"} :no-clean true)))))

(deftask build-rv []
         (comp (sift) (aot) (uber) (let [[_ main-ns jar-filename] (nth rita-modules 9)]
             (comp
               (jar :file jar-filename :main main-ns)
               (target :dir #{"target"} :no-clean true)))))

(deftask build-ec []
         (comp (sift) (aot) (uber) (let [[_ main-ns jar-filename] (nth rita-modules 10)]
             (comp
               (jar :file jar-filename :main main-ns)
                                       (target :dir #{"target"} :no-clean true)))))

(deftask build-rp []
         (comp (sift) (aot) (uber) (let [[_ main-ns jar-filename] (nth rita-modules 11)]
                                     (comp
                                       (jar :file jar-filename :main main-ns)
                                       (target :dir #{"target"} :no-clean true)))))

; Helpful task for catching errors early on !!
(deftask check-errors-deeply
         "Use aot to check for errors"
         []
         (println "Compiling files")
         (comp (aot :all true) (target :dir #{"target-compile"} :no-clean true)))

(deftask check-errors
         "Use aot to check for errors"
         []
         (println "Compiling files")
         (comp (aot) (target :dir #{"target-compile"} :no-clean true)))

;; NOTE: Requires PAMELA_MODE=prod (or unset)
(deftask cli-test
  "Run the command line tests."
  []
  (let [cmd ["./bin/cli-test"]]
    (comp
      (build-jar)
      (with-post-wrap [_]
        (apply dosh cmd))))) ;; will throw exception on non-zero exit

(deftask all-tests
  "Run the Clojure and command line tests."
  []
  (comp
    (cli-test)                                              ;Call build-jar, which recreates target, which deletes target/gen-files.
    ; we need to preserve target/gen-files
    (test)
    ))

;; boot -d onetom/boot-lein-generate generate
;; For Emacs if you Customize your Cider Boot Parameters to 'cider-boot'
;; then this task will be invoked upon M-x cider-jack-in-clojurescript
;; which is on C-c M-J
;; CIDER will then open two REPL buffers for you, one for CLJS
;; and another for CLJ. FFI:
;; https://cider.readthedocs.io/en/latest/up_and_running/#clojurescript-usage

;; This task is commented out here for users that have not copied
;; a profile.boot file to ~/.boot/ which defines the cider task:
;;

;; (deftask cider-boot
;;   "Cider boot params task"
;;   []
;;   ;; (cider))
;;   (comp
;;     (cider)
;;     (repl :server true)
;;     (wait)))
