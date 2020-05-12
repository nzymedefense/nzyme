import Reflux from 'reflux';

import RESTClient from "../util/RESTClient";
import TrackersActions from "../actions/TrackersActions";

class TrackersStore extends Reflux.Store {

    constructor() {
        super();
        this.listenables = TrackersActions;
    }

    onFindAll() {
        const self = this;

        RESTClient.get("/trackers", {}, function(response) {
            self.setState({trackers: response.data.trackers, groundstationEnabled: response.data.groundstation_enabled});
        });
    }

    onFindOne(trackerName) {
        const self = this;
        RESTClient.get("/trackers/show/" + trackerName, {}, function(response) {
            self.setState({tracker: response.data});
        });
    }

}

export default TrackersStore;