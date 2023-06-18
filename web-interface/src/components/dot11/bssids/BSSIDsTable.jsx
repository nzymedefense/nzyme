import React from "react";
import BSSIDRow from "./BSSIDRow";

function BSSIDsTable(props) {

  const bssids = props.bssids;

  return (
      <table className="table table-sm table-hover table-striped mb-0">
        <thead>
        <tr>
          <th>BSSID</th>
          <th>Signal Strength</th>
          <th>Advertised SSIDs</th>
          <th>Last Seen</th>
        </tr>
        </thead>
        <tbody>
        {Object.keys(bssids.sort((a, b) => b.signal_strength_average - a.signal_strength_average))
            .map(function (key, i) {
          return <BSSIDRow key={'bssid-' + i} bssid={bssids[key]} />
        })}
        </tbody>
      </table>
  )

}

export default BSSIDsTable;