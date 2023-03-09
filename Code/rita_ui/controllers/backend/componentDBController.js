const catchAsync = require('../../utils/catchAsync');
const RitaComponents = require('../../models/backend/ritaComponentModel');
const APIFeatures = require('../../utils/apiFeatures');

exports.getAllComponents = catchAsync(async (req, res, next) => {
  // 1) Building up the query using APIFeatures class
  const features = new APIFeatures(RitaComponents.find(), req.query).filter().sort().limitFields().paginate();

  // 2) Execute the query
  const components = await features.query;

  // 3) Send response
  res.status(200).json({
    status: 'success',
    requestedAt: req.currentTime,
    results: components.length,
    data: { components },
  });
});

exports.getComponent = catchAsync(async (req, res, next) => {
  // 1) Get compoenet
  const component = await RitaComponents.findById(req.params.id);

  // 2) Send response
  res.status(200).json({
    status: 'success',
    data: { component },
  });
});

exports.createComponent = catchAsync(async (req, res, next) => {
  const newComponent = await RitaComponents.create(req.body);

  res.status(201).json({
    status: 'success',
    data: {
      tour: newComponent,
    },
  });
});

exports.updateComponent = catchAsync(async (req, res, next) => {
  const component = await RitaComponents.findByIdAndUpdate(req.params.id, req.body, {
    new: true, // new: return the modified document rather than the original
    runValidators: true,
  });

  res.status(200).json({
    status: 'success',
    data: { component },
  });
});

exports.deleteComponent = catchAsync(async (req, res, next) => {
  await RitaComponents.findByIdAndDelete(req.params.id);

  res.status(204).json({
    status: 'success',
    data: null,
  });
});
