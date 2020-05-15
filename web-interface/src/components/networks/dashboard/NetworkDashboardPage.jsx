import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../../misc/LoadingSpinner";
import NetworksStore from "../../../stores/NetworksStore";
import NetworksActions from "../../../actions/NetworksActions";
import numeral from "numeral";
import AlertsStore from "../../../stores/AlertsStore";
import AlertsActions from "../../../actions/AlertsActions";

class NetworkDashboardPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.ssidParam = decodeURIComponent(props.match.params.ssid);

        this.stores = [NetworksStore, AlertsStore];

        this.state = {
            ssid: undefined,
            active_alerts: undefined
        }

        this._loadFull = this._loadFull.bind(this);
    }

    componentDidMount() {
        const self = this;

        self._loadFull();
        setInterval(function () {
            self._loadFull();
        }, 15000);
    }

    _loadFull() {
        NetworksActions.findSSID(this.ssidParam);
        AlertsActions.findActive();
    }

    render() {
        if (!this.state.ssid || !this.state.active_alerts) {
            return <LoadingSpinner />;
        }

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Network <em>{this.state.ssid.name}</em></h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-3">
                        <div className={"card dashboard-card " + (this.state.ssid.is_monitored ? "bg-success" : "bg-warning")}>
                            <div className="dashboard-align">
                                <h4>Monitoring</h4>

                                <span className="dashboard-value">
                                    {this.state.ssid.is_monitored ? "monitored" : "not monitored"}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="col-md-3">
                        <div className="card dashboard-card bg-info">
                            <div className="dashboard-align">
                                <h4>Total Frames</h4>

                                <span className="dashboard-value">
                                    {numeral(this.state.ssid.total_frames).format('0,0')}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="col-md-3">
                        <div className="card dashboard-card bg-info">
                            <div className="dashboard-align">
                                <h4>Access Points</h4>

                                <span className="dashboard-value">
                                    {numeral(this.state.ssid.bssids.length).format('0,0')}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="col-md-3">
                        <div className={"card dashboard-card " + (this.state.active_alerts.length === 0 ? "bg-success" : "bg-warning")}>
                            <div className="dashboard-align">
                                <h4>Active Alerts</h4>

                                <span className="dashboard-value">
                                    {this.state.active_alerts.length}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

}

export default NetworkDashboardPage;



