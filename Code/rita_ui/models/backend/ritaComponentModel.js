const mongoose = require('mongoose');

const RitaComponentSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, 'A component must have a name'],
    unique: true,
  },
  shortDes: {
    type: String,
    required: [true, 'A component must have a short description'],
  },
  longDes: {
    type: String,
    required: [true, 'A component must have a long description'],
  },
  author: {
    type: String,
    required: [true, 'A component must have an author'],
  },
  status: {
    type: String,
    required: [true, 'A component must have a status: under-construction/Working'],
    unique: true,
  },
  efficiencyLevel: {
    type: String,
    required: [true, 'A component must have an efficiency level'],
  },
  lastUpdate: {
    type: Date,
    default: Date.now(),
  },
  route: {
    type: String,
    required: [true, 'A component must have a route'],
    unique: true,
  },
});

const RitaComponent = mongoose.model('RitaComponent', RitaComponentSchema);

module.exports = RitaComponent;
