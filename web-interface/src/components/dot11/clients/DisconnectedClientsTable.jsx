import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Dot11Service from "../../../services/Dot11Service";
import Paginator from "../../misc/Paginator";
import moment from "moment";
import SSIDsList from "../util/SSIDsList";
import ClientBSSIDHistory from "../util/ClientBSSIDHistory";

const dot11Service = new Dot11Service();
const MINUTES = 15;

function DisconnectedClientsTable() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [clients, setClients] = useState(null);

  const perPage = 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setClients(null);
    dot11Service.findAllDisconnectedClients(MINUTES, selectedTaps, setClients, perPage, (page-1)*perPage)
  }, [selectedTaps, page])

  if (!clients) {
    return <LoadingSpinner />
  }

  if (clients.total === 0) {
    return (
        <div className="alert alert-info mb-2">
          No WiFi clients recorded in last {MINUTES} minutes.
        </div>
    )
  }

  return (
      <React.Fragment>
        <p className="mb-1">Total Disconnected Clients: {clients.total}</p>

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Signal</th>
            <th>Client MAC</th>
            <th>Client OUI</th>
            <th>BSSID Connection History</th>
            <th>Probe Requests</th>
            <th>Last Seen</th>
          </tr>
          </thead>
          <tbody>
          {clients.clients.map(function (client, i) {
            return (
                <tr key={"client-" + i}>
                  <td>???</td>
                  <td><a href="">{client.mac}</a></td>
                  <td>{client.oui ? client.oui : <span className="text-muted">Unknown</span>}</td>
                  <td><ClientBSSIDHistory bssids={client.bssid_history} /></td>
                  <td>
                    { client.probe_request_ssids && client.probe_request_ssids.length > 0 ?
                        <SSIDsList ssids={client.probe_request_ssids} /> : <span className="text-muted">None</span> }
                  </td>
                  <td title={moment(client.last_seen).format()}>{moment(client.last_seen).fromNow()}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={clients.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default DisconnectedClientsTable;