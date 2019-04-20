import React from 'react';
import Reflux from 'reflux';

import numeral from "numeral";

class SSIDRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    static _abbreviateFingerprints(fingerprints) {
        let abbv = "";

        let i = 0;
        fingerprints.forEach(function(f) {
            abbv += f;

            if (i !== fingerprints.length-1) {
                abbv += ", "
            }

            i++;
        });

        return abbv;
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
                <td className={c.fingerprints.length !== 1 ? "text-danger" : ""}>
                    {SSIDRow._abbreviateFingerprints(c.fingerprints)}
                </td>
            </tr>
        )
    }

}

export default SSIDRow;