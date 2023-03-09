/* eslint-disable no-use-before-define */
/* eslint-disable no-shadow */
// eslint-disable-next-line import/no-extraneous-dependencies

/* Note: offlinePubQueue is an internal queue for messages that could not be sent 
when the application was offline. The application will check this queue and 
send the messages in the queue if a message is added to the queue.
*/
const amqp = require('amqplib/callback_api');
let amqpConn = null;
let pubChannel = null;
process.env.CLOUDAMQP_URL = 'amqp://localhost'; // const amqpURL = 'amqp://obablkhe:vN3l2Lpj5sfUcstYJp1oQsHnR-I_0bHZ@toad.rmq.cloudamqp.com/obablkhe';
let test = true;

function closeOnErr(err, type) {
  if (!err) return false;
  console.error('[AMQP] error', err.message, err.stack);
  type.close();
  return true;
}
function establishConnAndSendMessage(exchangeName, routingKey, data) {
  // 1. Establishes RMQ connection
  amqp.connect(`${process.env.CLOUDAMQP_URL}?heartbeat=60`, function (_err, connection) {
    connection.on('error', function (err) {
      closeOnErr(err, amqpConn);
      amqpConn = null;
    });
    connection.on('close', function () {
      if (test) console.log('[AMQP] RMQ Connection closed');
      amqpConn = null;
    });

    amqpConn = connection;
    if (test) console.log('[AMQP] Successfully established the RMQ connection');

    // 2. Establishes RMQ channe;
    establishRMQChannel(exchangeName, routingKey, data);
  });
}

function establishRMQChannel(exchangeName, routingKey, data) {
  amqpConn.createChannel(function (_err, channel) {
    channel.on('error', function (err) {
      closeOnErr(err, channel);
      pubChannel = null;
    });
    channel.on('close', function () {
      if (test) console.log('[AMQP] RMQ Channel closed');
      pubChannel = null;
    });

    pubChannel = channel;
    pubChannel.assertExchange(exchangeName, 'topic', {
      durable: false,
    });
    if (test) console.log('[AMQP] Successfully established the RMQ channel');

    // 3. Start publishing message
    const payloadAsString = JSON.stringify(data);
    publish(exchangeName, routingKey, payloadAsString);
    if (test) {
      console.log(`[AMQP] Sent a message via exchange-name ${exchangeName} with routing-key ${routingKey}`);
      console.log(payloadAsString);
    }
  });
}

function publish(exchange, routingKey, content) {
  try {
    const options = {
      persistent: true,
      noAck: false,
      timestamp: Date.now(),
      contentEncoding: 'utf-8',
      contentType: 'text/plain',
    };

    pubChannel.publish(exchange, routingKey, Buffer.from(content), options);

    // setTimeout(function () {
    //   pubChannel.close();
    //   amqpConn.close();
    // }, 500);
  } catch (e) {
    console.error('[AMQP Error] publish', e.message);
  }
}

module.exports.establishConnAndSendMessage = establishConnAndSendMessage;
