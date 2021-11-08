import RESTClient from "../util/RESTClient";
import {notify} from "react-notify-toast";

class BanditsService {

    findAll() {
        const self = this;

        RESTClient.get("/bandits", {}, function(response) {
            self.setState({bandits: response.data.bandits});
        });
    }

    findOne(id) {
        const self = this;

        RESTClient.get("/bandits/show/" + id, {}, function(response) {
            self.setState({bandit: response.data});
        });
    }

    findContactOfBandit(banditUUID, contactUUID, detailed_ssids, detailed_bssids) {
        const self = this;

        RESTClient.get("/bandits/show/" + banditUUID + "/contacts/" + contactUUID, {"detailed_ssids": detailed_ssids, "detailed_bssids": detailed_bssids}, function(response) {
            self.setState({contact: response.data});
        });
    }

    createBandit(name, description, successCallback, errorCallback) {
        RESTClient.post("/bandits", {name: name, description: description}, function() {
            successCallback();
        }, function() {
            errorCallback();
        });
    }

    updateBandit(id, name, description, successCallback, errorCallback) {
        RESTClient.put("/bandits/show/" + id, {name: name, description: description}, function() {
            successCallback();
        }, function() {
            errorCallback();
        });
    }

    deleteBandit(banditUUID, successCallback) {
        RESTClient.delete("/bandits/show/" + banditUUID, function () {
            successCallback();
        }, function() {
            notify.show("Could not delete bandit. Please check nzyme log file.", "error");
        });
    }

    findAllIdentifierTypes() {
        const self = this;

        RESTClient.get("/bandits/identifiers/types", {}, function(response) {
            self.setState({banditIdentifierTypes: response.data.types});
        });
    }

    createIdentifier(banditUUID, createRequest) {
        const self = this;

        RESTClient.post("/bandits/show/" + banditUUID + "/identifiers", createRequest, function() {
            self.setState({submitting: false, submitted: true});
            notify.show("Identifier created.", "success");
        }, function() {
            self.setState({submitting: false, submitted: false});
            notify.show("Could not create identifier. Please check nzyme log file.", "error");
        });
    }

    deleteIdentifier(banditUUID, identifierUUID, successCallback) {
        const self = this;

        RESTClient.delete("/bandits/show/" + banditUUID + "/identifiers/" + identifierUUID, successCallback, function() {
            self.setState({submitting: false, submitted: false});
            notify.show("Could not delete identifier. Please check nzyme log file.", "error");
        });
    }

}

export default BanditsService;