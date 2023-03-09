const express = require('express');
const viewsController = require('../controllers/backend/viewsController');

const router = express.Router();

router.get('/', viewsController.getOverview); // Landing page
router.get('/state-estimation', viewsController.getStateEstimation); // state-estimation
router.get('/map', viewsController.getMap); // state-estimation
router.get('/stacked-graphs', viewsController.getStackedGraphs); // stacked-graphstate-estimation


module.exports = router;
