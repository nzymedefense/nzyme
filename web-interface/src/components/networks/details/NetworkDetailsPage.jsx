import React from 'react';
import Reflux from 'reflux';
import AlertsStore from "../../../stores/AlertsStore";

class NetworkDetailsPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.bssid = props.match.params.bssid;
        this.ssid = props.match.params.ssid;
        this.channel = props.match.params.channel;

        this.state = {
            alert: undefined
        };
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Network Details</h1>

                        {this.bssid} {this.ssid} {this.channel}
                    </div>
                </div>
            </div>
        );
    }

}

export default NetworkDetailsPage;



