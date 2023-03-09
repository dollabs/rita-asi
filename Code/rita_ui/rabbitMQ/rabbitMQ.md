### rabbitMQ-received.js do the following tasks:

- Establishes a RabbitMQ connection: start(exchange, routingKeys)
- Opens a channel in the established connection: establishChannel(connection, exchange, routingKeys)
- Opens a topic exchange and starts consuming incoming messages: consumeMessages(channel, exchange, routingKeys, connection)

### processMessage.js do the following tasks: Digest the messages/data consumed by the topic exchange:

1.  Converts all messages to json format: `const receivedMsg = await JSON.parse(msg.content.toString());`

2.  Digests Timestamp from the Testbed (processing data)

    - Data from: `receivedMsg['testbed-message']['msg']['timestamp'];`
    - Converts whatever Date format to minutes.
    - Sends the processed timestamp using Emitter: `emit.emit('timestampMin', timeScaleMin)`
    - Receiver(s): SocketIO

3.  Digests Predictions from State Estimation and Prediction Generator (Filtering data)

    - Data from: `receivedMsg['predictions']`
    - Obtains predictions with state unknown and true from State Estimation.
    - Obtains predictions with state false from Prediction Generator.
    - Sends predictions using Emitter: `emit.emit('newPrediction', receivedMsg['predictions'])`
    - Receiver(s): SocketIO

4.  Digests Cognitive Load from State Estimation (Filtering data)
    - Data from: receivedMsg['belief-state-changes']['cognitive-load']
    - Sends Cognitive Load values using Emitter: `emit.emit('newCognitiveLoad', receivedMsg['belief-state-changes']['values']`
    - Receiver (s): SocketIO
