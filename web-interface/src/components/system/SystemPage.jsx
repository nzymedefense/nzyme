import React from 'react';
import Reflux from 'reflux';

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
                        <h2>System Overview</h2>

                        <SystemStatus />
                    </div>
                </div>
            </div>
        )
    }

}

export default SystemPage;