import RESTClient from "../util/RESTClient";

class SystemService {

    getStatus() {
        let self = this;

        RESTClient.get("/system/status", {}, function(response) {
            self.setState({systemStatus: response.data.status});
        });
    }

    getMetrics() {
        let self = this;

        RESTClient.get("/system/metrics", {}, function(response) {
            self.setState({systemMetrics: response.data.metrics});
        });
    }

    getVersionInfo() {
        let self = this;

        RESTClient.get("/system/version", {}, function(response) {
            self.setState({versionInfo: response.data});
        });
    }

}

export default SystemService;