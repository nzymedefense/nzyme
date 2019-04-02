import React from 'react';
import Reflux from 'reflux';

class SystemStatusState extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <li className={this.props.state.active ? "text-success" : "state-not-active"}>
                {this.props.state.name}
            </li>
        )
    }

}

export default SystemStatusState;