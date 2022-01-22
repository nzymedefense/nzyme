import React from 'react'
import LoadingSpinner from '../misc/LoadingSpinner'
import TrapTableRow from './TrapTableRow'

class TrapsTable extends React.Component {
  render () {
    const self = this

    if (!this.props.traps) {
      return <LoadingSpinner/>
    } else {
      if (this.props.traps.length === 0) {
        return <div className="alert alert-info">No traps are currently configured.</div>
      }

      return (
                <table className="table table-sm table-hover table-striped">
                    <thead>
                    <tr>
                        <th>Type</th>
                        <th>Probe</th>
                        <th>Description</th>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.keys(this.props.traps).map(function (key, i) {
                      return <TrapTableRow key={i} trap={self.props.traps[key]} />
                    })}
                    </tbody>
                </table>
      )
    }
  }
}

export default TrapsTable
