import React from "react";
import {notify} from "react-notify-toast";
import Dot11Service from "../../../services/Dot11Service";

const dot11Service = new Dot11Service();

function MonitoredSecuritySuitesTable(props) {

  const ssid = props.ssid;
  const bumpRevision = props.bumpRevision;
  const alertingEnabled = props.alertingEnabled;

  const onDelete = function (uuid) {
    if (!confirm("Really delete security suite monitoring configuration?")) {
      return;
    }

    dot11Service.deleteMonitoredSecuritySuite(ssid.uuid, uuid, function () {
      bumpRevision();
      notify.show("Security suite monitoring configuration deleted.", "success");
    })
  }

  if (ssid.security_suites.length === 0) {
    return (
        <div className="alert alert-info">
          No monitored security suites configured yet.
        </div>
    )
  }

  return (
      <React.Fragment>
        {alertingEnabled ? null : <div className="alert alert-warning">Alerting for unexpected security suites is disabled.</div>}
        
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Security Suite</th>
            <th>&nbsp;</th>
          </tr>
          </thead>
          <tbody>
          {ssid.security_suites.map(function(suite, i) {
            return (
                <tr key={"suite-" + i}>
                  <td>{suite.suite}</td>
                  <td><a href="#" onClick={() => onDelete(suite.uuid)}>Delete</a></td>
                </tr>
            )
          })}
          </tbody>
        </table>
      </React.Fragment>
  )

}

export default MonitoredSecuritySuitesTable;