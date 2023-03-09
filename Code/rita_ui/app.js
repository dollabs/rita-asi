/* ------- IMPORT -------- */
// build-in
const path = require('path'); // use path.join to join several segments into 1 path

// 3rd party import
const express = require('express'); // a Node.js framework
const morgan = require('morgan'); // a middleware function that will produce a log entry
const compression = require('compression');

// custom import
const componentRouter = require('./routes/componentRoutes');
const viewRouter = require('./routes/viewRoutes');
const AppError = require('./utils/appError');
const globalErrorHandler = require('./controllers/backend/errorController');

/* ----- Start Express ----- */
const app = express();

/* ---- Setting up VIEWs ----- */
app.set('view engine', 'pug');
app.set('views', path.join(__dirname, 'views'));
app.use(express.static(path.join(__dirname, 'public'))); // can use static files (from public folder)
app.use(express.json());

/* ---- Environment status ----- */
if (process.env.NODE_ENV === 'development') {
  console.log(`We are in: ${process.env.NODE_ENV} environment`);
  app.use(morgan('dev'));
}

app.use(compression()); // compress all texts sent to clients

/* ------- ROUTES -------- */
app.use('/ritaComponents', componentRouter); // API routes: displaying json data
app.use('/', viewRouter); // Render views
app.all('*', (req, res, next) => {
  next(new AppError(`Can't find ${req.originalUrl} on this server!`, 404));
});

// CUSTOMIZE GLOBAL ERROR HANDLING MIDDLEWARE
app.use(globalErrorHandler);

module.exports = app;
