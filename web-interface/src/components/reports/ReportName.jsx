import React from 'react'

class ReportName extends React.Component {
  render () {
    const name = this.props.name

    if (name.includes('-')) {
      return name.split('-')[0]
    } else {
      return name
    }
  }
}

export default ReportName
