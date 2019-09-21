import React from 'react';
import Reflux from 'reflux';

import numeral from "numeral";
import SimpleLineChart from "../../charts/SimpleLineChart";
import HeatmapWaterfallChart from "../../charts/HeatmapWaterfallChart";

class ChannelDetails extends Reflux.Component {

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

    render() {
        if (!this.props.channel) {
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
                            <dd>{numeral(this.props.channel.total_frames).format("0,0")}</dd>
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

                    <div className="row">
                        <div className="col-md-12">
                            <SimpleLineChart
                                title="Signal Strength Distribution"
                                width={1100}
                                height={200}
                                xaxistitle="Signal Strength (dBm)"
                                yaxistitle="Signal Count"
                                customMarginLeft={60}
                                customMarginRight={60}
                                finalData={this._formatSignalIndexDistribution(self.props.channel.signal_index_distribution)}
                            />
                        </div>
                    </div>

                    <div className="row">
                        <div className="col-md-12">
                            <HeatmapWaterfallChart
                                title="Signal Strength Waterfall"
                                xaxistitle="Signal Strength (dBm)"
                                yaxistitle="Sample Minute"
                                hovertemplate="Signal Strength: %{x} dBm, %{z} frames at %{y}<extra></extra>"
                                data={this._formatSignalIndexHeatmap(self.props.channel.signal_index_history)}
                            />
                        </div>
                    </div>
                </div>

                <hr />
            </div>
        );
    }

}

export default ChannelDetails;



