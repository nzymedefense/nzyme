import React from 'react';
import Reflux from 'reflux';
import ChannelTable from "./ChannelTable";

class SSIDTableRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        const self = this;

        return (
            <table className="table table-sm table-hover table-striped">
                <thead>
                    <tr>
                        <th>SSID</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>{this.props.ssid.name}</td>
                    </tr>
                    <tr>
                    {Object.keys(this.props.ssid.channels).map(function (key,i) {
                        return <ChannelTable key={i} channelNumber={key} channel={self.props.ssid.channels[key]} />;
                    })}
                    </tr>
                </tbody>
            </table>
        )
    }

}

export default SSIDTableRow;