import React from "react";
import L4Address from "../../shared/L4Address";
import GenericConnectionStatus from "../../shared/GenericConnectionStatus";
import numeral from "numeral";
import {calculateConnectionDuration} from "../../../../util/Tools";
import moment from "moment/moment";
import SSHVersion from "./SSHVersion";
import SSHSessionKeyLink from "../../shared/SSHSessionKeyLink";

export default function SSHSessionsTableRow(props) {

  const session = props.session;

  return (
      <tr>
        <td><SSHSessionKeyLink sessionKey={session.tcp_session_key} /></td>
        <td>
          <L4Address address={session.client} hidePort={true} />{' '}
          <span className="text-muted"><SSHVersion version={session.client_version} /></span>
        </td>
        <td>
          <L4Address address={session.server}/>{' '}
          <span className="text-muted"><SSHVersion version={session.server_version}/></span>
        </td>
        <td><GenericConnectionStatus status={session.connection_status}/></td>
        <td>{numeral(session.tunneled_bytes).format("0,0b")}</td>
        <td>{calculateConnectionDuration(session.connection_status, session.established_at, session.terminated_at, session.most_recent_segment_time)}</td>
        <td>{moment(session.established_at).format()}</td>
        <td>{session.terminated_at ? moment(session.terminated_at).format() :
            <span className="text-muted">n/a</span>}</td>
      </tr>
  )
}