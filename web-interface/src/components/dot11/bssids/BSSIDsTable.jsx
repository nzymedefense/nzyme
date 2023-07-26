import React from "react";
import BSSIDRow from "./BSSIDRow";

function BSSIDsTable(props) {

  const bssids = props.bssids;
  const minutes = props.minutes;
  const isAutoRefresh = props.isAutoRefresh;

  if (bssids.length === 0) {
    return <div className="alert alert-info mb-0">
      No access points discovered yet. Make sure a WiFi-enabled nzyme tap is connected and configured properly.
    </div>
  }

  return (
      <table className="table table-sm table-hover table-striped mb-0">
        <thead>
        <tr>
          <th style={{width: 145}}>BSSID</th>
          <th>Signal Strength</th>
          <th>Mode</th>
          <th>Advertised SSIDs</th>
          <th>Clients</th>
          <th>Security</th>
          <th>OUI</th>
          <th>Last Seen</th>
        </tr>
        </thead>
        <tbody>
        {Object.keys(bssids.sort((a, b) => b.signal_strength_average - a.signal_strength_average))
            .map(function (key, i) {
          return <BSSIDRow key={'bssid-' + i} bssid={bssids[key]} minutes={minutes} isAutoRefresh={isAutoRefresh} />
        })}
        </tbody>
      </table>
  )

}

export default BSSIDsTable;