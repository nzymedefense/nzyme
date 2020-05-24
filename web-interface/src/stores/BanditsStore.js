import Reflux from 'reflux';

import RESTClient from "../util/RESTClient";
import BanditsActions from "../actions/BanditsActions";
import {notify} from "react-notify-toast";

class BanditsStore extends Reflux.Store {

    constructor() {
        super();
        this.listenables = BanditsActions;
    }

    onFindAll() {
        const self = this;

        RESTClient.get("/bandits", {}, function(response) {
            self.setState({bandits: response.data.bandits});
        });
    }

    onFindOne(id) {
        const self = this;

        RESTClient.get("/bandits/show/" + id, {}, function(response) {
            self.setState({bandit: response.data});
        });
    }

    onCreateBandit(name, description, successCallback, errorCallback) {
        RESTClient.post("/bandits", {name: name, description: description}, function() {
            successCallback();
        }, function() {
            errorCallback();
        });
    }

    onUpdateBandit(id, name, description, successCallback, errorCallback) {
        RESTClient.put("/bandits/show/" + id, {name: name, description: description}, function() {
            successCallback();
        }, function() {
            errorCallback();
        });
    }

    onDeleteBandit(banditUUID, successCallback) {
        RESTClient.delete("/bandits/show/" + banditUUID, function () {
            successCallback();
        }, function() {
            notify.show("Could not delete bandit. Please check nzyme log file.", "error");
        });
    }

    onFindAllIdentifierTypes() {
        const self = this;

        RESTClient.get("/bandits/identifiers/types", {}, function(response) {
            self.setState({banditIdentifierTypes: response.data.types});
        });
    }

    onCreateIdentifier(banditUUID, createRequest) {
        const self = this;

        RESTClient.post("/bandits/show/" + banditUUID + "/identifiers", createRequest, function() {
            self.setState({submitting: false, submitted: true});
            notify.show("Identifier created.", "success");
        }, function() {
            self.setState({submitting: false, submitted: false});
            notify.show("Could not create identifier. Please check nzyme log file.", "error");
        });
    }

    onDeleteIdentifier(banditUUID, identifierUUID, successCallback) {
        const self = this;

        RESTClient.delete("/bandits/show/" + banditUUID + "/identifiers/" + identifierUUID, successCallback, function() {
            self.setState({submitting: false, submitted: false});
            notify.show("Could not delete identifier. Please check nzyme log file.", "error");
        });
    }

}

export default BanditsStore;