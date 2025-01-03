import React from "react";
import BSSIDRow from "./BSSIDRow";
import Paginator from "../../misc/Paginator";
import numeral from "numeral";

function BSSIDsTable(props) {

  const bssids = props.bssids;
  const timeRange = props.timeRange;
  const setFilters = props.setFilters;

  const page = props.page;
  const setPage = props.setPage;
  const perPage = props.perPage;

  if (bssids.length === 0) {
    return <div className="alert alert-info mb-0">
      No access points discovered yet. Make sure a WiFi-enabled nzyme tap is connected and configured properly.
    </div>
  }

  return (
    <React.Fragment>
      <div className="mb-1">
        Total: {numeral(bssids.total).format("0,0")}
      </div>

      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th style={{width: 175}}>BSSID</th>
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
        {Object.keys(bssids.bssids.sort((a, b) => b.signal_strength_average - a.signal_strength_average))
          .map(function (key, i) {
            return <BSSIDRow key={'bssid-' + i} bssid={bssids.bssids[key]} timeRange={timeRange} setFilters={setFilters}/>
          })}
        </tbody>
      </table>

      <Paginator itemCount={bssids.total} perPage={perPage} setPage={setPage} page={page}/>
    </React.Fragment>
  )

}

export default BSSIDsTable;