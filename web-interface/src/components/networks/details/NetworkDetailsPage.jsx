import React from 'react';
import Reflux from 'reflux';
import NetworkDetails from "./NetworkDetails";
import Routes from "../../../util/Routes";

class NetworkDetailsPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.bssid = decodeURIComponent(props.match.params.bssid);
        this.ssid = decodeURIComponent(props.match.params.ssid);
        this.channelNumber = decodeURIComponent(props.match.params.channel);
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <nav aria-label="breadcrumb">
                            <ol className="breadcrumb">
                                <li className="breadcrumb-item"><a href={Routes.NETWORKS.INDEX}>Networks</a></li>
                                <li className="breadcrumb-item active" aria-current="page">{this.bssid} {this.ssid} (Channel {this.channelNumber})</li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h1>Network Details</h1>
                    </div>
                </div>
                
                <NetworkDetails bssid={this.bssid} ssid={this.ssid} channelNumber={this.channelNumber} />
            </div>
        );
    }

}

export default NetworkDetailsPage;



