import React from 'react';
import Reflux from 'reflux';
import numeral from "numeral";

class ProbesTableRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    _decideStatus(isInLoop) {
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

    render() {
        const probe = this.props.probe;

        return (
            <tr>
                <td>{probe.name}</td>
                <td>{this._decideStatus(probe.is_in_loop)}</td>
                <td>{probe.class_name}</td>
                <td>{probe.network_interface}</td>
                <td>{probe.current_channel}</td>
                <td>{probe.channels.toString()}</td>
                <td>{numeral(probe.total_frames).format('0,0')}</td>
            </tr>
        )
    }

}

export default ProbesTableRow;