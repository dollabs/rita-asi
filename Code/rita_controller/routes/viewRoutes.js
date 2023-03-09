const express = require('express');
const viewsController = require('../controllers/viewController');

const router = express.Router();

router.get('/', viewsController.getPage); // Landing page

module.exports = router;
