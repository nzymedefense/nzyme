import Reflux from 'reflux';

import StatisticsActions from "../actions/StatisticsActions";
import RESTClient from "../util/RESTClient";

class StatisticsStore extends Reflux.Store {

  constructor() {
    super();
    this.listenables = StatisticsActions;
  }

  onFindGlobal() {
    let self = this;

    RESTClient.get("/system/networks/global", {}, function(response) {
      self.setState({global_statistics: response.data});
    });
  }

}

export default StatisticsStore;