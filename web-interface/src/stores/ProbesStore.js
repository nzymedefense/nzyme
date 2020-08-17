import Reflux from 'reflux';

import ProbesActions from "../actions/ProbesActions";
import RESTClient from "../util/RESTClient";

class ProbesStore extends Reflux.Store {

  constructor() {
    super();
    this.listenables = ProbesActions;
  }

  onFindAll() {
    let self = this;
    RESTClient.get("/system/probes", {}, function(response) {
      self.setState({probes: response.data.probes});
    });
  }

  onFindAllTraps() {
    let self = this;
    RESTClient.get("/system/probes/traps", {}, function(response) {
      self.setState({traps: response.data.traps});
    });
  }

  onFindCurrentChannels() {
    let self = this;

    RESTClient.get("/system/probes/channels", {}, function(response) {
      self.setState({current_channels: response.data});
    });
  }

}

export default ProbesStore;