import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../../misc/LoadingSpinner";
import NetworksStore from "../../../stores/NetworksStore";
import NetworksActions from "../../../actions/NetworksActions";
import numeral from "numeral";
import AlertsStore from "../../../stores/AlertsStore";
import AlertsActions from "../../../actions/AlertsActions";
import SimpleLineChart from "../../charts/SimpleLineChart";

class NetworkDashboardPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.ssidParam = decodeURIComponent(props.match.params.ssid);

        this.stores = [NetworksStore, AlertsStore];

        this.state = {
            ssid: undefined,
            active_alerts: undefined,
            notFound: false
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

    _formatBeaconRateHistory(data) {
        const result = [];

        if (data) {
            for (let [bssid, bssidData] of Object.entries(data)) {
                const br = {
                    x: [],
                    y: [],
                    type: "scatter",
                    name: "Beacon Rate for BSSID " + bssid,
                    line: {width: 1, shape: "linear"},
                };

                bssidData.forEach(function (point) {
                    const date = new Date(point["created_at"]);
                    br["x"].push(date);
                    br["y"].push(point["rate"]);
                });

                result.push(br);
            }
        }

        return result;
    }

    render() {
        /*
         * Handling 404's a little more specific to work better on wall-mounted screens.
         *
         * For example, right after a restart of nzyme, the network might not be in the
         * internal networks table yet. ... or it might have just disappeared and we
         * shouldn't show outdated data in that case, but a warning.
         */
        if (this.state.notFound) {
            return (
                <div className="row">
                    <div className="col-md-6 offset-md-3 mt-md-3">
                        <div className="alert alert-danger">
                            <h2><i className="fas fa-exclamation-triangle" /> Network Not Found.</h2>
                            <p>
                                The requested network was not found. It has either not appeared yet, or nzyme is not
                                configured to scan for it on channels that it is communicating on.
                            </p>
                            <p>
                                This page will keep on refreshing, trying to find the network.
                            </p>
                        </div>
                    </div>
                </div>
            )
        }

        if (!this.state.ssid || !this.state.active_alerts) {
            return <LoadingSpinner />;
        }

        return (
            <div className="dashboard">
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
                        <div className={"card dashboard-card " + (this.state.active_alerts.length === 0 ? "bg-success" : "bg-danger blink")}>
                            <div className="dashboard-align">
                                <h4>Active Alerts</h4>

                                <span className="dashboard-value">
                                    {this.state.active_alerts.length}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="row lower-row">
                    <div className="col-md-6">

                    </div>

                    <div className="col-md-6">
                        <div className="card dashboard-card dashboard-card-high bg-dark">
                            <div className="dashboard-manual-align">
                                <h4>Beacon Rates</h4>

                                <div className="dashboard-chart">
                                    <SimpleLineChart
                                        height={235}
                                        customMarginLeft={20}
                                        customMarginRight={20}
                                        customMarginTop={1}
                                        customMarginBottom={15}
                                        backgroundColor="#32334a"
                                        textColor="#ffffff"
                                        disableHover={true}
                                        finalData={this._formatBeaconRateHistory(this.state.ssid.beacon_rates)}
                                    />
                                </div>
                            </div>
                        </div>

                    </div>

                </div>
            </div>
        );
    }

}

export default NetworkDashboardPage;



