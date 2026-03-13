import React from "react";

import numeral from "numeral";

export default function TapDot11CoverageMap(props) {

  const tap = props.tap;

  if (!tap.active) {
    return <div className="alert alert-warning">No recent data.</div>
  }

  if (!tap.dot11_frequencies || tap.dot11_frequencies.length === 0) {
    return <div className="alert alert-info mb-0">This tap has no 802.11/WiFi captures collecting data.</div>
  }

  const frequencies = {};
  tap.dot11_frequencies.forEach((freq) => { frequencies[freq.frequency] = freq.channel_widths; });

  const wifiCaptures = tap.captures.filter((c) => c.capture_type === "WiFi");

  return (
      <React.Fragment>
        <h4>Capture Cycle Times</h4>

        <p className="text-muted">
          A WiFi capture should ideally scan its entire assigned spectrum within 60 seconds. Longer durations can
          complicate traffic analysis, as the default chart bucket size is one minute and may not encompass
          complete <a href="https://go.nzyme.org/wifi-hopping" target="_blank">channel hop cycles</a> Consider
          re-assigning channels to other captures if the current capture process is too slow.
        </p>

        <table className="table table-sm mb-3">
          <thead>
          <tr>
            <th>Capture</th>
            <th>Cycle Time</th>
            <th>Status</th>
          </tr>
          </thead>
          <tbody>
          {Object.keys(wifiCaptures.sort((a, b) => a.interface_name.localeCompare(b.interface_name))).map((key, i) => {
            return (
                <tr key={i}>
                  <td>{wifiCaptures[key].interface_name}</td>
                  <td>{numeral(wifiCaptures[key].cycle_time/1000).format("0,0")} seconds</td>
                  <td>{wifiCaptures[key].cycle_time < 60000 ? <span className="badge bg-success">Good</span> :
                      <span className="badge bg-warning">Slow</span>}</td>
                </tr>
            )
          })}
          </tbody>
        </table>
      </React.Fragment>
  )

}