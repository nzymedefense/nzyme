import React from "react";
import TransparentContextSource from "./TransparentContextSource";
import moment from "moment/moment";

export default function TransparentIpAddressTable(props) {

  const addresses = props.addresses;

  if (!addresses || addresses.length === 0) {
    return <div className="alert alert-info mb-0">No IP addresses found.</div>
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>IP Address</th>
          <th>Source</th>
          <th>Created</th>
          <th>Last Seen</th>
        </tr>
        </thead>
        <tbody>
        {addresses.map((address, i) => {
          return (
              <tr key={i}>
                <td>{address.ip_address}</td>
                <td><TransparentContextSource source={address.source}/></td>
                <td title={moment(address.created_at).format()}>
                  {moment(address.created_at).fromNow()}
                </td>
                <td title={moment(address.last_seen).format()}>
                  {moment(address.last_seen).fromNow()}
                </td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}