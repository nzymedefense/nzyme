import React from "react";
import TimerRow from "../../../../misc/metrics/TimerRow";

function NodeTimers(props) {

  const timers = props.timers;

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Metric</th>
          <th>Max</th>
          <th>Min</th>
          <th>Mean</th>
          <th>P99</th>
          <th>StdDev</th>
          <th>Calls</th>
        </tr>
        </thead>
        <tbody>
          <TimerRow title="Password Hashing" timer={timers.password_hashing} />
          <TimerRow title="Mac Address Context Lookup" timer={timers.context_mac_lookup} />
        </tbody>
      </table>
  )

}

export default NodeTimers;