import React from "react";
import GenericWidgetLoadingSpinner from "../../../widgets/GenericWidgetLoadingSpinner";
import Paginator from "../../../misc/Paginator";
import DHCPTransactionNotesCount from "./DHCPTransactionNotesCount";
import DHCPDuration from "./DHCPDuration";
import moment from "moment";
import EthernetMacAddress from "../../../shared/context/macs/EthernetMacAddress";
import DHCPTransactionSuccess from "./DHCPTransactionSuccess";
import numeral from "numeral";
import ApiRoutes from "../../../../util/ApiRoutes";
import ColumnSorting from "../../../shared/ColumnSorting";
import AssetName from "../../shared/AssetName";
import L4Address from "../../shared/L4Address";
import FullCopyShortenedId from "../../../shared/FullCopyShortenedId";
import FilterValueIcon from "../../../shared/filtering/FilterValueIcon";
import {DHCP_FILTER_FIELDS} from "./DHCPFilterFields";

export default function DHCPTransactionsTable(props) {

  const data = props.data;
  const page = props.page;
  const setFilters = props.setFilters;
  const setPage = props.setPage;
  const perPage = props.perPage;
  const setOrderColumn = props.setOrderColumn;
  const orderColumn = props.orderColumn;
  const setOrderDirection = props.setOrderDirection;
  const orderDirection = props.orderDirection;

  const columnSorting = (columnName) => {
    return <ColumnSorting thisColumn={columnName}
                          orderColumn={orderColumn}
                          setOrderColumn={setOrderColumn}
                          orderDirection={orderDirection}
                          setOrderDirection={setOrderDirection} />
  }

  if (!data) {
    return <GenericWidgetLoadingSpinner height={150} />
  }

  if (data.transactions.length === 0) {
    return <div className="mb-0 alert alert-info">No DHCP transactions were observed during selected time range.</div>
  }

  return (
      <React.Fragment>
        <p className="mb-2 mt-0">
          <strong>Total:</strong> {numeral(data.total).format("0,0")}
        </p>

        <table className="table table-sm table-hover table-striped mb-4 mt-3">
          <thead>
          <tr>
            <th style={{width: 200}}>Initiated At {columnSorting("initiated_at")}</th>
            <th>Type {columnSorting("transaction_type")}</th>
            <th>Client MAC {columnSorting("client_mac")}</th>
            <th>Client Name</th>
            <th>Server MAC {columnSorting("server_mac")}</th>
            <th>Server Name</th>
            <th>Requested IP {columnSorting("requested_ip_address")}</th>
            <th>Fingerprint {columnSorting("fingerprint")}</th>
            <th>Success</th>
            <th>Complete</th>
            <th>Duration</th>
            <th>Notes</th>
          </tr>
          </thead>
          <tbody>
          {data.transactions.map((t, i) => {
            return (
                <tr key={i}>
                  <td>
                    <a href={ApiRoutes.ETHERNET.ASSETS.DHCP.TRANSACTION_DETAILS(t.transaction_id) + "?transaction_time=" + encodeURIComponent(t.first_packet)}>
                      {moment(t.first_packet).format()}
                    </a>
                  </td>
                  <td>
                    {t.transaction_type}

                    <FilterValueIcon setFilters={setFilters}
                                     fields={DHCP_FILTER_FIELDS}
                                     field="transaction_type"
                                     value={t.transaction_type} />
                  </td>
                  <td>
                    <EthernetMacAddress addressWithContext={t.client_mac}
                                        filterElement={<FilterValueIcon setFilters={setFilters}
                                                                        fields={DHCP_FILTER_FIELDS}
                                                                        field="client_mac"
                                                                        value={t.client_mac.address} />}
                                        withAssetLink />
                  </td>
                  <td><AssetName addressWithContext={t.client_mac} /></td>
                  <td>
                    {t.server_mac ?
                      <EthernetMacAddress addressWithContext={t.server_mac}
                                          filterElement={<FilterValueIcon setFilters={setFilters}
                                                                          fields={DHCP_FILTER_FIELDS}
                                                                          field="server_mac"
                                                                          value={t.server_mac.address} />}
                                          withAssetLink />
                      : <span className="text-muted">n/a</span>}</td>
                  <td><AssetName addressWithContext={t.server_mac} /></td>
                  <td>
                    <L4Address address={t.requested_ip_address}
                               filterElement={<FilterValueIcon setFilters={setFilters}
                                                               fields={DHCP_FILTER_FIELDS}
                                                               field="requested_ip"
                                                               value={t.requested_ip_address.address} />}
                               hideFlag />
                  </td>
                  <td>
                    <FullCopyShortenedId value={t.fingerprint} />
                    <FilterValueIcon setFilters={setFilters}
                                     fields={DHCP_FILTER_FIELDS}
                                     field="fingerprint"
                                     value={t.fingerprint} />
                  </td>
                  <td><DHCPTransactionSuccess success={t.is_successful} /></td>
                  <td>{t.is_complete ? <span className="text-success">Complete</span>
                      : <span className="text-warning">Incomplete</span>}</td>
                  <td><DHCPDuration duration={t.duration_ms} /></td>
                  <td><DHCPTransactionNotesCount notes={t.notes} /></td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={data.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  );

}