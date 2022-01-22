import React from 'react'

import ProbesTable from './ProbesTable'
import TrapsTable from './TrapsTable'
import ProbesService from '../../services/ProbesService'

class Probes extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      probes: undefined,
      traps: undefined
    }

    this.probesService = new ProbesService()
    this.probesService.findAll = this.probesService.findAll.bind(this)
    this.probesService.findAllTraps = this.probesService.findAllTraps.bind(this)

    this._loadData = this._loadData.bind(this)
  }

  componentDidMount () {
    const self = this

    setInterval(function () {
      self._loadData()
    }, 1000)
  }

  _loadData () {
    this.probesService.findAll()
    this.probesService.findAllTraps()
  }

  render () {
    return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h3>Probes</h3>

                        <ProbesTable probes={this.state.probes} />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h3>Traps</h3>

                        <TrapsTable traps={this.state.traps} />
                    </div>
                </div>
            </div>
    )
  }
}

export default Probes
