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
          <TimerRow title="Password Hashing" timer={timers.password_hashing}/>
          <TimerRow title="802.11 Monitor: BSSIDs" timer={timers.dot11_network_monitor_bssid}/>
          <TimerRow title="802.11 Monitor: Channel" timer={timers.dot11_network_monitor_channel}/>
          <TimerRow title="802.11 Monitor: Security Suites" timer={timers.dot11_network_monitor_security_suites}/>
          <TimerRow title="802.11 Monitor: Fingerprint" timer={timers.dot11_network_monitor_fingerprint}/>
          <TimerRow title="802.11 Monitor: Signal Tracks" timer={timers.dot11_network_monitor_signal_tracks}/>
        </tbody>
      </table>
  )

}

export default NodeTimers;