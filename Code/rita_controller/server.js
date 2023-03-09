/*----- DOTENV: Nodejs environment -----*/
const dotenv = require('dotenv');
// dotenv.config reads all the variables from the config.env file
// and save them to Node.js environment variables
dotenv.config({ path: './config.env' });

/*----- Socket IO -----*/
const socketBackEnd = require('socket.io');
const SocketIOFileUpload = require('socketio-file-upload');

/*----- Require Express App -----*/
const app = require('./app');

/*----- Handle unexpected errors before starting the server -----*/
process.on('uncaughtException', (err) => {
  console.log('UNCAUGHT EXCEPTION! 💥 Shutting down...');
  console.log(err.name, err.message);
  console.log(err.stack);
  process.exit(1);
});

/*----- Start SERVER -----*/
const port = process.env.PORT || 8080;
const server = app.listen(port, () => {
  console.log(`App running on port ${port}...`);
});

/*------ Socket IO  Setup -------*/
const io = socketBackEnd(server);
const uploader = new SocketIOFileUpload();
require('./controllers/socketController').startBackEndSocketIO(io, uploader);

/*----- Handle unexpected errors after starting the server -----*/
process.on('unhandledRejection', (err) => {
  console.log('UNHANDLED REJECTION! 💥 Shutting down...');
  console.log(err.name, err.message);
  console.log(err.stack);
  server.close(() => {
    process.exit(1);
  });
});
