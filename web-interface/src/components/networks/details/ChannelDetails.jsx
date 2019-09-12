import React from 'react';
import Reflux from 'reflux';

import numeral from "numeral";
import SimpleLineChart from "../../charts/SimpleLineChart";

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

        // We want a static scale from -10.0 to +10.0.
        distribution["x"].push(-10);
        distribution["y"].push(0);
        distribution["x"].push(10);
        distribution["y"].push(0);

        Object.keys(data).forEach(function(point){
            distribution["x"].push(point);
            distribution["y"].push(data[point]);
        });

        result.push(distribution);

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
                                title="Signal Index Distribution"
                                width={1100}
                                height={200}
                                customMarginLeft={60}
                                customMarginRight={60}
                                finalData={this._formatSignalIndexDistribution(self.props.channel.signal_index_distribution)}
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



