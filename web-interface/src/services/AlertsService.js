import RESTClient from "../util/RESTClient";

class AlertsService {

    findActive(limit) {
        let self = this;

        RESTClient.get("/alerts/active", {limit: limit}, function(response) {
            self.setState({active_alerts: response.data.alerts});
        });
    }

    findAll(page) {
        let self = this;

        RESTClient.get("/alerts", {page: page}, function(response) {
            self.setState({alerts: response.data.alerts, total_alerts: response.data.total});
        });
    }

    findActiveCount() {
        let self = this;

        RESTClient.get("/alerts/active", {limit: 9999}, function(response) {
            self.setState({active_alerts_count: response.data.alerts.length});
        });
    }

    findOne(id) {
        let self = this;

        RESTClient.get("/alerts/show/" + id, {}, function(response) {
            console.log(response.data);
            self.setState({alert: response.data});
        });
    }

    getConfiguration() {
        let self = this;

        RESTClient.get("/alerts/configuration", {}, function(response) {
            self.setState({alert_configuration: response.data});
        });
    }

}

export default AlertsService;