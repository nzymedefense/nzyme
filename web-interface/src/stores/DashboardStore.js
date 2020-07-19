import Reflux from 'reflux';

import RESTClient from "../util/RESTClient";
import DashboardActions from "../actions/DashboardActions";

class DashboardStore extends Reflux.Store {

    constructor() {
        super();
        this.listenables = DashboardActions;
    }

    onFindAll() {
        let self = this;

        RESTClient.get("/dashboard", {}, function(response) {
            self.setState({dashboard: response.data});
        });
    }

}

export default DashboardStore;