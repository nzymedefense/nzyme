import React from "react";
import L4Address from "../../shared/L4Address";
import GenericConnectionStatus from "../../shared/GenericConnectionStatus";
import numeral from "numeral";
import {calculateConnectionDuration} from "../../../../util/Tools";
import moment from "moment/moment";
import SSHVersion from "./SSHVersion";
import SSHSessionKey from "../../shared/SSHSessionKey";
import {SSH_FILTER_FIELDS} from "./SSHFilterFields";
import FilterValueIcon from "../../../shared/filtering/FilterValueIcon";
import FullCopy from "../../../shared/FullCopy";

export default function SSHSessionsTableRow(props) {

  const session = props.session;
  const setFilters = props.setFilters;

  return (
      <tr>
        <td>
          <SSHSessionKey sessionKey={session.tcp_session_key} />

          <FilterValueIcon setFilters={setFilters}
                           fields={SSH_FILTER_FIELDS}
                           field="session_key"
                           value={session.tcp_session_key} />
        </td>
        <td>
          <L4Address address={session.client}
                     hidePort={true}
                     suffixElement={<span className="text-muted"><SSHVersion version={session.client_version} /></span>}
                     filterElement={<FilterValueIcon setFilters={setFilters}
                                                     fields={SSH_FILTER_FIELDS}
                                                     field="client_address"
                                                     value={session.client.address} />} />
        </td>
        <td>
          <L4Address address={session.server}
                     suffixElement={<span className="text-muted"><SSHVersion version={session.server_version} /></span>}
                     filterElement={<FilterValueIcon setFilters={setFilters}
                                                     fields={SSH_FILTER_FIELDS}
                                                     field="server_address"
                                                     value={session.server.address} />}/>
        </td>
        <td>
          <GenericConnectionStatus status={session.connection_status}/>
          <FilterValueIcon setFilters={setFilters}
                           fields={SSH_FILTER_FIELDS}
                           field="connection_status"
                           value={session.connection_status} />
        </td>
        <td>
          <FullCopy shortValue={numeral(session.tunneled_bytes).format("0,0b")} fullValue={session.tunneled_bytes} />

          <FilterValueIcon setFilters={setFilters}
                           fields={SSH_FILTER_FIELDS}
                           field="tunneled_bytes"
                           value={session.tunneled_bytes} />
        </td>
        <td>{calculateConnectionDuration(session.connection_status, session.established_at, session.terminated_at, session.most_recent_segment_time)}</td>
        <td>{moment(session.established_at).format()}</td>
        <td>{session.terminated_at ? moment(session.terminated_at).format() :
            <span className="text-muted">n/a</span>}</td>
      </tr>
  )
}