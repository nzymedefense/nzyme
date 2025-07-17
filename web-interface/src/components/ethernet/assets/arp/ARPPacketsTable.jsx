import React from "react";
import ColumnSorting from "../../../shared/ColumnSorting";
import GenericWidgetLoadingSpinner from "../../../widgets/GenericWidgetLoadingSpinner";
import numeral from "numeral";
import moment from "moment/moment";
import EthernetMacAddress from "../../../shared/context/macs/EthernetMacAddress";
import Paginator from "../../../misc/Paginator";
import AssetName from "../../shared/AssetName";
import L4Address from "../../shared/L4Address";

export default function ARPPacketsTable(props) {

  const packets = props.packets;
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

  if (!packets) {
    return <GenericWidgetLoadingSpinner height={150} />
  }

  if (packets.packets.length === 0) {
    return <div className="mb-0 alert alert-info">No ARP packets were observed during selected time range.</div>
  }


  return (
      <React.Fragment>
        <p className="mb-2 mt-0">
          <strong>Total:</strong> {numeral(packets.total).format("0,0")}
        </p>

        <table className="table table-sm table-hover table-striped mb-4 mt-3">
          <thead>
          <tr>
            <th style={{width: 200}}>Timestamp {columnSorting("timestamp")}</th>
            <th>ARP Operation</th>
            <th>Source MAC</th>
            <th>Destination MAC</th>
            <th>Target MAC</th>
            <th>Target Name</th>
            <th>Target Address</th>
            <th>Target MAC</th>
            <th>Target Name</th>
            <th>Target Address</th>
          </tr>
          </thead>
          <tbody>
          {packets.packets.map((p, i) => {
            return (
                <tr key={i}>
                  <td>{moment(p.timestamp).format()}</td>
                  <td>{p.operation}</td>
                  <td><EthernetMacAddress addressWithContext={p.ethernet_source_mac} withAssetLink /></td>
                  <td><EthernetMacAddress addressWithContext={p.ethernet_destination_mac} withAssetLink /></td>
                  <td><EthernetMacAddress addressWithContext={p.arp_sender.mac} withAssetLink /></td>
                  <td><AssetName addressWithContext={p.arp_sender ? p.arp_sender.mac : null} /></td>
                  <td><L4Address address={p.arp_sender} hideFlag /></td>
                  <td><EthernetMacAddress addressWithContext={p.arp_target.mac} withAssetLink /></td>
                  <td><AssetName addressWithContext={p.arp_target ? p.arp_target.mac : null} /></td>
                  <td><L4Address address={p.arp_target} hideFlag /></td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={packets.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  );

}