import React, {useState} from 'react'
import LoadingSpinner from '../../misc/LoadingSpinner'
import TapRow from './TapRow'

function TapsTable(props) {

  const taps = props.taps

  const [showOfflineTaps, setShowOfflineTaps] = useState(false)

  const updateOfflineTapsSelection = function(e) {
    setShowOfflineTaps(e.target.checked)
  }

  if (!taps) {
    return <LoadingSpinner />
  }

  if (taps.length === 0) {
    return <div className="alert alert-warning">No nzyme taps have reported in. Install a tap and point it to the nzyme leader.</div>
  }

  return (
    <div className="row">
      <div className="col-md-12">
        <h3 style={{display: "inline-block"}}>All Taps</h3>

        <div className="form-check form-switch float-end">
          <input className="form-check-input" type="checkbox" role="switch"
                 id="showOfflineNodes"onChange={updateOfflineTapsSelection} />
          <label className="form-check-label" htmlFor="showOfflineNodes">
            Show recently active but offline taps
          </label>
        </div>

        <table className="table table-sm table-hover table-striped">
          <thead>
            <tr>
              <th>Name</th>
              <th>Throughput</th>
              <th>Total data processed</th>
              <th>CPU Load</th>
              <th>Memory Used</th>
              <th>Clock</th>
              <th>Last Seen</th>
            </tr>
          </thead>
          <tbody>
            {Object.keys(taps.sort((a, b) => a.name.localeCompare(b.name))).map(function (key, i) {
              return <TapRow key={'tap-' + i} tap={taps[i]} showOfflineTaps={showOfflineTaps} />
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default TapsTable
