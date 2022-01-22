import React from 'react'
import NetworksList from './NetworksList'

class NetworksPage extends React.Component {
  render () {
    return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Networks</h1>
                    </div>
                </div>

                <NetworksList />
            </div>
    )
  }
}

export default NetworksPage
