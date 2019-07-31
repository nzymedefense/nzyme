import Reflux from 'reflux';

import AuthenticationActions from "../actions/AuthenticationActions";
import RESTClient from "../util/RESTClient";
import Store from "../util/Store";

class AuthenticationStore extends Reflux.Store {

    constructor() {
        super();
        this.listenables = AuthenticationActions;
    }

    onCreateSession(username, password) {
        let self = this;

        RESTClient.post("/authentication/session", {username: username, password: password}, function(response) {
            Store.set("api_token", response.data.token);
        }, function(response) {
            self.setState({loggingIn: false});
        });
    }

    onCheckSession() {
        RESTClient.get("/authentication/session/information", {}, function(response) {
            if(response.data.seconds_left_valid <= 60) {
                Store.delete("api_token");
            }
        });
    }

}

export default AuthenticationStore;