import Reflux from 'reflux';

import RESTClient from "../util/RESTClient";
import TrackersActions from "../actions/TrackersActions";
import {notify} from "react-notify-toast";

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

    onIssueStartTrackingRequest(trackerName, banditUUID, successCallback) {
        RESTClient.post("/trackers/show/" + trackerName + "/command/start_track_request", {bandit_uuid:banditUUID}, successCallback, function () {
            notify.show("Could not issue cancel tracking request. Please check nzyme log file.", "error");
        })
    }

    onIssueCancelTrackingRequest(trackerName, banditUUID, successCallback) {
        RESTClient.post("/trackers/show/" + trackerName + "/command/cancel_track_request", {bandit_uuid:banditUUID}, successCallback, function () {
            notify.show("Could not issue cancel tracking request. Please check nzyme log file.", "error");
        })
    }

}

export default TrackersStore;