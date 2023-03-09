const express = require('express');
const componentController = require('../controllers/backend/componentDBController');

/* --------------------------------- */
/* ----------- ROUTES -------------- */
/* --------------------------------- */

// express.Router() is a middleware ;; also known as the Mounting technique
const Router = express.Router();

Router.route('/').get(componentController.getAllComponents).post(componentController.createComponent);
Router.route('/:id').get(componentController.getComponent).patch(componentController.updateComponent).delete(componentController.deleteComponent);

module.exports = Router;
