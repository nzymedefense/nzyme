import React from 'react';
import numeral from "numeral";
import SignalIndexThreshold from "./SignalIndexThreshold";

class SignalIndex extends React.Component {

    render() {
        if (!this.props.ssid.human_readable) {
            return (
                <span>-</span>
            )
        }

        return (
            <span>
                {numeral(this.props.channel.signal_index).format("0.00")}&nbsp;
                (<SignalIndexThreshold threshold={this.props.channel.signal_index_threshold} status={this.props.channel.signal_index_status} ssid={this.props.ssid} />)
            </span>
        )
    }

}

export default SignalIndex;