import React from 'react';
import Reflux from 'reflux';

import numeral from "numeral";
import SignalIndex from "../SignalIndex";
import SimpleLineChart from "../../charts/SimpleLineChart";
import BeaconRate from "./BeaconRate";

class ChannelDetails extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    _formatSignalQualityHistory(data) {
        const result = [];

        const avgSignalQuality = {
            x: [],
            y: [],
            type: "scatter",
            name: "Signal Quality",
            line: {width: 1, shape: "linear", color: "#2983fe"}
        };

        const avgDeltaLower = {
            x: [],
            y: [],
            type: "scatter",
            name: "Expected Lower Boundary",
            line: {width: 1, shape: "linear", color: "#588918"}
        };

        const avgDeltaUpper = {
            x: [],
            y: [],
            type: "scatter",
            name: "Expected Upper Boundary",
            line: {width: 1, shape: "linear", color: "#a80000"}
        };

        Object.keys(data).map(function (key) {
            const point = data[key];
            const date = new Date(point["created_at"]);
            avgSignalQuality["x"].push(date);
            avgSignalQuality["y"].push(point["average_signal_quality"]);

            avgDeltaLower["x"].push(date);
            avgDeltaLower["y"].push(point["average_expected_delta_lower"]);

            avgDeltaUpper["x"].push(date);
            avgDeltaUpper["y"].push(point["average_expected_delta_upper"]);
        });

        result.push(avgSignalQuality);
        result.push(avgDeltaLower);
        result.push(avgDeltaUpper);

        return result;
    }

    _formatSignalIndexHistory(data) {
        const result = [];

        const avgSignalIndex = {
            x: [],
            y: [],
            type: "scatter",
            name: "Signal Index",
            line: {width: 1, shape: "linear", color: "#2983fe"}
        };

        const avgSignalIndexThreshold = {
            x: [],
            y: [],
            type: "scatter",
            name: "Signal Index Threshold",
            line: {width: 1, shape: "linear", color: "#a80000"}
        };


        Object.keys(data).map(function (key) {
            const point = data[key];
            const date = new Date(point["created_at"]);
            avgSignalIndex["x"].push(date);
            avgSignalIndex["y"].push(point["average_signal_index"]);

            avgSignalIndexThreshold["x"].push(date);
            avgSignalIndexThreshold["y"].push(point["average_signal_index_threshold"]);
        });

        result.push(avgSignalIndex);
        result.push(avgSignalIndexThreshold);

        return result;
    }

    _formatBeaconRateHistory(data) {
        const result = [];

        const avgBeaconRate = {
            x: [],
            y: [],
            type: "bar",
            name: "Beacon Rate",
            line: {width: 1, shape: "linear", color: "#2983fe"}
        };

        Object.keys(data).map(function (key) {
            const point = data[key];
            const date = new Date(point["created_at"]);
            avgBeaconRate["x"].push(date);
            avgBeaconRate["y"].push(point["rate"]);
        });

        result.push(avgBeaconRate);

        return result;
    }

    render() {
        const self = this;
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h3>Channel {this.props.channel.channel_number}</h3>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-3">
                        <dl>
                            <dt>Total Frames</dt>
                            <dd>{numeral(this.props.channel.total_frames).format("0,0")}</dd>
                            <dt>Beacon Rate</dt>
                            <dd><BeaconRate rate={this.props.channel.beacon_rate} /></dd>
                        </dl>
                    </div>

                    <div className="col-md-3">
                        <dl>
                            <dt>Signal Index</dt>
                            <dd>
                                <SignalIndex channel={this.props.channel} ssid={this.props.ssid} />
                            </dd>
                            <dt>Expected Signal Quality</dt>
                            <dt>
                                {numeral(this.props.channel.expected_delta.lower).format("0")}
                                &nbsp;<i className="fa fa-long-arrow-alt-right" />&nbsp;
                                {numeral(this.props.channel.expected_delta.upper).format("0")}
                            </dt>
                        </dl>
                    </div>

                    <div className="col-md-6">
                        <strong>Fingerprints</strong>
                        <ul className="channel-details-fingerprints">
                            {Object.keys(this.props.channel.fingerprints).map(function (key,i) {
                                return <li>{self.props.channel.fingerprints[key]}</li>
                            })}
                        </ul>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12 mt-md-1">
                        <SimpleLineChart
                            title="Beacon Rate"
                            width={1100}
                            height={200}
                            customMarginLeft={60}
                            customMarginRight={60}
                            finalData={this._formatBeaconRateHistory(this.props.channel.beacon_rate_history)}
                        />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12 mt-md-3">
                        <SimpleLineChart
                            title="Signal Quality and Expected Delta"
                            width={1100}
                            height={200}
                            customMarginLeft={60}
                            customMarginRight={60}
                            finalData={this._formatSignalQualityHistory(this.props.channel.signal_history)}
                        />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12 mt-md-1">
                        <SimpleLineChart
                            title="Signal Index"
                            width={1100}
                            height={200}
                            customMarginLeft={60}
                            customMarginRight={60}
                            finalData={this._formatSignalIndexHistory(this.props.channel.signal_history)}
                        />
                    </div>
                </div>

                <hr />
            </div>
        );
    }

}

export default ChannelDetails;



