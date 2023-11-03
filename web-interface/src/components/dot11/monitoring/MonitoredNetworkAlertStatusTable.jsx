import React from "react";
import MonitoredNetworkSingleAlertStatus from "./MonitoredNetworkSingleAlertStatus";
import HelpBubble from "../../misc/HelpBubble";
import LoadingSpinner from "../../misc/LoadingSpinner";
import AlertEnabledTrigger from "./AlertEnabledTrigger";

function MonitoredNetworkAlertStatusTable(props) {

  const ssid = props.ssid;
  const renderControls = props.renderControls;
  const bumpRevision = props.bumpRevision;

  if (!ssid) {
    return <LoadingSpinner />
  }

  return (
      <table className="table table-sm table-hover table-striped mb-0">
        <thead>
        <tr>
          <th>Type</th>
          <th>Status</th>
          { renderControls ? <th>&nbsp;</th> : null }
        </tr>
        </thead>
        <tbody>
        <tr>
          <td>Expected BSSIDs / Access Points</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_bssid" /></td>
          { renderControls ? <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_bssid" bumpRevision={bumpRevision} /> </td> : null }
        </tr>
        <tr>
          <td>Expected Fingerprints</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_fingerprint" /></td>
          { renderControls ? <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_fingerprint" bumpRevision={bumpRevision} /> </td> : null }
        </tr>
        <tr>
          <td>Expected Channels</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_channel" /></td>
          { renderControls ? <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_channel" bumpRevision={bumpRevision} /> </td> : null }
        </tr>
        <tr>
          <td>Expected Security Suites</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_security_suites" /></td>
          { renderControls ? <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_security_suites" bumpRevision={bumpRevision} /> </td> : null }
        </tr>
        <tr>
          <td>Expected Signal Track <HelpBubble link="https://go.nzyme.org/wifi-network-monitoring-signal-tracks" /></td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_signal_tracks" />{' '}</td>
          { renderControls ? <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_signal_tracks" bumpRevision={bumpRevision} /> </td> : null }
        </tr>

        <tr>
          <td>Disconnection Anomalies <HelpBubble link="https://go.nzyme.org/wifi-network-monitoring-disco-anomalies" /></td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="disco_anomalies" skipEnabledCheck={true} />{' '}</td>
          { renderControls ? <td><span className="text-muted">n/a</span></td> : null }
        </tr>

        </tbody>
      </table>
  )

}

export default MonitoredNetworkAlertStatusTable;