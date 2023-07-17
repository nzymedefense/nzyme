import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Paginator from "../../misc/Paginator";
import moment from "moment";
import SSIDsList from "../util/SSIDsList";
import ClientBSSIDHistory from "../util/ClientBSSIDHistory";
import ApiRoutes from "../../../util/ApiRoutes";


function ConnectedClientsTable(props) {

  const clients = props.clients;
  const minutes = props.minutes;

  const perPage = props.perPage;
  const page = props.page;
  const setPage = props.setPage;

  if (!clients) {
    return <LoadingSpinner />
  }

  if (clients.total === 0) {
    return (
        <div className="alert alert-info mb-2">
          No WiFi clients recorded in last {minutes} minutes.
        </div>
    )
  }

  return (
      <React.Fragment>
        <p className="mb-1">Total Connected Clients: {clients.total}</p>

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Client MAC</th>
            <th>Client OUI</th>
            <th>Connected BSSID</th>
            <th>Connected BSSID OUI</th>
            <th>BSSID Connection History</th>
            <th>Probe Requests</th>
            <th>Last Seen</th>
          </tr>
          </thead>
          <tbody>
          {clients.clients.map(function (client, i) {
            return (
                <tr key={"client-" + i}>
                  <td><a href={ApiRoutes.DOT11.CLIENTS.DETAILS(client.mac)}>{client.mac}</a></td>
                  <td>{client.oui ? client.oui :
                      <span className="text-muted">Unknown</span>}</td>
                  <td>{client.connected_bssid}</td>
                  <td>{client.connected_bssid_oui ? client.connected_bssid_oui :
                      <span className="text-muted">Unknown</span>}</td>
                  <td><ClientBSSIDHistory bssids={client.bssid_history} connectedBSSID={client.connected_bssid} /></td>
                  <td>
                    { client.probe_request_ssids && client.probe_request_ssids.length > 0 ?
                        <SSIDsList ssids={client.probe_request_ssids} /> : <span className="text-muted">None</span> }
                  </td>
                  <td>{moment(client.last_seen).format()}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={clients.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default ConnectedClientsTable;