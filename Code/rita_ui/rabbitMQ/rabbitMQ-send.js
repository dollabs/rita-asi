/* eslint-disable no-use-before-define */
/* eslint-disable no-shadow */
// eslint-disable-next-line import/no-extraneous-dependencies
const amqp = require('amqplib/callback_api');

let amqpConn = null;
process.env.CLOUDAMQP_URL = 'amqp://localhost';
// const amqpURL = 'amqp://obablkhe:vN3l2Lpj5sfUcstYJp1oQsHnR-I_0bHZ@toad.rmq.cloudamqp.com/obablkhe';

// The start function will establish a connection to RabbitMQ
function start() {
  amqp.connect(`${process.env.CLOUDAMQP_URL}?heartbeat=60`, function (err, connection) {
    // handle amqp connection error
    if (err) {
      console.error('[AMQP]', err.message);
      return setTimeout(start, 1000);
    }

    connection.on('error', function (err) {
      if (err.message !== 'Connection closing') {
        console.error('[AMQP] conn error', err.message);
      }
    });

    // If the connection is closed or fails to be established, it will try to reconnect.
    connection.on('close', function () {
      console.error('[AMQP] reconnecting');
      return setTimeout(start, 1000);
    });

    // if no errors found, set the successful connection to amqpConn for futher usage
    console.log('[AMQP] connected');
    amqpConn = connection;
    startSending();
  });
}

// Initialized variables
// offlinePubQueue is an internal queue for messages that could not be sent when the application was offline. The application will check this queue and send the messages in the queue if a message is added to the queue.
// Routing/Binding key patterns for the topic exchange type
const keyPattern = 'dog';
const exchangeName = 'rita';
let pubChannel = null;

function startSending() {
  amqpConn.createChannel(function (err, channel) {
    // Handling errors
    if (closeOnErr(err)) return;
    channel.on('error', function (err) {
      console.error('[AMQP] channel error', err.message);
    });
    channel.on('close', function () {
      console.log('[AMQP] channel closed');
    });

    // If no errors:
    // Create a topic exchange
    pubChannel = channel;
    pubChannel.assertExchange(exchangeName, 'topic', {
      durable: false,
    });

    // Try sending message
    // const msg = `{"mission-id": "6483fec4-153c-4994-8f42-a2e9b00d4db3", "timestamp": ${Date.now()}, "routing-key": "belief-state-changes", "app-id": "StateEstimation"}`;

    setInterval(function () {
      publish(
        exchangeName,
        keyPattern,
        Buffer.from(
          `{"mission-id": "6483fec4-153c-4994-8f42-a2e9b00d4db3", "timestamp": ${Date.now()}, "routing-key": "belief-state-changes", "app-id": "StateEstimation"}`
        )
      );
      console.log(" [x] Sent %s: '%s'", keyPattern);
    }, 3000);
  });
}

function publish(exchange, routingKey, content) {
  try {
    pubChannel.publish(exchange, routingKey, content, {
      persistent: true,
    });
  } catch (e) {
    console.error('[AMQP] publish', e.message);
  }
}

// Close the connection on errors
function closeOnErr(err) {
  if (!err) return false;
  console.error('[AMQP] error', err);
  amqpConn.close();
  return true;
}

start();
