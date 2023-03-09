export default class Search {
  constructor(query) {
    this.query = query;
    this.result = '';
  }

  async getResults() {
    try {
      //   const res = await for data;
      //   this.result = res;
    } catch (error) {
      alert(error);
    }
  }
}
