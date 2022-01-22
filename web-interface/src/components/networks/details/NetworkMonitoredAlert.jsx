import React from 'react'

class NetworkMonitoredAlert extends React.Component {
  render () {
    if (this.props.ssid.is_monitored) {
      return (
                <div className="alert alert-primary">
                    <i className="fas fa-heartbeat" />&nbsp;
                    Nzyme has been configured to monitor this network and will raise alerts for any deviation from it's
                    configured expected state.
                </div>
      )
    } else {
      return null
    }
  }
}

export default NetworkMonitoredAlert
