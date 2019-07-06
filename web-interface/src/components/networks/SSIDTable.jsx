import React from 'react';
import Reflux from 'reflux';
import SSIDRow from "./SSIDRow";

class SSIDTableRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        const self = this;

        return (
            <tr style={{"display": this.props.display ? "" : "none"}} >
                <td colSpan="7">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                            <tr>
                                <th>SSID</th>
                                <th>Channel</th>
                                <th>Frames</th>
                                <th>Security</th>
                                <th>Signal Strength Index</th>
                                <th>Fingerprints</th>
                            </tr>
                        </thead>
                        <tbody>
                            {Object.keys(this.props.ssid.channels).map(function (key,i) {
                                return <SSIDRow key={i} ssid={self.props.ssid} channelNumber={key} channel={self.props.ssid.channels[key]} />;
                            })}
                        </tbody>
                    </table>
                </td>
            </tr>
        )
    }

}

export default SSIDTableRow;