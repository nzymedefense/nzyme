import React from "react";
import ColumnSorting from "../../shared/ColumnSorting";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import numeral from "numeral";
import Paginator from "../../misc/Paginator";
import moment from "moment";
import L4Address from "../shared/L4Address";
import EthernetMacAddress from "../../shared/context/macs/EthernetMacAddress";
import InternalAddressOnlyWrapper from "../shared/InternalAddressOnlyWrapper";
import AssetName from "../shared/AssetName";
import L4SessionState from "./L4SessionState";
import FilterValueIcon from "../../shared/filtering/FilterValueIcon";
import {L4_SESSIONS_FILTER_FIELDS} from "./L4SessionFilterFields";
import ApiRoutes from "../../../util/ApiRoutes";

export default function L4SessionsTable(props) {

  const sessions = props.sessions;
  const page = props.page;
  const setPage = props.setPage;
  const perPage = props.perPage;
  const setOrderColumn = props.setOrderColumn;
  const orderColumn = props.orderColumn;
  const setOrderDirection = props.setOrderDirection;
  const orderDirection = props.orderDirection;
  const setFilters = props.setFilters;

  const columnSorting = (columnName) => {
    return <ColumnSorting thisColumn={columnName}
                          orderColumn={orderColumn}
                          setOrderColumn={setOrderColumn}
                          orderDirection={orderDirection}
                          setOrderDirection={setOrderDirection} />
  }

  const macFilter = (address, fieldName) => {
    if (!address) {
      return null;
    }

    return <FilterValueIcon setFilters={setFilters}
                            fields={L4_SESSIONS_FILTER_FIELDS}
                            field={fieldName}
                            value={address.address} />
  }

  if (!sessions) {
    return <GenericWidgetLoadingSpinner height={700} />
  }

  if (sessions.sessions.length === 0) {
    return <div className="mb-0 alert alert-info">No sessions were observed during selected time range.</div>
  }

  return (
      <React.Fragment>
        <p className="mb-2 mt-0">
          <strong>Total:</strong> {numeral(sessions.total).format("0,0")}
        </p>

        <table className="table table-sm table-hover table-striped mb-4 mt-3">
          <thead>
          <tr>
            <th>Session {columnSorting("session_key")}</th>
            <th>State {columnSorting("state")}</th>
            <th>Type {columnSorting("l4_type")}</th>
            <th>Source MAC {columnSorting("source_mac")}</th>
            <th>Source Address {columnSorting("source_address")}</th>
            <th title="Source Name">Src. Name</th>
            <th>Destination MAC {columnSorting("destination_mac")}</th>
            <th>Destination Address {columnSorting("destination_address")}</th>
            <th title="Destination Name">Dest. Name</th>
            <th>Bytes {columnSorting("bytes_count")}</th>
            <th>Last Activity {columnSorting("most_recent_segment_time")}</th>
          </tr>
          </thead>
          <tbody>
          {sessions.sessions.map((s, i) => {
            return (
                <tr key={i}>
                  <td>
                    <a href="#" className="machine-data">{s.session_key.substr(0, 6)}</a>
                    <FilterValueIcon setFilters={setFilters}
                                     fields={L4_SESSIONS_FILTER_FIELDS}
                                     field="session_key"
                                     value={s.session_key} />
                  </td>
                  <td>
                    <L4SessionState state={s.state} />
                    <FilterValueIcon setFilters={setFilters}
                                     fields={L4_SESSIONS_FILTER_FIELDS}
                                     field="state"
                                     value={s.state} />
                  </td>
                  <td>
                    {s.l4_type}
                    <FilterValueIcon setFilters={setFilters}
                                     fields={L4_SESSIONS_FILTER_FIELDS}
                                     field="l4_type"
                                     value={s.l4_type} />
                  </td>
                  <td>
                    <InternalAddressOnlyWrapper
                        address={s.source}
                        inner={<EthernetMacAddress
                            filterElement={macFilter(s.source.mac, "source_mac")}
                            addressWithContext={s.source.mac}
                            assetId={s.source && s.source.asset_id ? s.source.asset_id : null}
                            href={s.source && s.source.asset_id
                                ? ApiRoutes.ETHERNET.ASSETS.DETAILS(s.source.asset_id) : null} />} />
                  </td>
                  <td>
                    <L4Address address={s.source} />
                    <FilterValueIcon setFilters={setFilters}
                                     fields={L4_SESSIONS_FILTER_FIELDS}
                                     field="source_address"
                                     value={s.source.address} />
                  </td>
                  <td><AssetName name={s.source.asset_name} /></td>
                  <td>
                    <InternalAddressOnlyWrapper
                        address={s.destination}
                        inner={<EthernetMacAddress
                            filterElement={macFilter(s.destination.mac, "destination_mac")}
                            addressWithContext={s.destination.mac}
                            href={s.destination && s.destination.asset_id
                                ? ApiRoutes.ETHERNET.ASSETS.DETAILS(s.destination.asset_id) : null} />} />
                  </td>
                  <td>
                    <L4Address address={s.destination} />
                    <FilterValueIcon setFilters={setFilters}
                                     fields={L4_SESSIONS_FILTER_FIELDS}
                                     field="destination_address"
                                     value={s.destination.address} />
                  </td>
                  <td><AssetName name={s.destination.asset_name} /></td>
                  <td>
                    {numeral(s.bytes_count).format("0b")}
                    <FilterValueIcon setFilters={setFilters}
                                     fields={L4_SESSIONS_FILTER_FIELDS}
                                     field="bytes_count"
                                     value={s.bytes_count} />
                  </td>
                  <td title={moment(s.most_recent_segment_time).format()}>
                    {moment(s.most_recent_segment_time).fromNow()}
                  </td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={sessions.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )
}