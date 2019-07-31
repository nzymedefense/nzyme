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
        RESTClient.post("/authentication/session", {username: username, password: password}, function(response) {
            Store.set("api_token", response.data.token);
        });
    }

}

export default AuthenticationStore;