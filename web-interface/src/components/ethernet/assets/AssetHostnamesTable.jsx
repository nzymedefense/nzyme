import React from "react";
import TransparentContextSource from "../../shared/context/transparent/TransparentContextSource";
import moment from "moment";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Paginator from "../../misc/Paginator";
import numeral from "numeral";
import ColumnSorting from "../../shared/ColumnSorting";

export default function AssetHostnamesTable(props) {

  const hostnames = props.hostnames;
  const page = props.page;
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

  if (hostnames === null) {
    return <LoadingSpinner />
  }

  if (hostnames.hostnames.length === 0) {
    return <div className="alert alert-info mb-0">No hostnames found.</div>
  }

  return (
      <React.Fragment>
        <p className="mb-2 mt-0">
          <strong>Total:</strong> {numeral(hostnames.total).format("0,0")}
        </p>

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Hostname {columnSorting("hostname")}</th>
            <th>Source {columnSorting("source")}</th>
            <th>First Seen {columnSorting("first_seen")}</th>
            <th>Last Seen {columnSorting("last_seen")}</th>
          </tr>
          </thead>
          <tbody>
          {hostnames.hostnames.map((hostname, i) => {
            return (
                <tr key={i}>
                  <td>{hostname.hostname}</td>
                  <td><TransparentContextSource source={hostname.source}/></td>
                  <td title={moment(hostname.first_seen).format()}>
                    {moment(hostname.first_seen).fromNow()}
                  </td>
                  <td title={moment(hostname.last_seen).format()}>
                    {moment(hostname.last_seen).fromNow()}
                  </td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={hostnames.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}