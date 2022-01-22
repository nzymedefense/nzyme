import React from 'react'

import SystemStatus from './SystemStatus'
import Metrics from './metrics/Metrics'
import Probes from './Probes'
import AlertConfiguration from './AlertConfiguration'
import Versioncheck from './Versioncheck'

class SystemPage extends React.Component {
  render () {
    return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>System Status</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6">
                        <AlertConfiguration />
                    </div>

                    <div className="col-md-6">
                        <SystemStatus />
                    </div>
                </div>

                <div className="row mt-3">
                    <div className="col-md-12">
                        <Versioncheck />
                    </div>
                </div>

                <div className="row mt-3">
                    <div className="col-md-12">
                        <Probes />
                    </div>
                </div>

                <div className="row mt-3">
                    <div className="col-md-12">
                        <Metrics />
                    </div>
                </div>
            </div>
    )
  }
}

export default SystemPage
