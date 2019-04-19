import React from 'react';
import Reflux from 'reflux';
import numeral from "numeral";

class ProbesTableRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    static _decideStatus(isInLoop) {
        if (isInLoop) {
            return (
                <i className="fas fa-check-square text-success"></i>
            )
        } else {
            return (
                <i className="fas fa-exclamation-triangle text-danger" title="NOT RUNNING! Check nzyme logs."></i>
            )
        }
    }

    // Limit to 13 total channels and abbreviate if it's more.
    static _printChannels(channels) {
        if (channels.length > 13) {
            return channels.slice(0,13).toString() + " ..."
        } else {
            return channels.toString();
        }
    }

    render() {
        const probe = this.props.probe;

        return (
            <tr>
                <td>{probe.name}</td>
                <td>{ProbesTableRow._decideStatus(probe.is_in_loop)}</td>
                <td>{probe.class_name}</td>
                <td>{probe.network_interface}</td>
                <td title={probe.channels.toString()}>{ProbesTableRow._printChannels(probe.channels)}</td>
                <td>{numeral(probe.total_frames).format('0,0')}</td>
            </tr>
        )
    }

}

export default ProbesTableRow;