import React from "react";
import ApiRoutes from "../../../util/ApiRoutes";

function ClientBSSIDHistory(props) {

  const connectedBSSID = props.connectedBSSID;
  const bssids = props.bssids;

  const parsedSSIDs = (ssids) => {
    return ssids.map((ssid) => {
      return ssid ? ssid : "Hidden";
    })
  }

  if (!bssids || bssids.length === 0) {
    return <div className="alert alert-info mb-0">No connections observed.</div>
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped mb-0 mt-3">
          <thead>
          <tr>
            <th>BSSID</th>
            <th>OUI</th>
            <th>Advertised SSIDs</th>
          </tr>
          </thead>
          <tbody>
          {bssids.map(function (bssid, i) {
            return (
                <tr key={i}>
                  <td>
                    <span className={connectedBSSID && connectedBSSID === bssid.bssid ? "highlighted" : ""}>
                      <a href={ApiRoutes.DOT11.NETWORKS.BSSID(bssid.bssid)} className="dot11-mac">{bssid.bssid}</a>{' '}
                    </span>
                  </td>
                  <td>{bssid.oui ? bssid.oui : "Unknown"}</td>
                  <td>{parsedSSIDs(bssid.possible_ssids).join(", ")}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <p className="mb-0 mt-3 text-muted">
          The MAC address of the currently connected BSSID is <span className="highlighted">highlighted.</span>
        </p>
      </React.Fragment>
  )

}

export default ClientBSSIDHistory;