import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import numeral from "numeral";
import ColumnSorting from "../../shared/ColumnSorting";
import EthernetMacAddress from "../../shared/context/macs/EthernetMacAddress";
import moment from "moment";
import AssetHostnames from "./AssetHostnames";
import AssetIpAddresses from "./AssetIpAddresses";
import Paginator from "../../misc/Paginator";
import AssetName from "./AssetName";
import {truncate} from "../../../util/Tools";
import ApiRoutes from "../../../util/ApiRoutes";

export default function AssetsTable(props) {

  const assets = props.assets;
  const page = props.page;
  const setPage = props.setPage;
  const perPage = props.perPage;
  const orderColumn = props.orderColumn;
  const setOrderColumn = props.setOrderColumn;
  const orderDirection = props.orderDirection;
  const setOrderDirection = props.setOrderDirection;

  const columnSorting = (columnName) => {
    return <ColumnSorting thisColumn={columnName}
                          orderColumn={orderColumn}
                          setOrderColumn={setOrderColumn}
                          orderDirection={orderDirection}
                          setOrderDirection={setOrderDirection} />
  }

  if (assets === null) {
    return <LoadingSpinner />
  }

  if (assets.assets.length === 0) {
    return <div className="alert alert-info mb-0">No Ethernet Assets discovered yet.</div>
  }

  return (
      <React.Fragment>
        <p className="mb-2 mt-0">
          <strong>Total:</strong> {numeral(assets.total).format("0,0")}
        </p>

        <table className="table table-sm table-hover table-striped mb-4 mt-3">
          <thead>
          <tr>
            <th style={{width: 170}}>MAC Address {columnSorting("mac")}</th>
            <th>OUI</th>
            <th>Name</th>
            <th>Hostname</th>
            <th>IP Address</th>
            <th>Last Seen {columnSorting("last_seen")}</th>
          </tr>
          </thead>
          <tbody>
          {assets.assets.map((a, i) => {
            return (
                <tr key={i}>
                  <td><EthernetMacAddress addressWithContext={a.mac} href={ApiRoutes.ETHERNET.ASSETS.DETAILS(a.uuid)} /></td>
                  <td><td>{a.oui ? truncate(a.oui, 30, false) : <span className="text-muted">Unknown</span>}</td></td>
                  <td><AssetName name={a.name} /></td>
                  <td><AssetHostnames hostnames={a.hostnames} /></td>
                  <td><AssetIpAddresses addresses={a.ip_addresses} /></td>
                  <td title={moment(a.last_seen).format()}>{moment(a.last_seen).fromNow()}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={assets.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}