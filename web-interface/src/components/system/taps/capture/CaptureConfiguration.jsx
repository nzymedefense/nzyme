import React from 'react'
import CaptureRow from './CaptureRow'

function CaptureConfiguration (props) {
  if (!props.tap.active) {
    return <div className="alert alert-warning mb-0">No recent data.</div>
  }

  if (props.tap.captures.length === 0) {
    return <div className="alert alert-info mb-0">No captures are active on this tap.</div>
  }

  return (
        <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
                <th>Interface</th>
                <th>Type</th>
                <th>Received Frames / Packets / Blocks</th>
                <th>Dropped (Interface)</th>
                <th>Dropped (Buffer)</th>
            </tr>
            </thead>
            <tbody>
            {Object.keys(props.tap.captures.sort((a, b) => a.interface_name.localeCompare(b.interface_name))).map(function (key, i) {
              return <CaptureRow key={'capture-' + i} capture={props.tap.captures[i]} tap={props.tap} />
            })}
            </tbody>
        </table>
  )
}

export default CaptureConfiguration
