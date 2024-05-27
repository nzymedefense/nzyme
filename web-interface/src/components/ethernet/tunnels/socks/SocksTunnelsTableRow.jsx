import React from "react";

import numeral from "numeral";
import moment from "moment";
import IPAddressLink from "../../shared/IPAddressLink";
import HostnameLink from "../../shared/HostnameLink";
import L4Address from "../../shared/L4Address";

export default function SocksTunnelsTableRow(props) {

  const tunnel = props.tunnel;

  const duration = () => {
    let endTime;
    if (tunnel.connection_status === "Inactive") {
      endTime = moment(tunnel.terminated_at);
    } else {
      endTime = moment(new Date());
    }

    const duration = moment.duration(endTime.diff(tunnel.established_at));

    if (duration.asSeconds() > 60) {
      if (duration.asMinutes() > 60) {
        return numeral(duration.asHours()).format("0,0.0") + " Hours";
      } else {
        return numeral(duration.asMinutes()).format("0,0.0") + " Minutes";
      }
    } else {
      return numeral(duration.asSeconds()).format("0,0.0") + " Seconds";
    }
  }

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

  const connectionStatus = () => {
    switch (tunnel.connection_status) {
      case "Active":
        return <span className="badge bg-success">Active</span>
      case "Inactive":
        return <span className="badge bg-warning">Inactive</span>
      case "InactiveTimeout":
        return <span className="badge bg-warning">TCP Timeout</span>
      default:
        return <span className="badge bg-secondary">Invalid</span>
    }
  }

  return (
      <tr>
        <td><a href="#">{tunnel.uuid.substring(0, 6).toUpperCase()}</a></td>
        <td><L4Address address={tunnel.socks_server} /></td>
        <td>{destination()}</td>
        <td>{socksType()}</td>
        <td>{connectionStatus()}</td>
        <td>{numeral(tunnel.tunneled_bytes).format("0,0b")}</td>
        <td>{duration()}</td>
        <td>{moment(tunnel.established_at).format()}</td>
        <td>{tunnel.terminated_at ? moment(tunnel.terminated_at).format() : <span className="text-muted">n/a</span>}</td>
      </tr>
  )

}