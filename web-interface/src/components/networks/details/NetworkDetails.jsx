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

        this.state = {
            ssid: undefined
        };
    }

    componentDidMount() {
        const self = this;

        const bssid = this.props.bssid;
        const ssid = this.props.ssid;

        NetworksActions.findSSIDOnBSSID(bssid, ssid);
        setInterval(function () {
            NetworksActions.findSSIDOnBSSID(bssid, ssid)
        }, 5000);
    }

    render() {
        const self = this;

        if (!this.state.ssid) {
            return <LoadingSpinner />;
        } else {
            return (
                <div>
                    <div className="row">
                        <div className="col-md-3">
                            <dl>
                                <dt>BSSID</dt>
                                <dd>{this.state.ssid.bssid}</dd>
                            </dl>
                        </div>

                        <div className="col-md-3">
                            <dl>
                                <dt>SSID</dt>
                                <dd>{this.state.ssid.name}</dd>
                            </dl>
                        </div>
                    </div>

                    <div className="row">
                        <div className="col-md-12">
                            <hr />

                            {Object.keys(this.state.ssid.channels).map(function (key,i) {
                                return <ChannelDetails channel={self.state.ssid.channels[key]} ssid={self.state.ssid} />;
                            })}
                        </div>
                    </div>
                </div>
            );
        }
    }

}

export default NetworkDetails;



