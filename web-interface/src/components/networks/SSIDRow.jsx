import React from 'react';
import Reflux from 'reflux';

import numeral from "numeral";
import SSID from "./SSID";

class SSIDRow extends Reflux.Component {

    _listFingerprints() {
        const fingerprints = this.props.channel.fingerprints;

        if (!fingerprints || fingerprints.length === 0) {
            return "n/a";
        }

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

    _printSecurity() {
        let x = "";

        const total = this.props.ssid.security.length;
        this.props.ssid.security.forEach(function(mode, ix) {
            x += mode.as_string;

            if(ix < total-1) {
                x += ", ";
            }
        });

        if (!x) {
            return (
                <span className="text-warning">None</span>
            )
        }

        return x;
    }

    render() {
        const c = this.props.channel;

        return (
            <tr>
                <td><SSID ssid={this.props.ssid} channel={c.channel_number}/></td>
                <td><strong>{c.channel_number}</strong></td>
                <td title="Frames per Second"><span className={this.props.isMostActiveChannel ? "most-active-channel" : undefined}>
                    {numeral(c.total_frames_recent/60).format('0.00')}
                </span></td>
                <td>{this._printSecurity()}</td>
            </tr>
        )
    }

}

export default SSIDRow;