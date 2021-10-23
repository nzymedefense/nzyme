import React from 'react';
import Routes from "../../../util/Routes";
import {Redirect} from "react-router-dom";
import LoadingSpinner from "../../misc/LoadingSpinner";
import NetworksService from "../../../services/NetworksService";

class NetworkDetailsPage extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            ssid: undefined
        };

        this.service = new NetworksService();
        this.service.findSSIDOnBSSID = this.service.findSSIDOnBSSID.bind(this);

        this.bssid = decodeURIComponent(props.match.params.bssid);
        this.ssid = decodeURIComponent(props.match.params.ssid);
    }

    componentDidMount() {
        this.service.findSSIDOnBSSID(this.bssid, this.ssid, false, 0);
    }

    render() {
        if (!this.state.ssid) {
            return <LoadingSpinner />;
        }

        const mostActiveChannel = this.state.ssid.most_active_channel;

        return <Redirect to={Routes.NETWORKS.SHOW(this.bssid, this.ssid, mostActiveChannel)} />
    }

}

export default NetworkDetailsPage;



