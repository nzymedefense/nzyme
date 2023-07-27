import React from "react";
import MonitoredBSSIDFingerprintsModal from "./MonitoredBSSIDFingerprintsModal";
import Dot11Service from "../../../services/Dot11Service";
import {notify} from "react-notify-toast";

const dot11Service = new Dot11Service();

function Dot11MonitoredBSSIDs(props) {

  const bssids = props.bssids;
  const bumpRevision = props.bumpRevision;
  const parentIsLoading = props.parentIsLoading;

  if (!bssids || bssids.length === 0) {
    return (
        <div className="alert alert-info">
          No monitored BSSIDs configured yet.
        </div>
    )
  }

  const deleteBSSID = function (bssid) {
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
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>BSSID</th>
            <th>BSSID Vendor</th>
            <th>Expected Fingerprints</th>
            <th>Online in last 15 Minutes</th>
            <th>&nbsp;</th>
          </tr>
          </thead>
          <tbody>
          {bssids.map(function(bssid, i) {
            return (
              <tr key={"bssid-" + i}>
                <td>
                  <a href="#"
                     data-bs-toggle="modal"
                     data-bs-target={"#bssid-fingerprints-" + bssid.bssid.replaceAll(":", "")}>
                    {bssid.bssid}</a>
                </td>
                <td>{bssid.bssid_oui ? bssid.bssid_oui : "Unknown Vendor"}</td>
                <td>{bssid.fingerprints.length}</td>
                <td>
                  {bssid.is_online ? <i className="fa-solid fa-circle-check text-success"></i>
                      : <i className="fa-solid fa-triangle-exclamation text-danger"></i>}
                </td>
                <td>
                  <a href="#" onClick={() => deleteBSSID(bssid)}>Delete</a>
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

export default Dot11MonitoredBSSIDs;