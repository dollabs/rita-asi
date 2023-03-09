/*----- How to start the webpage ------*/
// npm start - enter the development environment
// npm run start prod - enter the production environment

/*----- DOTENV: Nodejs environment -----*/
// dotenv.config reads all the variables from the config.env file and save them to Node.js environment variables
// process.env has all the environment variables: console.log(process.env);
const dotenv = require('dotenv');
dotenv.config({ path: './config.env' });
const mongoose = require('mongoose');
const socketBackEnd = require('socket.io');
const app = require('./app');

/*----- Handle unexpected errors before starting the server -----*/
process.on('uncaughtException', (err) => {
  console.log('UNCAUGHT EXCEPTION! 💥 Shutting down...');
  console.log(err.name, err.message);
  console.log(err.stack);
  process.exit(1);
});

/* ------- Start listening to rabbitMQ -------- */
const RMQ = require('./rabbitMQ/rabbitMQ-receive');

// Rita Runs
const exchangeName = 'rita';
const routingKeys = ['testbed-message', 'predictions', 'belief-state-changes', 'ac-controller'];
RMQ.startListening(exchangeName, routingKeys);

// Reinforcement Learning Robot
// const exchangeNameRL = 'rita-v1';
// const routingKeysRL = ['observations.ui'];
// RMQ.startListening(exchangeNameRL, routingKeysRL);

/* --- DATABASE CONNECTION CONFIG -- */
const DB = process.env.DATABASE.replace('<PASSWORD>', process.env.DATABASE_PASSWORD);

mongoose
  .connect(DB, {
    useNewUrlParser: true,
    useCreateIndex: true,
    useFindAndModify: false,
    useUnifiedTopology: true,
  })
  .then(() => console.log('DB connection successful!'));

/*----- Start SERVER -----*/
const port = process.env.PORT || 3000;
const server = app.listen(port, () => {
  console.log(`App running on port ${port} ...`);
});

/*------ Socket IO Setup -------*/
const io = socketBackEnd(server);
require('./socketIO/socketIOInterface')(io);

/*----- Handle unexpected errors -----*/
process.on('unhandledRejection', (err) => {
  console.log('UNHANDLED REJECTION! 💥 Shutting down...');
  console.log(err.name, err.message);
  console.log(err.stack);
  server.close(() => {
    process.exit(1);
  });
});
