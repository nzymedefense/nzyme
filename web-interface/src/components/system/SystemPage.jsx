import React from 'react'

import Metrics from './metrics/Metrics'
import Probes from './Probes'
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
                    <div className="col-md-12">
                        <div className="card">
                            <div className="card-body">
                                <Versioncheck />
                            </div>
                        </div>
                    </div>
                </div>
                
                <div className="row mt-3">
                    <div className="col-md-12">
                        <div className="card">
                            <div className="card-body">
                                <Metrics />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
    )
  }
}

export default SystemPage
