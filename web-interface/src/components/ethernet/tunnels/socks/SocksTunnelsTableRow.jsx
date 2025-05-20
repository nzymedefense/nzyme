import React from "react";

import numeral from "numeral";
import moment from "moment";
import IPAddressLink from "../../shared/IPAddressLink";
import HostnameLink from "../../shared/HostnameLink";
import L4Address from "../../shared/L4Address";
import {calculateConnectionDuration} from "../../../../util/Tools";
import GenericConnectionStatus from "../../shared/GenericConnectionStatus";
import SocksTunnelIdLink from "../../shared/SocksTunnelIdLink";

export default function SocksTunnelsTableRow(props) {

  const tunnel = props.tunnel;

  const socksType = () => {
    switch (tunnel.socks_type) {
      case "Socks5": return "SOCKS5";
      case "Socks4": return "SOCKS4";
      case "Socks4A": return "SOCKS4-A";
      default: return tunnel.socks_type
    }
  }

  const destination = () => {
    if (tunnel.tunneled_destination_address) {
      return <IPAddressLink ip={tunnel.tunneled_destination_address} port={tunnel.tunneled_destination_port} />
    }

    if (tunnel.tunneled_destination_host) {
      return <HostnameLink hostname={tunnel.tunneled_destination_host} port={tunnel.tunneled_destination_port} />
    }

    return "[Invalid]"
  }

  return (
      <tr>
        <td><SocksTunnelIdLink tunnelId={tunnel.tcp_session_key} /></td>
        <td><L4Address address={tunnel.client}/></td>
        <td><L4Address address={tunnel.socks_server}/></td>
        <td>{destination()}</td>
        <td>{socksType()}</td>
        <td><GenericConnectionStatus status={tunnel.connection_status} /></td>
        <td>{numeral(tunnel.tunneled_bytes).format("0,0b")}</td>
        <td>{calculateConnectionDuration(tunnel.connection_status, tunnel.established_at, tunnel.terminated_at)}</td>
        <td>{moment(tunnel.established_at).format()}</td>
        <td>{tunnel.terminated_at ? moment(tunnel.terminated_at).format() : <span className="text-muted">n/a</span>}</td>
      </tr>
  )

}