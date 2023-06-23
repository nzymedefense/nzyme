import React from "react";
import SignalStrength from "../util/SignalStrength";
import moment from "moment/moment";
import LoadingSpinner from "../../misc/LoadingSpinner";

function BSSIDDetailsRows(props) {

  const COLSPAN = 6;

  const ssids = props.ssids;
  const loading = props.loading;

  if (loading) {
    return (
        <tr>
          <td colSpan={COLSPAN}>
            <LoadingSpinner />
          </td>
        </tr>
    )
  }

  if (ssids === null) {
    return null;
  }

  if (ssids.length === 0) {
    return (
        <tr>
          <td colSpan={COLSPAN}>
            Only hidden SSIDs.
          </td>
        </tr>
    )
  }

  return (
    <React.Fragment>
      {ssids.sort((a, b) => a.ssid.localeCompare(b.ssid)).map(function (ssid, i) {
        return (
            <tr key={"ssid-" + i}>
              <td colSpan={COLSPAN}>
                <table className="table table-sm table-hover table-striped mb-0">
                  <thead>
                  <tr>
                    <th>SSID</th>
                    <th>Signal Strength</th>
                    <th>Security</th>
                    <th>WPS</th>
                    <th>Last Seen</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr>
                    <td>{ssid.ssid}</td>
                    <td><SignalStrength strength={ssid.signal_strength_average} /></td>
                    <td>{ssid.security_protocols.length === 0 ? "None" : ssid.security_protocols.join(",")}</td>
                    <td>{ssid.is_wps.join(",")}</td>
                    <td title={moment(ssid.last_seen).format()}>
                      {moment(ssid.last_seen).fromNow()}
                    </td>
                  </tr>
                  </tbody>
                </table>
              </td>
            </tr>
        )
      })}
    </React.Fragment>
  )

}

export default BSSIDDetailsRows;