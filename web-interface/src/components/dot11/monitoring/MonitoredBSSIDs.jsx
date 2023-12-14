import React from "react";
import MonitoredBSSIDFingerprintsModal from "./MonitoredBSSIDFingerprintsModal";
import Dot11Service from "../../../services/Dot11Service";
import {notify} from "react-notify-toast";
import Dot11MacAddress from "../../shared/context/macs/Dot11MacAddress";

const dot11Service = new Dot11Service();

function MonitoredBSSIDs(props) {

  const bssids = props.bssids;
  const bssidAlertingEnabled = props.bssidAlertingEnabled;
  const fingerprintAlertingEnabled = props.fingerprintAlertingEnabled;

  const bumpRevision = props.bumpRevision;
  const parentIsLoading = props.parentIsLoading;

  if (!bssids || bssids.length === 0) {
    return (
        <div className="alert alert-info">
          No monitored BSSIDs configured yet.
        </div>
    )
  }

  const deleteBSSID = function (e, bssid) {
    e.preventDefault();

    if (!confirm("Really delete BSSID monitoring configuration?")) {
      return;
    }

    dot11Service.deleteMonitoredBSSID(bssid.ssid_uuid, bssid.uuid, function () {
      bumpRevision();
      notify.show("BSSID monitoring configuration deleted.", "success");
    })
  }

  return (
      <React.Fragment>
        {bssidAlertingEnabled ? null : <div className="alert alert-warning">Alerting for unexpected BSSIDs is disabled.</div>}
        {fingerprintAlertingEnabled ? null : <div className="alert alert-warning">Alerting for unexpected fingerprints is disabled.</div>}

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>BSSID</th>
            <th>BSSID Vendor</th>
            <th>Expected Fingerprints</th>
            <th>Online in last 15 Minutes</th>
            <th>Fingerprints</th>
            <th>&nbsp;</th>
          </tr>
          </thead>
          <tbody>
          {bssids.map(function(bssid, i) {
            return (
              <tr key={"bssid-" + i}>
                <td><Dot11MacAddress addressWithContext={bssid.mac} /></td>
                <td>{bssid.mac.oui ? bssid.mac.oui : "Unknown Vendor"}</td>
                <td>{bssid.fingerprints.length}</td>
                <td>
                  {bssid.is_online ? <i className="fa-solid fa-circle-check text-success"></i>
                      : <i className="fa-solid fa-triangle-exclamation text-danger"></i>}
                </td>
                <td>
                  <a href="#"
                     data-bs-toggle="modal"
                     data-bs-target={"#bssid-fingerprints-" + bssid.mac.address.replaceAll(":", "")}>
                    Manage Monitored Fingerprints
                  </a>
                </td>
                <td>
                  <a href="#" onClick={(e) => deleteBSSID(e, bssid)}>Delete</a>
                </td>
              </tr>
            )
          })}
          </tbody>
        </table>

        {bssids.map(function(bssid, i) {
          return <MonitoredBSSIDFingerprintsModal key={"bssidmodal-" + i}
                                                  bssid={bssid}
                                                  bumpRevision={bumpRevision}
                                                  parentIsLoading={parentIsLoading} />
        })}
      </React.Fragment>
  )

}

export default MonitoredBSSIDs;