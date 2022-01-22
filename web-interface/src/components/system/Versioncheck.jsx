import React from 'react'

import LoadingSpinner from '../misc/LoadingSpinner'
import VersionInfo from './VersionInfo'
import SystemService from '../../services/SystemService'

class Versioncheck extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      versionInfo: undefined
    }

    this.systemService = new SystemService()
    this.systemService.getVersionInfo = this.systemService.getVersionInfo.bind(this)
  }

  componentDidMount () {
    this.systemService.getVersionInfo()
  }

  render () {
    if (!this.state.versionInfo) {
      return <LoadingSpinner/>
    } else {
      return <VersionInfo version={this.state.versionInfo} />
    }
  }
}

export default Versioncheck
