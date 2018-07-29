import Reflux from 'reflux';

import PingActions from "../actions/PingActions";
import {API_ROOT} from "../util/API";

var axios = require('axios');

class PingStore extends Reflux.Store {

  constructor() {
    super();

    this.listenables = PingActions;
    setInterval(PingActions.ping, 5000);
  }

  static buildUri(uri) {
    let stableRoot = API_ROOT;
    if(API_ROOT.slice(-1) !== '/') {
      stableRoot = API_ROOT + "/";
    }

    let stableUri = uri;
    if (uri.charAt(0) === "/") {
      stableUri = uri.substr(1);
    }

    return stableRoot + stableUri;
  }

  onPing() {
    // NOT USING RESTClient wrapper here because it's kind of a special call with special error handler etc and we
    // can keep things simple this way.

    let self = this;

    axios.get(PingStore.buildUri("/ping"))
      .then(function (response) {
        if(response.data === "pong") {
          self.setState({apiConnected: true});
        } else {
          self.setState({apiConnected: false});
        }
      })
      .catch(function () {
        self.setState({apiConnected: false});
      });
  }

}

export default PingStore;