import Reflux from 'reflux';

import NetworksActions from "../actions/NetworksActions";
import RESTClient from "../util/RESTClient";

class NetworksStore extends Reflux.Store {

    constructor() {
        super();
        this.listenables = NetworksActions;
    }

    onFindAll(filter) {
        let self = this;

        RESTClient.get("/networks/bssids", {}, function(response) {
            let bssids = {};

            if (filter) {
                filter = filter.toLowerCase();
                Object.keys(response.data.bssids).forEach(function (key) {
                    const bssid = response.data.bssids[key];

                    // Check if BSSID matches filter.
                    if(key.toLowerCase().includes(filter)) {
                        bssids[key] = bssid;
                        return;
                    }

                    // Check if OUI matches filter.
                    if(bssid.oui.toLowerCase().includes(filter)) {
                        bssids[key] = bssid;
                        return;
                    }

                    // Check if any SSID matches filter.
                    Object.keys(bssid.ssids).forEach(function (ssid) {
                        if (ssid.toLowerCase().includes(filter)) {
                            bssids[key] = bssid;
                            return;
                        }
                    });
                });
            } else {
                bssids = response.data.bssids;
            }

            self.setState({bssids: bssids});
        });
    }

}

export default NetworksStore;