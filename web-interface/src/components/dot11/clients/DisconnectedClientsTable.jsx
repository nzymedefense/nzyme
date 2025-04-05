import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Paginator from "../../misc/Paginator";
import moment from "moment";
import numeral from "numeral";
import SSIDsList from "../util/SSIDsList";
import ApiRoutes from "../../../util/ApiRoutes";
import Dot11MacAddress from "../../shared/context/macs/Dot11MacAddress";
import FilterValueIcon from "../../shared/filtering/FilterValueIcon";
import {DISCONNECTED_CLIENT_FILTER_FIELDS} from "./DisconnectedClientFilterFields";
import {CONNECTED_CLIENT_FILTER_FIELDS} from "./ConnectedClientFilterFields";

function DisconnectedClientsTable(props) {

  const clients = props.clients;

  const perPage = props.perPage;
  const page = props.page;
  const setPage = props.setPage;

  const setFilters = props.setFilters;

  if (!clients) {
    return <LoadingSpinner />
  }

  if (clients.total === 0) {
    return (
        <div className="alert alert-info mb-2">
          No WiFi clients recorded in selected time frame.
        </div>
    )
  }

  return (
      <React.Fragment>
        <p className="mb-1">Total Disconnected Clients: {numeral(clients.total).format("0,0")}</p>

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Client MAC</th>
            <th>Client OUI</th>
            <th>Probe Requests</th>
            <th>Last Seen</th>
          </tr>
          </thead>
          <tbody>
          {clients.clients.map(function (client, i) {
            return (
                <tr key={"client-" + i}>
                  <td>
                    <Dot11MacAddress addressWithContext={client.mac} href={ApiRoutes.DOT11.CLIENTS.DETAILS(client.mac.address)} />
                    <FilterValueIcon setFilters={setFilters}
                                     fields={DISCONNECTED_CLIENT_FILTER_FIELDS}
                                     field="client_mac"
                                     value={client.mac.address} />
                  </td>
                  <td>{client.mac.oui ? client.mac.oui : <span className="text-muted">Unknown</span>}</td>
                  <td>
                    { client.probe_request_ssids && client.probe_request_ssids.length > 0 ?
                        <SSIDsList ssids={client.probe_request_ssids}
                                   filterFields={CONNECTED_CLIENT_FILTER_FIELDS}
                                   filterFieldName="probe_request" />
                        : <span className="text-muted">None</span> }
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

export default DisconnectedClientsTable;