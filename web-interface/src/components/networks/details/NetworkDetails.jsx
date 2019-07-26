import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../../overview/AlertsList";
import NetworksStore from "../../../stores/NetworksStore";
import NetworksActions from "../../../actions/NetworksActions";
import ChannelDetails from "./ChannelDetails";

class NetworkDetails extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = NetworksStore;

        this.stateKey = props.bssid + "_" + props.ssid;
        const state = {};
        state[this.stateKey] = undefined;
        this.state = state;
    }

    componentDidMount() {
        const self = this;

        const bssid = this.props.bssid;
        const ssid = this.props.ssid;

        NetworksActions.findSSIDOnBSSID(bssid, ssid, true);
        setInterval(function () {
            NetworksActions.findSSIDOnBSSID(bssid, ssid, true)
        }, 15000);
    }

    render() {
        const self = this;

        const ssid = this.state[this.stateKey];

        if (!ssid) {
            return <LoadingSpinner />;
        } else {
            return (
                <div>
                    <div className="row">
                        <div className="col-md-3">
                            <dl>
                                <dt>BSSID</dt>
                                <dd>{ssid.bssid}</dd>
                            </dl>
                        </div>

                        <div className="col-md-3">
                            <dl>
                                <dt>SSID</dt>
                                <dd>{ssid.name}</dd>
                            </dl>
                        </div>
                    </div>

                    <div className="row">
                        <div className="col-md-12">
                            <hr />

                            {Object.keys(ssid.channels).map(function (key,i) {
                                return <ChannelDetails channel={ssid.channels[key]} ssid={ssid} />;
                            })}
                        </div>
                    </div>
                </div>
            );
        }
    }

}

export default NetworkDetails;



