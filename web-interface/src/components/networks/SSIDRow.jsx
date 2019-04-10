import React from 'react';
import Reflux from 'reflux';

import numeral from "numeral";

class SSIDRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        const c = this.props.channel;

        console.log(this.props.ssid);

        return (
            <tr>
                <td>{/[^a-zA-Z0-9]/.test(this.props.ssid) ? "[hidden]" : this.props.ssid }</td>
                <td><strong>{this.props.channelNumber}</strong></td>
                <td>{numeral(c.total_frames).format('0,0')}</td>
                <td>{numeral(c.signal_quality_min).format('0')}</td>
                <td>{numeral(c.signal_quality_max).format('0')}</td>
                <td>{numeral(c.signal_quality_avg_recent).format('0')}</td>
                <td>{numeral(c.out_of_delta_avg_recent_percent).format('0.00')}</td>
            </tr>
        )
    }

}

export default SSIDRow;