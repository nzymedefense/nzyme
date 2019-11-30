import Reflux from 'reflux';

import SystemActions from "../actions/SystemActions";
import RESTClient from "../util/RESTClient";

class SystemStore extends Reflux.Store {

    constructor() {
        super();
        this.listenables = SystemActions;
    }

    onGetStatus() {
        let self = this;

        RESTClient.get("/system/status", {}, function(response) {
            self.setState({systemStatus: response.data.status});
        });
    }

    onGetMetrics() {
        let self = this;

        RESTClient.get("/system/metrics", {}, function(response) {
            self.setState({systemMetrics: response.data.metrics});
        });
    }

    onGetVersionInfo() {
        let self = this;

        RESTClient.get("/system/version", {}, function(response) {
            self.setState({versionInfo: response.data});
        });
    }

}

export default SystemStore;