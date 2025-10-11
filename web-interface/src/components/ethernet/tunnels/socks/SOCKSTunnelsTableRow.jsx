import React from "react";

import numeral from "numeral";
import moment from "moment";
import IPAddressLink from "../../shared/IPAddressLink";
import L4Address from "../../shared/L4Address";
import {calculateConnectionDuration} from "../../../../util/Tools";
import GenericConnectionStatus from "../../shared/GenericConnectionStatus";
import Hostname from "../../shared/Hostname";
import Port from "../../shared/Port";
import FilterValueIcon from "../../../shared/filtering/FilterValueIcon";
import SocksTunnelId from "../../shared/SocksTunnelId";
import {SOCKS_FILTER_FIELDS} from "./SOCKSFilterFields";
import FullCopy from "../../../shared/FullCopy";
import InternalAddressOnlyWrapper from "../../shared/InternalAddressOnlyWrapper";
import EthernetMacAddress from "../../../shared/context/macs/EthernetMacAddress";

export default function SOCKSTunnelsTableRow(props) {

  const tunnel = props.tunnel;
  const setFilters = props.setFilters;

  const macFilter = (address, fieldName) => {
    if (!address) {
      return null;
    }

    return <FilterValueIcon setFilters={setFilters}
                            fields={SOCKS_FILTER_FIELDS}
                            field={fieldName}
                            value={address.address} />
  }

  const destination = () => {
    if (tunnel.tunneled_destination_address) {
      return (
          <>
            <IPAddressLink ip={tunnel.tunneled_destination_address} port={tunnel.tunneled_destination_port} />

            <FilterValueIcon setFilters={setFilters}
                             fields={SOCKS_FILTER_FIELDS}
                             field="tunneled_destination_address"
                             value={tunnel.tunneled_destination_address}  />
          </>
      )
    }

    if (tunnel.tunneled_destination_host) {
      return (
        <>
          <Hostname hostname={tunnel.tunneled_destination_host} />
          {tunnel.tunneled_destination_port === undefined || tunnel.tunneled_destination_port === null ? null
            : <Port port={tunnel.tunneled_destination_port} />}

          <FilterValueIcon setFilters={setFilters}
                           fields={SOCKS_FILTER_FIELDS}
                           field="tunneled_destination_host"
                           value={tunnel.tunneled_destination_host}  />
        </>
      )
    }

    return "[Invalid]"
  }

  return (
      <tr>
        <td>
          <SocksTunnelId tunnelId={tunnel.tcp_session_key} />

          <FilterValueIcon setFilters={setFilters}
                           fields={SOCKS_FILTER_FIELDS}
                           field="session_key"
                           value={tunnel.tcp_session_key}  />
        </td>
        <td>
          <L4Address address={tunnel.client}
                     filterElement={<FilterValueIcon setFilters={setFilters}
                                                     fields={SOCKS_FILTER_FIELDS}
                                                     field="client_address"
                                                     value={tunnel.client.address} />}/>
        </td>
        <td>
          <InternalAddressOnlyWrapper
            address={tunnel.client}
            inner={<EthernetMacAddress addressWithContext={tunnel.client.mac}
                                       filterElement={macFilter(tunnel.client.mac, "client_mac")}
                                       withAssetLink withAssetName />} />
        </td>
        <td>
          <L4Address address={tunnel.socks_server}
                     filterElement={<FilterValueIcon setFilters={setFilters}
                                                     fields={SOCKS_FILTER_FIELDS}
                                                     field="server_address"
                                                     value={tunnel.socks_server.address} />}/>
        </td>
        <td>
          <InternalAddressOnlyWrapper
            address={tunnel.socks_server}
            inner={<EthernetMacAddress addressWithContext={tunnel.socks_server.mac}
                                       filterElement={macFilter(tunnel.socks_server.mac, "server_mac")}
                                       withAssetLink withAssetName />} />
        </td>
        <td>{destination()}</td>
        <td>
          {tunnel.socks_type}
          <FilterValueIcon setFilters={setFilters}
                           fields={SOCKS_FILTER_FIELDS}
                           field="type"
                           value={tunnel.socks_type}  />
        </td>
        <td>
          <GenericConnectionStatus status={tunnel.connection_status} />
          <FilterValueIcon setFilters={setFilters}
                           fields={SOCKS_FILTER_FIELDS}
                           field="status"
                           value={tunnel.connection_status}  />
        </td>
        <td>
          <FullCopy shortValue={numeral(tunnel.tunneled_bytes).format("0,0b")} fullValue={tunnel.tunneled_bytes} />

          <FilterValueIcon setFilters={setFilters}
                           fields={SOCKS_FILTER_FIELDS}
                           field="tunneled_bytes"
                           value={tunnel.tunneled_bytes} />
        </td>
        <td>{calculateConnectionDuration(tunnel.connection_status, tunnel.established_at, tunnel.terminated_at)}</td>
        <td>{moment(tunnel.established_at).format()}</td>
      </tr>
  )

}