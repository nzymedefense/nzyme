import React from 'react';
import Reflux from 'reflux';

import SystemStatus from "./SystemStatus";
import Metrics from "./Metrics";
import Probes from "./Probes";
import AlertConfiguration from "./AlertConfiguration";

class SystemPage extends Reflux.Component {

    constructor(props) {
        super(props);
    }
    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h2>System Overview</h2>

                        <SystemStatus />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <AlertConfiguration />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <Probes />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <Metrics />
                    </div>
                </div>
            </div>
        )
    }

}

export default SystemPage;