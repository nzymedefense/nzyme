import React from 'react';
import Reflux from 'reflux';

import numeral from "numeral";
import SimpleLineChart from "../../charts/SimpleLineChart";
import HeatmapWaterfallChart from "../../charts/HeatmapWaterfallChart";
import HelpBubble from "../../misc/HelpBubble";
import TimerangeSwitcher from "./TimerangeSwitcher";

class ChannelDetails extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            channel: props.channel,
            historyHours: props.historyHours
        };
    }

    _formatSignalIndexDistribution(data) {
        const result = [];

        const distribution = {
            x: [],
            y: [],
            type: "bar",
            name: "Signal Index",
            line: {width: 1, shape: "linear", color: "#2983fe"}
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

        return result;
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

    componentWillReceiveProps(newProps) {
        this.setState({
            channel: newProps.channel,
            historyHours: newProps.historyHours
        });
    }

    render() {
        if (!this.state.channel) {
            return (
                <div>
                    <div className="row">
                        <div className="col-md-12">
                            <div className="alert alert-danger" role="alert">
                                Requested channel not found.
                            </div>
                        </div>
                    </div>
                </div>
            )
        }

        const self = this;
        return (
            <div>
                <div className="row">
                    <div className="col-md-3">
                        <dl>
                            <dt>Total Frames</dt>
                            <dd>{numeral(this.state.channel.total_frames).format("0,0")}</dd>
                        </dl>
                    </div>

                    <div className="col-md-6">
                        <h6>
                            Channel Fingerprints <small><HelpBubble link="https://go.nzyme.org/fingerprinting" /></small>
                        </h6>
                        <ul className="channel-details-fingerprints">
                            {Object.keys(this.state.channel.fingerprints).map(function (key,i) {
                                return <li key={"channel-fp-" + self.state.channel.fingerprints[key]}>{self.state.channel.fingerprints[key]}</li>
                            })}
                        </ul>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <SimpleLineChart
                            title={"Signal Strength Distribution (last " + self.state.channel.signal_index_distribution_minutes + " minutes)"}
                            width={1100}
                            height={200}
                            xaxistitle="Signal Strength (dBm)"
                            yaxistitle="Signal Count"
                            customMarginLeft={60}
                            customMarginRight={60}
                            finalData={this._formatSignalIndexDistribution(self.state.channel.signal_index_distribution)}
                        />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <HeatmapWaterfallChart
                            title={"Signal Strength Waterfall (last " + self.state.historyHours + " hours)"}
                            xaxistitle="Signal Strength (dBm)"
                            yaxistitle="Time"
                            hovertemplate="Signal Strength: %{x} dBm, %{z} frames at %{y}<extra></extra>"
                            data={this._formatSignalIndexHeatmap(self.state.channel.signal_index_history)}
                        />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-4">
                        <TimerangeSwitcher
                            ranges={[1,2,4,8,12,24]}
                            currentRange={self.state.historyHours}
                            _changeRange={self.props._changeRange}
                            title={"Waterfall Time Range"}
                        />
                    </div>
                </div>

                <hr />
            </div>
        );
    }

}

export default ChannelDetails;



