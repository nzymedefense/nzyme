import React from 'react';

import numeral from "numeral";
import SSID from "./SSID";

class SSIDRow extends React.Component {

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