import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../../misc/LoadingSpinner";
import NetworksStore from "../../../stores/NetworksStore";
import NetworksActions from "../../../actions/NetworksActions";
import numeral from "numeral";
import AlertsStore from "../../../stores/AlertsStore";
import AlertsActions from "../../../actions/AlertsActions";
import SimpleLineChart from "../../charts/SimpleLineChart";
import StatisticsActions from "../../../actions/StatisticsActions";
import StatisticsStore from "../../../stores/StatisticsStore";
import HeatmapWaterfallChart from "../../charts/HeatmapWaterfallChart";

class NetworkDashboardPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.ssidParam = decodeURIComponent(props.match.params.ssid);

        this.stores = [NetworksStore, AlertsStore, StatisticsStore];

        this.state = {
            globalSSID: undefined,
            ssid: undefined,
            active_alerts: undefined,
            notFound: false,
            width: undefined,
            height: undefined,
            fillHeight: 0,
            doubleColumnWidth: 0,
            cycleCount: 0,
            currentBSSID: undefined
        }

        this.lastRow = React.createRef();
        this.doubleColumn = React.createRef();

        this._loadFull = this._loadFull.bind(this);
        this._updateDynamicSizes = this._updateDynamicSizes.bind(this);
    }

    componentDidMount() {
        const self = this;

        setInterval(this._updateDynamicSizes, 1000);

        self._loadFull();
        setInterval(function () {
            self._loadFull();
        }, 60000);
    }

    _updateDynamicSizes() {
        let fillHeight;
        if (this.lastRow.current) {
            fillHeight = this.state.height-this.lastRow.current.getBoundingClientRect().top-20;
        } else {
            fillHeight = 0;
        }

        let doubleColumnWidth;
        if (this.doubleColumn.current) {
            doubleColumnWidth = this.doubleColumn.current.getBoundingClientRect().width;
        } else {
            doubleColumnWidth = 0;
        }


        this.setState({
            width: window.innerWidth,
            height: window.innerHeight,
            fillHeight: fillHeight,
            doubleColumnWidth: doubleColumnWidth
        });
    }

    _loadFull() {
        const self = this;
        const ssid = this.ssidParam;
        NetworksActions.findSSID(ssid, function (response) {
            const nextCycleCount = self.state.cycleCount < response.data.bssids.length-1 ? self.state.cycleCount+1 : 0;
            const currentBSSID = response.data.bssids[self.state.cycleCount];
            self.setState({
                globalSSID: response.data,
                notFound: false,
                currentBSSID: currentBSSID,
                cycleCount: nextCycleCount
            });

            NetworksActions.findSSIDOnBSSID(currentBSSID, ssid, true, 86400);
        });

        AlertsActions.findActive();
        StatisticsActions.findGlobal();
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

    _buildBeaconRateHistoryShapes(data, threshold) {
        const beaconRateData = Object.values(data)[0];

        if (!threshold || !beaconRateData) {
            return [];
        }

        return [
            {
                type: "line",
                visible: true,
                x0: new Date(beaconRateData[0].created_at),
                x1: new Date(beaconRateData[beaconRateData.length-1].created_at),
                y0: threshold,
                y1: threshold,
                line: {
                    color: "#d50000",
                    dash: "dash",
                    width: 1,
                }
            }
        ];
    }

    _formatSignalIndexHeatmap(data) {
        const yDates = [];

        Object.keys(data.y).forEach(function(point) {
            yDates.push(new Date(data.y[point]));
        });

        return {
            "z": data.z,
            "x": data.x,
            "y": yDates
        };
    }

    _buildSignalIndexHeatmapTracks(data, tracks) {
        const shapes = [];
        const annotations = [];

        const firstDate = new Date(data.y[0]);
        const lastDate = new Date(data.y[data.y.length-1]);

        // Tracks.
        if(tracks) {
            Object.keys(tracks).forEach(function(t) {
                const track = tracks[t];

                // Left.
                shapes.push(
                    {
                        type: "line",
                        visible: true,
                        x0: track.min_signal,
                        x1: track.min_signal,
                        y0: new Date(track.start),
                        y1: new Date(track.end),
                        line: {
                            color: "#ff0000",
                            dash: "dashdot",
                            width: 3,
                        }
                    }
                );

                // Right.
                shapes.push(
                    {
                        type: "line",
                        visible: true,
                        x0: track.max_signal,
                        x1: track.max_signal,
                        y0: new Date(track.start),
                        y1: new Date(track.end),
                        line: {
                            color: "#ff0000",
                            dash: "dashdot",
                            width: 3,
                        }
                    }
                );

                // Top.
                let topComplete = false;
                if (new Date(track.end).getTime() !== lastDate.getTime()) {
                    topComplete = true;
                    shapes.push(
                        {
                            type: "line",
                            visible: true,
                            x0: track.min_signal,
                            x1: track.max_signal,
                            y0: new Date(track.end),
                            y1: new Date(track.end),
                            line: {
                                color: "#ff0000",
                                dash: "dashdot",
                                width: 3,
                            }
                        }
                    );
                }

                // Bottom.
                let bottomComplete = false;
                if (new Date(track.start).getTime() !== firstDate.getTime()) {
                    bottomComplete = true;
                    shapes.push(
                        {
                            type: "line",
                            visible: true,
                            x0: track.min_signal,
                            x1: track.max_signal,
                            y0: new Date(track.start),
                            y1: new Date(track.start),
                            line: {
                                color: "#ff0000",
                                dash: "dashdot",
                                width: 3,
                            }
                        }
                    );
                }

                // Annotations.
                if (topComplete && bottomComplete) {
                    annotations.push(
                        {
                            type: "text",
                            align: "left",
                            font: {
                                color: "#ff0000"
                            },
                            text: "track<br>" + track.id,
                            showarrow: false,
                            x: track.max_signal,
                            y: new Date(track.end),
                            xshift: 25,
                            yshift: -10,
                        }
                    )
                }
            });
        }


        return {shapes: shapes, annotations: annotations};
    }

    _formatSignalIndexDistribution(channels) {
        const result = [];

        Object.keys(channels).forEach(function(channelNumber) {
            const data = channels[channelNumber].signal_index_distribution;

            const distribution = {
                x: [],
                y: [],
                type: "bar",
                name: "Channel " + channelNumber
            };

            // We want a static scale from -100 to 0.
            distribution["x"].push(-100);
            distribution["y"].push(0);
            distribution["x"].push(0);
            distribution["y"].push(0);

            Object.keys(data).forEach(function(point) {
                distribution["x"].push(point);
                distribution["y"].push(data[point]);
            });

            result.push(distribution);
        });

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

        if (!this.state.globalSSID || !this.state.active_alerts || !this.state.global_statistics || !this.state.ssid) {
            return <LoadingSpinner />;
        }

        return (
            <div className="dashboard">
                <div className="row">
                    <div className="col-md-12">
                        <h1>Network <em>{this.state.globalSSID.name}</em></h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-3">
                        <div className={"card dashboard-card " + (this.state.globalSSID.is_monitored ? "bg-success" : "bg-warning")}>
                            <div className="dashboard-align">
                                <h4>Monitoring</h4>

                                <span className="dashboard-value">
                                    {this.state.globalSSID.is_monitored ? "monitored" : "not monitored"}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="col-md-3">
                        <div className="card dashboard-card bg-info">
                            <div className="dashboard-align">
                                <h4>Total Frames</h4>

                                <span className="dashboard-value">
                                    {numeral(this.state.globalSSID.total_frames).format('0,0')}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="col-md-3">
                        <div className="card dashboard-card bg-info">
                            <div className="dashboard-align">
                                <h4>Access Points</h4>

                                <span className="dashboard-value">
                                    {numeral(this.state.globalSSID.bssids.length).format('0,0')}
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
                        <div className="card dashboard-card dashboard-card-high bg-dark">
                            <div className="dashboard-manual-align">
                                <h4>Global Frame Throughput</h4>

                                <div className="dashboard-chart">
                                    <SimpleLineChart
                                        height={235}
                                        width={this.state.doubleColumnWidth-5}
                                        customMarginLeft={30}
                                        customMarginRight={30}
                                        customMarginTop={1}
                                        customMarginBottom={15}
                                        backgroundColor="#32334a"
                                        textColor="#ffffff"
                                        disableHover={true}
                                        data={this.state.global_statistics.histogram_frame_throughput}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="col-md-6">
                        <div className="card dashboard-card dashboard-card-high bg-dark">
                            <div className="dashboard-manual-align">
                                <h4>Beacon Rates</h4>

                                <div className="dashboard-chart">
                                    <SimpleLineChart
                                        height={235}
                                        width={this.state.doubleColumnWidth-5}
                                        customMarginLeft={30}
                                        customMarginRight={30}
                                        customMarginTop={1}
                                        customMarginBottom={15}
                                        backgroundColor="#32334a"
                                        textColor="#ffffff"
                                        disableHover={true}
                                        finalData={this._formatBeaconRateHistory(this.state.globalSSID.beacon_rates)}
                                        shapes={this._buildBeaconRateHistoryShapes(
                                            this.state.globalSSID.beacon_rates,
                                            this.state.globalSSID.beacon_rate_threshold
                                        )}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="row lower-row" ref={this.lastRow} style={{display: (this.state.height > 900 && this.state.fillHeight > 0) ? undefined : "none"}}>
                    <div className="col-md-6">
                        <div className="card dashboard-card bg-dark" ref={this.doubleColumn} style={{height:this.state.fillHeight}}>
                            <div className="dashboard-manual-align">
                                <h4>Waterfall - BSSID: {this.state.currentBSSID} <small>({this.state.cycleCount+1}/{this.state.globalSSID.bssids.length})</small>,
                                    Channel: {this.state.ssid.most_active_channel}</h4>

                                <div className="dashboard-chart">
                                    <HeatmapWaterfallChart
                                        hovertemplate="Signal Strength: %{x} dBm, %{z} frames at %{y}<extra></extra>"
                                        backgroundColor="#32334a"
                                        height={this.state.fillHeight-60}
                                        width={this.state.doubleColumnWidth-5}
                                        customMarginTop={10}
                                        customMarginBottom={20}
                                        customMarginLeft={50}
                                        customMarginRight={5}
                                        disableHover={true}
                                        data={this._formatSignalIndexHeatmap(this.state.ssid.channels[this.state.ssid.most_active_channel].signal_index_history)}
                                        layers={this._buildSignalIndexHeatmapTracks(
                                            this.state.ssid.channels[this.state.ssid.most_active_channel].signal_index_history,
                                            this.state.ssid.channels[this.state.ssid.most_active_channel].signal_index_tracks)
                                        }
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="col-md-6">
                        <div className="card dashboard-card bg-dark" ref={this.doubleColumn} style={{height:this.state.fillHeight}}>
                            <div className="dashboard-manual-align">
                                <h4>Signals - BSSID: {this.state.currentBSSID} <small>({this.state.cycleCount+1}/{this.state.globalSSID.bssids.length})</small></h4>

                                <div className="dashboard-chart">
                                    <SimpleLineChart
                                        height={this.state.fillHeight-60}
                                        width={this.state.doubleColumnWidth-5}
                                        customMarginLeft={30}
                                        customMarginRight={30}
                                        customMarginTop={1}
                                        customMarginBottom={15}
                                        backgroundColor="#32334a"
                                        textColor="#ffffff"
                                        disableHover={true}
                                        finalData={this._formatSignalIndexDistribution(this.state.ssid.channels)}
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



