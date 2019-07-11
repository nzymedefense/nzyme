import React from 'react';
import Reflux from 'reflux';
import NetworkDetails from "./NetworkDetails";

class NetworkDetailsPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.bssid = decodeURIComponent(props.match.params.bssid);
        this.ssid = decodeURIComponent(props.match.params.ssid);
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Network Details</h1>
                    </div>
                </div>
                
                <NetworkDetails bssid={this.bssid} ssid={this.ssid} />
            </div>
        );
    }

}

export default NetworkDetailsPage;



