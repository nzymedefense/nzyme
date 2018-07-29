import Reflux from 'reflux';

import AlertsActions from "../actions/AlertsActions";
import RESTClient from "../util/RESTClient";

class AlertsStore extends Reflux.Store {

  constructor() {
    super();
    this.listenables = AlertsActions;
  }

  onFindActive(limit) {
    let self = this;

    RESTClient.get("/alerts/active", {limit: limit}, function(response) {
      self.setState({active_alerts: response.data.alerts});
    });
  }

}

export default AlertsStore;