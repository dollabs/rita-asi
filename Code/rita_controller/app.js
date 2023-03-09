// build-in
const path = require('path'); // use path.join to join several segments into 1 path
const events = require('events');

// 3rd party import
const express = require('express');
const morgan = require('morgan');
const compression = require('compression');
const upload = require('express-fileupload');
const SocketIOFileUpload = require('socketio-file-upload');

// custom import
const AppError = require('./models/backend/appError');
const globalErrorHandler = require('./controllers/errorController');
const viewRouter = require('./routes/viewRoutes');

/* ----- Start Express ----- */
const app = express();

/* ---- Setting up VIEWs ----- */
app.set('view engine', 'pug');
app.set('views', path.join(__dirname, 'views'));
app.use(express.static(path.join(__dirname, 'public'))); // can use static files (from public folder)
app.use(express.json());
app.use(upload());
app.use(SocketIOFileUpload.router);

/* ---- Environment status ----- */
console.log(`We are in: ${process.env.NODE_ENV} environment`);
if (process.env.NODE_ENV === 'development') {
  app.use(morgan('dev')); // a middleware function that will produce a log entry
}

app.use(compression()); // compress all texts sent to clients

/* ------- ROUTES -------- */
app.use('/', viewRouter); // Render views

app.all('*', (req, res, next) => {
  next(new AppError(`Can't find ${req.originalUrl} on this server!`, 404));
});

// CUSTOMIZE GLOBAL ERROR HANDLING MIDDLEWARE
app.use(globalErrorHandler);

module.exports = app;
