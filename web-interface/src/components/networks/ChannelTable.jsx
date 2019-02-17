import React from 'react';
import Reflux from 'reflux';

class ChannelTable extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        const c = this.props.channel;

        return (
            <table className="table table-sm table-hover table-striped">
                <thead>
                <tr>
                    <th>Channel</th>
                    <th>Total Frames</th>
                    <th>Quality (min)</th>
                    <th>Quality (max)</th>
                    <th>Recent Quality Average</th>
                    <th>Recent Quality Stddev</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>{this.props.channelNumber}</td>
                    <td>{c.total_frames}</td>
                    <th>{c.signal_quality_min}</th>
                    <th>{c.signal_quality_max}</th>
                    <th>{c.signal_quality_avg_recent}</th>
                    <th>{c.out_of_delta_avg_recent_percent}</th>
                </tr>
                </tbody>
            </table>
        )
    }

}

export default ChannelTable;