import React from 'react';
import Reflux from 'reflux';

import numeral from "numeral";

class SSIDRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    static listFingerprints(fingerprints) {
        if (!fingerprints || fingerprints.length === 0) {
            return "n/a";
        }

        let abbv = "";

        let i = 0;
        fingerprints.forEach(function(f) {
            abbv += f ? f : "n/a";

            if (i !== fingerprints.length-1) {
                abbv += ", "
            }

            i++;
        });

        return abbv;
    }

    render() {
        const c = this.props.channel;

        return (
            <tr>
                <td>{/[^a-zA-Z0-9]/.test(this.props.ssid) ? "[hidden]" : this.props.ssid }</td>
                <td><strong>{this.props.channelNumber}</strong></td>
                <td>{numeral(c.total_frames).format('0,0')}</td>
                <td>{numeral(c.signal_quality_min).format('0')}</td>
                <td>{numeral(c.signal_quality_max).format('0')}</td>
                <td className={c.fingerprints.length > 1 ? "text-danger" : ""}>
                    {SSIDRow.listFingerprints(c.fingerprints)}
                </td>
            </tr>
        )
    }

}

export default SSIDRow;