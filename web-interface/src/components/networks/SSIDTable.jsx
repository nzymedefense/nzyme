import React from 'react';
import Reflux from 'reflux';
import SSIDRow from "./SSIDRow";
import NetworksStore from "../../stores/NetworksStore";
import NetworksActions from "../../actions/NetworksActions";
import LoadingSpinner from "../misc/LoadingSpinner";

class SSIDTableRow extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = NetworksStore;

        this.stateKey = props.bssid + "_" + props.ssid;
        const state = {};
        state[this.stateKey] = undefined;
        this.state = state;
    }

    componentDidMount() {
        const ssid = this.props.ssid;
        const bssid = this.props.bssid;

        if (ssid !== "[not human readable]") {
            NetworksActions.findSSIDOnBSSID(bssid, ssid);
            setInterval(function () {
                NetworksActions.findSSIDOnBSSID(bssid, ssid)
            }, 15000);
        }
    }

    // fetch all details here.

    render() {
        const self = this;

        if (this.props.ssid === "[not human readable]") {
            return (
                <tr>
                    <td colSpan="7" style={{"text-align": "center"}}>
                        Not showing details for hidden or not human readable SSIDs.
                    </td>
                </tr>
            )
        }

        if (!this.state[this.stateKey]) {
            return (
                <tr>
                    <td colSpan="7">
                        <LoadingSpinner />
                    </td>
                </tr>
            )
        }

        return (
            <tr>
                <td colSpan="7">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                            <tr>
                                <th>SSID</th>
                                <th>Channel</th>
                                <th>Frames</th>
                                <th>Security</th>
                                <th>Beacon Rate</th>
                                <th>Signal Strength Index</th>
                            </tr>
                        </thead>
                        <tbody>
                        {Object.keys(this.state[this.stateKey].channels).map(function (key,i) {
                            return <SSIDRow key={i} ssid={self.state[self.stateKey]} channel={self.state[self.stateKey].channels[key]} />;
                        })}
                        </tbody>
                    </table>
                </td>
            </tr>
        )
    }

}

export default SSIDTableRow;