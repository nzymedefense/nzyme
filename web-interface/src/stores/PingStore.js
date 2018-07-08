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

  onPing() {
    // NOT USING RESTClient wrapper here because it's kind of a special call with special error handler etc and we
    // can keep things simple this way.

    let self = this;

    axios.get(API_ROOT + '/ping')
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