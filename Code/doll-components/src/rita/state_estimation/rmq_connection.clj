(ns rita.state-estimation.rmq-connection
  (:require  [langohr.core      :as rmq]
             [langohr.channel   :as lch]
             [langohr.queue     :as lq]
             [langohr.exchange  :as le]
             [langohr.consumers :as lc]
             [langohr.basic     :as lb]
             [nano-id.core :refer [nano-id]]
             [clojure.data.json :as json]
             [cheshire.core :refer :all]))

(def ^:dynamic default-exchange-name "rita-v1")
(def ^:dynamic default-publisher-routing-key "observations")
(def ^:dynamic default-consumer-routing-key "rita.*") ;; rita.rl and rita.ui
(def ^:dynamic id_index (atom 0))

;;; SCHEMA FOR THE PLANT INSTANCE
(defrecord RMQ [plantID        ; unique ID
                host           ; localhost
                port           ; 5672
                connection     ; RMQ Connection
                channel        ; RMQ Channel
                exchange-name  ; RMQ Exchange name
                exchange       ; RMQ Exchange
                p-routing      ; publisher routing key
                c-binding      ; consumer binding key
                printMyself    ; print important properties of this Plant
                subscribe      ; consumers subscribe to the exchange, and start consuming message
                publish        ; publishers starts sending out messages
                publishJSON    ; publishers starts sending out json data
                close          ; close RMQ connection and channel
                getPlantID     ; get the ID of this plant
                ])

(defn printSelf
  [self]
  (if (nil? (:channel self))
    (println "From RITAinterface: channel connection fail")
    (do
      (println "RabbitMQ connection Established, listening to: ")
      (println (format "PlantID: %s \nExchange: %s \nHost: %s \nPort: %s \nRouting key: %s \nBinding key: %s\n"
                       (:plantID self) (:exchange-name self) (:host self) (:port self) (:p-routing self) (:c-binding self))))))

(defn publishData
  "Publishes a new message"
  [channel exchange-name p-routing data]
  (lb/publish channel exchange-name p-routing data {:content-type "text/plain" :type "default-exchange.update"}))

(defn publishJSON
  "publish given object as json"
  [channel exchange-name p-routing jsonData]
  (lb/publish channel exchange-name p-routing (generate-string jsonData {:pretty true}) {:content-type "json" :type "default-exchange.update"}))

(defn closeConnection [connection]
  (when (and connection (rmq/open? connection))
    (rmq/close connection)
    (println "close the RMQ connection")))

(defn closeChannel [channel]
  (when (and channel (rmq/open? channel))
    (lch/close channel)
    (println "close the channel")))

(defn closeAll [channel connection]
  (closeChannel channel
                (closeConnection connection)))

(defn subscribeAndListen
  "Create and bound a queue to the given topic exchange"
  [channel exchange-name queue-name binding-key message-handler] ;empty "" queue-name = auto-generated name
  ; create a queue
  (let [queue-name' (:queue (lq/declare channel queue-name {:exclusive false :auto-delete true}))]
      ;binds the queue to the exchange, with attached routing key
    (lq/bind channel queue-name' exchange-name {:routing-key binding-key})
      ; consumer subscribes to the queue and starts listening, message-handler will handle incoming messages.
    (lc/subscribe channel queue-name' message-handler {:auto-ack true})
    (println "Waiting for incoming messages:")))

;;; CREATES A PLANT INSTANCE
(defn createRMQ
  [& {:keys [plantID host port conn ch ex-name ex p-routing c-binding],
      :or {plantID       (nano-id)
           host          "localhost"
           port          5672
           conn          (rmq/connect)
           ch            (lch/open conn)
           ex-name       default-exchange-name
           ex            (le/declare ch ex-name "topic" {:durable false :auto-delete true})
           p-routing     default-publisher-routing-key
           c-binding     default-consumer-routing-key}}]

  (RMQ.
   plantID                             ; plantID
   host                                ; host
   port                                ; port
   conn                                ; connection
   ch                                  ; channel
   ex-name                             ; exchange-name
   ex                                  ; exchange
   p-routing                           ; routing-key
   c-binding                           ; consumer binding key
   printSelf                           ; function printMyself
   subscribeAndListen                  ; function subscribe
   publishData                         ; function publish
   publishJSON                         ; function publish data as json objects
   closeAll                            ; function close
   (fn [self] (:plantID self))))       ; function getPlantID    








