import Reflux from 'reflux';

import ProbesActions from "../actions/ProbesActions";
import RESTClient from "../util/RESTClient";

class ProbesStore extends Reflux.Store {

  constructor() {
    super();
    this.listenables = ProbesActions;
  }

  onFindCurrentChannels() {
    let self = this;

    RESTClient.get("/probes/channels", {}, function(response) {
      self.setState({current_channels: response.data});
    });
  }

}

export default ProbesStore;