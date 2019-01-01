import React from 'react';
import Reflux from 'reflux';

import Routes from "../../util/Routes";
import SystemStatus from "./SystemStatus";

class SystemPage extends Reflux.Component {

    constructor(props) {
        super(props);
    }
    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <span className="float-right">
                            <a href={Routes.DASHBOARD} className="btn btn-dark">Back to Dashboard</a>
                        </span>

                        <h2>System Overview</h2>

                        <SystemStatus />
                    </div>
                </div>
            </div>
        )
    }

}

export default SystemPage;