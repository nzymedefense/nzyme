import React from 'react'

class SystemStatusWidget extends React.Component {
  render () {
    return (
            <div className={'card text-center overview-card ' + (this.props.systemHealthStatus !== 'GREEN' ? 'bg-danger' : 'bg-success') }>
                <div className="card-body">
                    <p>System Status</p>
                    <span>{this.props.systemHealthStatus}</span>
                </div>
            </div>
    )
  }
}

export default SystemStatusWidget
