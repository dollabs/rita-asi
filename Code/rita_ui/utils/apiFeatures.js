// APIFeatures class helps building the MONGOOSE QUERY
class APIFeatures {
  // query = mongoose query
  // queryString = query string coming from the route
  constructor(query, queryString) {
    this.query = query;
    this.queryString = queryString;
  }

  // Filtering
  filter() {
    // 1A) Simple filtering: do not include sort, fields, limit and page
    const queryObj = { ...this.queryString };
    const excludedFields = ['page', 'sort', 'limit', 'fields'];
    excludedFields.forEach((el) => delete queryObj[el]);

    // 1B) Advanced filtering (gte, gt, lte, lt)
    // { duration: { gte: '3' }, difficulty: 'easy' } : This is what we get
    // { duration: { $gte: '5' }, difficult: 'easy'} : This is what we want
    let queryStr = JSON.stringify(queryObj);
    queryStr = queryStr.replace(/\b(gte|gt|lte|lt)\b/g, (match) => `$${match}`);

    console.log(`Advanced Filtering: ${JSON.parse(queryStr)}`);

    this.query = this.query.find(JSON.parse(queryStr));

    return this;
  }

  // Sorting: sort the returned documents based on...
  // What we get from req.query.sort: price,ratingsAverage
  // What we want: -price ratingsAverage
  sort() {
    if (this.queryString.sort) {
      const sortBy = this.queryString.sort.split(',').join(' ');
      this.query = this.query.sort(sortBy);
    } else {
      this.query = this.query.sort('-createdAt');
    }

    return this;
  }

  // Field Limiting: only returns selected fields (ex: name, price, etc.)
  limitFields() {
    if (this.queryString.fields) {
      const fields = this.queryString.fields.split(',').join(' ');
      this.query = this.query.select(fields);
    } else {
      this.query = this.query.select('-__v');
    }

    return this;
  }

  // Pagination: how many documents should each page holds and which page I want to go to?
  paginate() {
    const page = this.queryString.page * 1 || 1; // jump to this page, default = page 1
    const limit = this.queryString.limit * 1 || 100; // page limit, default = 100/page
    const skip = (page - 1) * limit; // All the results that come before the actual results we want to see
    this.query = this.query.skip(skip).limit(limit);

    return this;
  }
}

module.exports = APIFeatures;
