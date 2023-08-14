import React from "react";
import MonitoredNetworkSingleAlertStatus from "./MonitoredNetworkSingleAlertStatus";
import HelpBubble from "../../misc/HelpBubble";
import LoadingSpinner from "../../misc/LoadingSpinner";

function MonitoredNetworkAlertStatusTable(props) {

  const ssid = props.ssid;

  if (!ssid) {
    return <LoadingSpinner />
  }

  return (
      <table className="table table-sm table-hover table-striped mb-0">
        <thead>
        <tr>
          <th>Type</th>
          <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <tr>
          <td>Expected BSSIDs / Access Points</td>
          <td><MonitoredNetworkSingleAlertStatus ssid_is_enabled={ssid.is_enabled} status={ssid.status_unexpected_bssid} /></td>
        </tr>
        <tr>
          <td>Expected Fingerprints</td>
          <td><MonitoredNetworkSingleAlertStatus ssid_is_enabled={ssid.is_enabled} status={ssid.status_unexpected_fingerprint} /></td>
        </tr>
        <tr>
          <td>Expected Channels</td>
          <td><MonitoredNetworkSingleAlertStatus ssid_is_enabled={ssid.is_enabled} status={ssid.status_unexpected_channel} /></td>
        </tr>
        <tr>
          <td>Expected Security Suites</td>
          <td><MonitoredNetworkSingleAlertStatus ssid_is_enabled={ssid.is_enabled} status={ssid.status_unexpected_security} /></td>
        </tr>
        <tr>
          <td>Expected Signal Track <HelpBubble link="https://go.nzyme.org/wifi-network-monitoring-signal-tracks" /></td>
          <td><MonitoredNetworkSingleAlertStatus ssid_is_enabled={ssid.is_enabled} status={ssid.status_unexpected_signal_tracks} />{' '}</td>
        </tr>
        </tbody>
      </table>
  )

}

export default MonitoredNetworkAlertStatusTable;