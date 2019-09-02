import React from 'react';
import Reflux from 'reflux';

import numeral from "numeral";
import SimpleLineChart from "../../charts/SimpleLineChart";

class ChannelDetails extends Reflux.Component {

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
                </div>

                <hr />
            </div>
        );
    }

}

export default ChannelDetails;



