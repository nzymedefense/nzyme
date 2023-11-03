import React from "react";
import ApiRoutes from "../../../../util/ApiRoutes";

function SSIDAccessPointClients(props) {

  const clients = props.clients.filter(c => c);

  if (!clients || clients.length === 0) {
    return (
        <div className="alert alert-info mb-0">
          No clients have been recorded.
        </div>
    )
  }

  return (
      <React.Fragment>
        <p className="mb-1">Total Clients: {clients.length}</p>

        <table className="table table-sm table-hover table-striped mb-0">
          <thead>
          <tr>
            <th>Client Address</th>
            <th>OUI / Vendor</th>
          </tr>
          </thead>
          <tbody>
          {clients.slice(0,5).map(function (client, i) {
            return (
                <tr key={"client-" + i}>
                  <td><a href={ApiRoutes.DOT11.CLIENTS.DETAILS(client.mac)} className="dot11-mac">{client.mac}</a></td>
                  <td>{client.oui ? client.oui : "Unknown"}</td>
                </tr>
                )
          })}
          {clients.length > 5 ? <tr><td colSpan={2} className="text-center"><a href="#" data-bs-toggle="modal" data-bs-target="#ssid-bssid-clients">Show more clients</a></td></tr> : null }
          </tbody>
        </table>

        <div className="modal configuration-dialog" id="ssid-bssid-clients"
             data-bs-keyboard="true" data-bs-backdrop="static" tabIndex="-1"
             aria-labelledby="staticBackdropLabel" aria-hidden="true">
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h3 className="modal-title">Clients of Access Point</h3>
                <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </div>

              <div className="modal-body">
                <table className="table table-sm table-hover table-striped mb-0">
                  <thead>
                  <tr>
                    <th>Client Address</th>
                    <th>OUI / Vendor</th>
                  </tr>
                  </thead>
                  <tbody>
                  {clients.map(function (client, i) {
                    return (
                        <tr key={"client-" + i}>
                          <td>
                            <a href={ApiRoutes.DOT11.CLIENTS.DETAILS(client.mac)} className="dot11-mac">
                              {client.mac}
                            </a>
                          </td>
                          <td>{client.oui ? client.oui : "Unknown"}</td>
                        </tr>
                    )
                  })}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default SSIDAccessPointClients;