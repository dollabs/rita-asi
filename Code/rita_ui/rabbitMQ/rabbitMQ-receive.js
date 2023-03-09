/* eslint-disable prefer-destructuring */
/* eslint-disable no-shadow */
/* eslint-disable import/no-extraneous-dependencies */
/* ----- IMPORT ------ */
const amqp = require('amqplib/callback_api');
const processMsg = require('./processMessage.js').processMsg;

/* ----- Initialized variables ------ */
process.env.CLOUDAMQP_URL = `amqp://localhost`;
// process.env.CLOUDAMQP_URL = `amqp://${process.env.ADDRESS}`;
// const amqpURL = 'amqp://obablkhe:vN3l2Lpj5sfUcstYJp1oQsHnR-I_0bHZ@toad.rmq.cloudamqp.com/obablkhe';

/* ----- Supporting functions ------ */
// Close the connection on errors
const closeOnErr = (err, connection) => {
  if (!err) return false;
  console.error('[AMQP] error', err);
  connection.close();
  return true;
};

// Opens a topic exchange and consumes incoming messages
const consumeMessages = async (channel, exchange, routingKeys, connection) => {
  // 1. Opens a topic exchange
  console.log(exchange);
  if (exchange === 'rita') {
    channel.assertExchange(exchange, 'topic', { durable: false, autoDelete: false });
  } else {
    channel.assertExchange(exchange, 'topic', { durable: false, autoDelete: true });
  }

  // 2. Creates a temporary queue, which is automatically deleted when job done
  // queue is the respond consist of : { queue: 'randome-name', messageCount: 0, consumerCount: 0}
  channel.assertQueue('', { durable: true }, (err, queue) => {
    if (closeOnErr(err, connection)) return;

    // 3. Binds temporary queue to exchange
    routingKeys.forEach((key) => {
      channel.bindQueue(queue.queue, exchange, key); // bindQueue(queue, source, pattern, [args])
    });

    // 4. Consumes messages
    channel.consume(queue.queue, processMsg, {
      noAck: true,
    });
    console.log('[*] Waiting for messages:');
  });
};

// Opens a channel in the established connection & starts consuming messages
const establishChannel = (connection, exchange, routingKeys) => {
  connection.createChannel((err, channel) => {
    // Handles errors
    if (closeOnErr(err, connection)) return;

    channel.on('error', function (err) {
      console.error('[AMQP] channel error', err.message);
    });

    channel.on('close', function () {
      console.log('[AMQP] channel closed');
    });

    // establishes channel
    channel.prefetch(10); // How many messages are being sent to the consumer at the same time.

    consumeMessages(channel, exchange, routingKeys, connection);
  });
};

/* ----- Establishes a connection to RabbitMQ & starts listening ------ */
const start = async (exchange, routingKeys) => {
  // 1. Establishes connection
  amqp.connect(`${process.env.CLOUDAMQP_URL}`, async (err, connection) => {
    // Error handling
    if (err) {
      console.error('[AMQP] starting to connect error', err.message);
      return setTimeout(start, 1000); // try to reconnect
    }
    connection.on('error', function (err) {
      if (err.message !== 'Connection closing') {
        console.error('[AMQP] connection error', err.message);
      }
    });
    connection.on('close', function () {
      console.error('[AMQP] reconnecting');
      return setTimeout(start, 1000); // try to reconnect
    });

    // Set up amqp Connection
    console.log('[AMQP] connected');

    // 2. starts listening
    establishChannel(connection, exchange, routingKeys);
  });
};

/* -----  Testing purpose ------ */
// const exchangeName = 'rita';
// const routingKeys = ['belief-state-changes', 'predictions', 'dog', 'cat']; // belief-state-changes
// start(exchangeName, routingKeys);

/* -----  Exports ------ */
module.exports.startListening = start;
