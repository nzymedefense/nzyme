import React from "react";
import TransparentContextSource from "./TransparentContextSource";
import moment from "moment/moment";

export default function TransparentHostnamesTable(props) {

  const hostnames = props.hostnames;

  if (!hostnames || hostnames.length === 0) {
    return <div className="alert alert-info mb-0">No hostnames found.</div>
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Hostname</th>
          <th>Source</th>
          <th>Created</th>
          <th>Last Seen</th>
        </tr>
        </thead>
        <tbody>
        {hostnames.map((hostname, i) => {
          return (
              <tr key={i}>
                <td>{hostname.hostname}</td>
                <td><TransparentContextSource source={hostname.source}/></td>
                <td title={moment(hostname.created_at).format()}>
                  {moment(hostname.created_at).fromNow()}
                </td>
                <td title={moment(hostname.last_seen).format()}>
                  {moment(hostname.last_seens).fromNow()}
                </td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}