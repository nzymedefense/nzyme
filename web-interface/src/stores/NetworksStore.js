import Reflux from 'reflux';

import NetworksActions from "../actions/NetworksActions";
import RESTClient from "../util/RESTClient";

import {notify} from "react-notify-toast";

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

    onFindSSIDOnBSSID(bssid, ssid, includeHistory = false, historySeconds = 600) {
        let self = this;

        RESTClient.get("networks/bssids/" + encodeURIComponent(bssid) + "/ssids/" + encodeURIComponent(ssid), {include_history: includeHistory, history_seconds: historySeconds}, function(response) {
            const key = bssid + "_" + ssid;
            const state = {};
            state[key] = response.data;
            self.setState(state);
        });
    }

    onResetFingerprints() {
        RESTClient.post("/networks/fingerprints/reset/", {}, function() {
            notify.show("Fingerprints reset.", "success");
        })
    }

}

export default NetworksStore;