import React from "react";
import SignalStrength from "../util/SignalStrength";
import moment from "moment/moment";
import numeral from "numeral";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Channel from "../util/Channel";
import ApiRoutes from "../../../util/ApiRoutes";
import InfrastructureTypes from "../util/InfrastructureTypes";

function BSSIDDetailsRows(props) {

  const COLSPAN = 8;

  const bssid = props.bssid;
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
      <tr>
        <td colSpan={COLSPAN}>
          <table className="table table-sm table-hover table-striped mb-0">
            <thead>
            <tr>
              <th>SSID</th>
              <th>Mode</th>
              <th>Channel</th>
              <th>Usage</th>
              <th>Signal Strength</th>
              <th>Security</th>
              <th>WPS</th>
              <th>Last Seen</th>
            </tr>
            </thead>
            <tbody>
              {ssids.sort((a, b) => a.ssid.localeCompare(b.ssid)).sort((a, b) => b.is_main_active - a.is_main_active).map(function (ssid, i) {
                return (
                  <tr key={"ssid-" + i}>
                    <td>
                      <a href={ApiRoutes.DOT11.NETWORKS.SSID(bssid.bssid, ssid.ssid, ssid.channel)}>{ssid.ssid}</a>
                    </td>
                    <td><InfrastructureTypes types={ssid.infrastructure_types} /></td>
                    <td>
                      <Channel channel={ssid.channel} frequency={ssid.frequency} is_main_active={ssid.is_main_active} />
                    </td>
                    <td>{numeral(ssid.total_frames).format("0,0")} frames / {numeral(ssid.total_bytes).format("0,0b")}</td>
                    <td><SignalStrength strength={ssid.signal_strength_average} /></td>
                    <td>{ssid.security_protocols.length === 0 ? "None" : ssid.security_protocols.join(",")}</td>
                    <td>{ssid.is_wps.join(",")}</td>
                    <td title={moment(ssid.last_seen).format()}>
                      {moment(ssid.last_seen).fromNow()}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </td>
      </tr>
    </React.Fragment>
  )

}

export default BSSIDDetailsRows;