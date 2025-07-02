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

export default function L4SessionsTable(props) {

  const sessions = props.sessions;
  const timeRange = props.timeRange;
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
            <th>Session</th>
            <th>Type</th>
            <th>Source MAC</th>
            <th>Source Address</th>
            <th>Source Name</th>
            <th>Destination MAC</th>
            <th>Destination Address</th>
            <th>Destination Name</th>
            <th>Bytes</th>
            <th>Segments</th>
            <th>Last Activity</th>
          </tr>
          </thead>
          <tbody>
          {sessions.sessions.map((s, i) => {
            return (
                <tr key={i}>
                  <td><a href="#" className="machine-data">{s.session_key.substr(0, 6)}</a></td>
                  <td>{s.l4_type}</td>
                  <td><InternalAddressOnlyWrapper address={s.source} inner={<EthernetMacAddress addressWithContext={s.source.mac} href="#" />} /></td>
                  <td><L4Address address={s.source} /></td>
                  <td><AssetName name={s.source.asset_name} /></td>
                  <td><InternalAddressOnlyWrapper address={s.destination} inner={<EthernetMacAddress addressWithContext={s.destination.mac} href="#" />} /></td>
                  <td><L4Address address={s.destination} /></td>
                  <td><AssetName name={s.destination.asset_name} /></td>
                  <td>{numeral(s.bytes_count).format("0b")}</td>
                  <td>{numeral(s.segments_count).format("0,0")}</td>
                  <td title={moment(s.most_recent_segment_time).format()}>{moment(s.most_recent_segment_time).fromNow()}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={sessions.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )
}