import React from 'react';
import Reflux from 'reflux';

import LoadingSpinner from "../misc/LoadingSpinner";

import SystemActions from "../../actions/SystemActions";
import SystemStore from "../../stores/SystemStore";
import SystemStatusState from "./SystemStatusState";

class SystemStatus extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = SystemStore;

        this.state = {
            systemStatus: undefined
        };
    }

    componentDidMount() {
        SystemActions.getStatus();
        setInterval(function () {
            SystemActions.getStatus();
        }, 5000);
    }

    render() {
        const self = this;

        if (!this.state.systemStatus) {
            return <LoadingSpinner/>;
        } else {
            return (
                <div>
                    <h3>Status <small><a href="https://go.nzyme.org/system-status-explained" target="_blank" rel="noopener noreferrer">help</a></small></h3>
                    <ul>
                        {Object.keys(this.state.systemStatus).map(function (key) {
                            return <SystemStatusState state={self.state.systemStatus[key]}/>
                        })}
                    </ul>
                </div>
            )
        }
    }
}

export default SystemStatus;