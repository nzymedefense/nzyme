import React from 'react';
import Reflux from 'reflux';

class SSIDRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        const c = this.props.channel;

        return (
            <tr>
                <td>{this.props.ssid}</td>
                <td><strong>{this.props.channelNumber}</strong></td>
                <td>{c.total_frames}</td>
                <th>{c.signal_quality_min}</th>
                <th>{c.signal_quality_max}</th>
                <th>{c.signal_quality_avg_recent}</th>
                <th>{c.out_of_delta_avg_recent_percent}</th>
            </tr>
        )
    }

}

export default SSIDRow;