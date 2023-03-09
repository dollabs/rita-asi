/* eslint-disable no-await-in-loop */
/* eslint-disable no-plusplus */
const RMQ = require('../../rabbitMQ/rabbitMQ-receive');
const catchAsync = require('../../utils/catchAsync');
const RitaComponent = require('../../models/backend/ritaComponentModel');

exports.getOverview = catchAsync(async (req, res, next) => {
  // 1) Get data
  const components = await RitaComponent.find();

  // 2) Render the template with data from dbs
  res.status(200).render('overview', {
    title: 'All components',
    components,
  });
});

exports.getStateEstimation = catchAsync(async (req, res, next) => {
  // Render the template 'stateEstimation'
  res.status(200).render('stateEstimation', {
    title: 'State Estimation',
    allPredictions: RMQ.predictionArray,
  });
});

exports.getMap = catchAsync(async (req, res, next) => {
  // Render the template 'tom'
  res.status(200).render('map', {
    title: '2D Map',
  });
});


exports.getStackedGraphs = catchAsync(async (req, res, next) => {
  // Render the template 'graphOnly'
  res.status(200).render('stackedGraphs', {
    title: 'Stacked Graphs',
  });
});


