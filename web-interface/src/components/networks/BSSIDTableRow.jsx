import React from 'react';
import Reflux from 'reflux';

import SSIDTableRow from "./SSIDTable";

class BSSIDTableRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    _calculateNumberofChannels() {
        let x = 0;
        const ssids = this.props.bssid.ssids;

        Object.keys(ssids).forEach(function (key) {
            x += Object.keys(ssids[key].channels).length;
        });

        return x;
    }

    _printSSIDs(ssids) {
        let x = "";

        let total = Object.keys(ssids).length;

        Object.keys(ssids).forEach(function (key, ix) {
            let ssid = ssids[key].name.trim();

            if (ssid.length === 0 || ssid.startsWith("\u0000")) {
                ssid = "[hidden]";
            }

            x += ssid;

            if(ix < total-1) {
                x += ", ";
            }
        });

        return x;
    }

    render() {
        const self = this;

        return (
            <React.Fragment>
                <tr>
                    <td>{this.props.bssidMac}</td>
                    <td>{this.props.bssid.oui}</td>
                    <td>{this._printSSIDs(this.props.bssid.ssids)}</td>
                    <td>{this._calculateNumberofChannels()}</td>
                </tr>
            </React.Fragment>
        )
    }

}

export default BSSIDTableRow;