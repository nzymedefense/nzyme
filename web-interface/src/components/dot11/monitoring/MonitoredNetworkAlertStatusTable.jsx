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
          { renderControls ? <th>&nbsp;</th> : null }
          { renderControls ? <th>&nbsp;</th> : null }
        </tr>
        </thead>
        <tbody>
        <tr>
          <td>Expected BSSIDs / Access Points</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_bssid"/></td>
          {renderControls ?
              <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_bssid" bumpRevision={bumpRevision}/>
              </td> : null}
          <td><span className="text-muted">n/a</span></td>
        </tr>
        <tr>
          <td>Expected Fingerprints</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_fingerprint"/></td>
          {renderControls ?
              <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_fingerprint" bumpRevision={bumpRevision}/>
              </td> : null}
          <td><span className="text-muted">n/a</span></td>
        </tr>
        <tr>
          <td>Expected Channels</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_channel"/></td>
          {renderControls ?
              <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_channel" bumpRevision={bumpRevision}/>
              </td> : null}
          <td><span className="text-muted">n/a</span></td>
        </tr>
        <tr>
          <td>Expected Security Suites</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_security_suites"/></td>
          {renderControls ?
              <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_security_suites" bumpRevision={bumpRevision}/>
              </td> : null}
          <td><span className="text-muted">n/a</span></td>
        </tr>
        <tr>
          <td>Expected Signal Track <HelpBubble link="https://go.nzyme.org/wifi-network-monitoring-signal-tracks"/></td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="unexpected_signal_tracks"/>{' '}</td>
          {renderControls ?
              <td><AlertEnabledTrigger ssid={ssid} parameter="unexpected_signal_tracks" bumpRevision={bumpRevision}/>
              </td> : null}
          <td><span className="text-muted">n/a</span></td>
        </tr>
        <tr>
          <td>Disconnection Anomalies <HelpBubble link="https://go.nzyme.org/wifi-network-monitoring-disco-anomalies"/>
          </td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="disco_anomalies" skipEnabledCheck={true}/>{' '}
          </td>
          {renderControls ? <td><span className="text-muted">n/a</span></td> : null}
          <td>
            <a href={ApiRoutes.DOT11.MONITORING.DISCO.CONFIGURATION(ssid.uuid)}>
              Configure
            </a>
          </td>
        </tr>
        <tr>
          <td>Similar SSIDs</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="similar_ssids" skipEnabledCheck={false}/>{' '}
          </td>
          {renderControls ?
              <td><AlertEnabledTrigger ssid={ssid} parameter="similar_ssids" bumpRevision={bumpRevision}/></td> : null}
          <td>
            <a href={ApiRoutes.DOT11.MONITORING.SIMILAR_SSID_CONFIGURATION(ssid.uuid)}>
              Configure
            </a>
          </td>
        </tr>
        <tr>
          <td>Restricted SSID Substrings</td>
          <td><MonitoredNetworkSingleAlertStatus ssid={ssid} parameter="restricted_ssid_substrings" skipEnabledCheck={false}/>{' '}
          </td>
          {renderControls ?
              <td><AlertEnabledTrigger ssid={ssid} parameter="restricted_ssid_substrings" bumpRevision={bumpRevision}/></td> : null}
          <td>
            <a href={ApiRoutes.DOT11.MONITORING.RESTRICTED_SUBSTRINGS_CONFIGURATION(ssid.uuid)}>
              Configure
            </a>
          </td>
        </tr>
        </tbody>
      </table>
  )

}

export default MonitoredNetworkAlertStatusTable;