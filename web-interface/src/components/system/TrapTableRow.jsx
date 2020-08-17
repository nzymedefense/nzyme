import React from 'react';
import Reflux from 'reflux';

class TrapTableRow extends Reflux.Component {

    render() {
        const trap = this.props.trap;

        return (
            <tr>
                <td>{trap.type}</td>
                <td>{trap.probe.name}</td>
                <td>{trap.description}</td>
            </tr>
        )
    }

}

export default TrapTableRow;