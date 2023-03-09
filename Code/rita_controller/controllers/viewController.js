const catchAsync = require('../utils/catchAsync');

exports.getPage = catchAsync(async (req, res, next) => {
  res.status(200).render('landing', {
    title: 'Main page',
  });
});
