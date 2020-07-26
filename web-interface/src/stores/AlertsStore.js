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

  onFindAll(page) {
    let self = this;

    RESTClient.get("/alerts", {page: page}, function(response) {
      self.setState({alerts: response.data.alerts, total_alerts: response.data.total});
    });
  }

  onFindActiveCount() {
    let self = this;

    RESTClient.get("/alerts/active", {limit: 9999}, function(response) {
      self.setState({active_alerts_count: response.data.alerts.length});
    });
  }

  onFindOne(id) {
    let self = this;

    RESTClient.get("/alerts/show/" + id, {}, function(response) {
      console.log(response.data);
      self.setState({alert: response.data});
    });
  }

  onGetConfiguration() {
    let self = this;

    RESTClient.get("/alerts/configuration", {}, function(response) {
      self.setState({alert_configuration: response.data});
    });
  }

}

export default AlertsStore;