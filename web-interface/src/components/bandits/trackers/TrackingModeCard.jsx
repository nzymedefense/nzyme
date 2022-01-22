import React from 'react'

class TrackingModeCard extends React.Component {
  render () {
    let mode = this.props.mode ? 'Tracking' : 'Idle'
    let color = this.props.mode ? 'bg-warning' : 'bg-primary'

    if (this.props.status === 'DARK') {
      mode = 'OFFLINE'
      color = 'bg-danger'
    }

    if (this.props.pendingRequests) {
      mode = 'Pending CMD Receipt'
      color = 'bg-warning'
    }

    return (
            <div className={'card text-white ' + color}>
                <div className="card-body text-center">
                    <h3 className="card-title">Tracking Mode</h3>
                    <p className="card-text text-center">
                        <h2>{mode}</h2>
                    </p>
                </div>
            </div>
    )
  }
}

export default TrackingModeCard
