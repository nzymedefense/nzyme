import React from 'react'
import TimerRow from '../../misc/metrics/TimerRow'
import LoadingSpinner from '../../misc/LoadingSpinner'

function PGPMetrics (props) {
  if (!props.metrics) {
    return <LoadingSpinner />
  }

  return (
        <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
                <th>Metric</th>
                <th>Maximum</th>
                <th>Minimum</th>
                <th>Mean</th>
                <th>99th Percentile</th>
                <th>Standard Deviation</th>
                <th>Calls</th>
            </tr>
            </thead>
            <tbody>
            <TimerRow title="Encryption Operations" timer={props.metrics.pgp_encryption_timer}/>
            <TimerRow title="Decryption Operations" timer={props.metrics.pgp_decryption_timer}/>
            </tbody>
        </table>
  )
}

export default PGPMetrics
