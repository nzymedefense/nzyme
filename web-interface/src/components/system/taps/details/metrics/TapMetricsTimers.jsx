import React from 'react'
import numeral from "numeral";

function TapMetricsTimers (props) {
  const timers = props.timers;

  if (!timers || Object.keys(timers).length === 0) {
    return <div className="alert alert-warning">
      No recent data.
    </div>
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Timer</th>
          <th>Mean</th>
          <th>P99</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {Object.keys(timers).sort((a, b) => a.localeCompare(b)).map(function (key, i) {
          return (
              <tr key={i}>
                <td>{timers[key].metric_name}</td>
                <td>{numeral(timers[key].mean).format("0,0.0")} &#956;s</td>
                <td>{numeral(timers[key].p99).format("0,0.0")} &#956;s</td>
                <td>
                  CHART
                </td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )
}

export default TapMetricsTimers
