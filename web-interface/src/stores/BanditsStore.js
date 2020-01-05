import Reflux from 'reflux';

import RESTClient from "../util/RESTClient";
import BanditsActions from "../actions/BanditsActions";

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
        RESTClient.post("/bandits", {name: name, description: description}, function(response) {
            successCallback();
        }, function(response) {
            errorCallback();
        });
    }

}

export default BanditsStore;