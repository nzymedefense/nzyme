import React from "react";
import MonitoredNetworkSingleAlertStatus from "./MonitoredNetworkSingleAlertStatus";
import HelpBubble from "../../misc/HelpBubble";
import LoadingSpinner from "../../misc/LoadingSpinner";
import AlertEnabledTrigger from "./AlertEnabledTrigger";
import ApiRoutes from "../../../util/ApiRoutes";

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
          {renderControls ? <React.Fragment><th>&nbsp;</th><th>&nbsp;</th></React.Fragment> : null}
        </tr>
        </thead>
        <tbody>
        <tr>
          <td>Expected BSSIDs / Access Points</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_bssid"/></td>
          {renderControls ?
              <React.Fragment>
                <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_bssid" bumpRevision={bumpRevision}/></td>
                <td><span className="text-muted">n/a</span></td>
              </React.Fragment> : null}
        </tr>
        <tr>
        <td>Expected Fingerprints</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_fingerprint"/></td>
          {renderControls ?
              <React.Fragment>
                <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_fingerprint" bumpRevision={bumpRevision}/></td>
                <td><span className="text-muted">n/a</span></td>
              </React.Fragment> : null}
        </tr>
        <tr>
        <td>Expected Channels</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_channel"/></td>
          {renderControls ?
              <React.Fragment>
                <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_channel" bumpRevision={bumpRevision}/></td>
                <td><span className="text-muted">n/a</span></td>
              </React.Fragment> : null}
        </tr>
        <tr>
        <td>Expected Security Suites</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_security_suites"/></td>
          {renderControls ?
              <React.Fragment>
                <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_security_suites" bumpRevision={bumpRevision}/></td>
                <td><span className="text-muted">n/a</span></td>
              </React.Fragment> : null}
        </tr>
        <tr>
          <td>Expected Signal Track <HelpBubble link="https://go.nzyme.org/wifi-network-monitoring-signal-tracks"/></td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_signal_tracks"/>{' '}</td>
          {renderControls ?
              <React.Fragment>
                <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_signal_tracks" bumpRevision={bumpRevision}/></td>
                <td><span className="text-muted">n/a</span></td>
              </React.Fragment> : null}
        </tr>
        <tr>
        <td>Disconnection Anomalies <HelpBubble link="https://go.nzyme.org/wifi-network-monitoring-disco-anomalies"/>
          </td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="disco_anomalies" skipEnabledCheck={true}/>{' '}
          </td>
          {renderControls ?
              <React.Fragment>
                <td><span className="text-muted">n/a</span></td>
                <td><a href={ApiRoutes.DOT11.MONITORING.DISCO.CONFIGURATION(ssid.uuid)}>Configure</a></td>
              </React.Fragment> : null}
        </tr>
        <tr>
          <td>Similar SSIDs</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="similar_ssids" skipEnabledCheck={false}/>{' '}
          </td>
          {renderControls ?
              <React.Fragment>
                <td><AlertEnabledTrigger ssid={ssid} parameter="similar_ssids" bumpRevision={bumpRevision}/></td>
                <td><a href={ApiRoutes.DOT11.MONITORING.SIMILAR_SSID_CONFIGURATION(ssid.uuid)}>Configure</a></td>
              </React.Fragment> : null}
        </tr>
        <tr>
          <td>Restricted SSID Substrings</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="restricted_ssid_substrings" skipEnabledCheck={false}/>{' '}
          </td>
          {renderControls ?
              <React.Fragment>
                <td><AlertEnabledTrigger ssid={ssid} parameter="restricted_ssid_substrings" bumpRevision={bumpRevision}/></td>
                <td><a href={ApiRoutes.DOT11.MONITORING.RESTRICTED_SUBSTRINGS_CONFIGURATION(ssid.uuid)}>Configure</a></td>
              </React.Fragment> : null}
        </tr>
        </tbody>
      </table>
  )

}

export default MonitoredNetworkAlertStatusTable;