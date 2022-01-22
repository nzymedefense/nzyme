import React from 'react'

class ReportExecutionLogStatusBadge extends React.Component {
  render () {
    switch (this.props.status) {
      case 'SUCCESS':
        return <span className="badge badge-success">Success</span>
      case 'ERROR':
        return <span className="badge badge-danger">Error</span>
      default:
        return <span className="badge badge-warning">Unknown</span>
    }
  }
}

export default ReportExecutionLogStatusBadge
