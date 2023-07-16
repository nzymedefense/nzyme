import React from "react";

function ClientBSSIDHistory(props) {

  const bssids = props.bssids;

  if (!bssids || bssids.length === 0) {
    return <span className="text-muted">None</span>
  }

  return (
      <React.Fragment>
        <ul className="mb-0 client-bssids">
        {bssids.map(function (bssid, i) {
          return (
              <li key={"clientbssid-" + i}>
                {bssid.bssid} {bssid.oui ? <span className="text-muted">({bssid.oui})</span> : null}
                <ul>
                  {bssid.possible_ssids.map(function (ssid, x) {
                    return <li key={"clientbssidssid-" + x}>Advertised SSID: {ssid ? ssid :  <span className="text-muted">Hidden</span>}</li>
                  })}
                </ul>
              </li>
          )
        })}
        </ul>
      </React.Fragment>
  )

}

export default ClientBSSIDHistory;