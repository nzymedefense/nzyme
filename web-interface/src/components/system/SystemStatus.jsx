import React from 'react'

import LoadingSpinner from '../misc/LoadingSpinner'

import SystemStatusState from './SystemStatusState'
import SystemService from '../../services/SystemService'

class SystemStatus extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      systemStatus: undefined
    }

    this.systemService = new SystemService()
    this.systemService.getStatus = this.systemService.getStatus.bind(this)
  }

  componentDidMount () {
    const self = this

    this.systemService.getStatus()
    setInterval(function () {
      self.systemService.getStatus()
    }, 5000)
  }

  render () {
    const self = this

    if (!this.state.systemStatus) {
      return <LoadingSpinner/>
    } else {
      return (
                <div>
                    <h3>Status <small><a href="https://go.nzyme.org/system-status-explained" target="_blank" rel="noopener noreferrer">help</a></small></h3>
                    <ul>
                        {Object.keys(this.state.systemStatus).map(function (key) {
                          return <SystemStatusState state={self.state.systemStatus[key]}/>
                        })}
                    </ul>
                </div>
      )
    }
  }
}

export default SystemStatus
