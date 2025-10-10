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
import EthernetMacAddress from "../../../shared/context/macs/EthernetMacAddress";
import InternalAddressOnlyWrapper from "../../shared/InternalAddressOnlyWrapper";

export default function SSHSessionsTableRow(props) {

  const session = props.session;
  const setFilters = props.setFilters;

  const macFilter = (address, fieldName) => {
    if (!address) {
      return null;
    }

    return <FilterValueIcon setFilters={setFilters}
                            fields={SSH_FILTER_FIELDS}
                            field={fieldName}
                            value={address.address} />
  }

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
                     filterElement={<FilterValueIcon setFilters={setFilters}
                                                     fields={SSH_FILTER_FIELDS}
                                                     field="client_address"
                                                     value={session.client.address} />} />
        </td>
        <td>
          <InternalAddressOnlyWrapper
            address={session.client}
            inner={<EthernetMacAddress addressWithContext={session.client.mac}
                                       filterElement={macFilter(session.client.mac, "client_mac")}
                                       withAssetLink withAssetName />} />
        </td>
        <td><SSHVersion version={session.client_version} /></td>
        <td>
          <L4Address address={session.server}
                     filterElement={<FilterValueIcon setFilters={setFilters}
                                                     fields={SSH_FILTER_FIELDS}
                                                     field="server_address"
                                                     value={session.server.address} />}/>
        </td>
        <td>
          <InternalAddressOnlyWrapper
            address={session.server}
            inner={<EthernetMacAddress addressWithContext={session.server.mac}
                                       filterElement={macFilter(session.server.mac, "server_mac")}
                                       withAssetLink withAssetName />} />
        </td>
        <td><SSHVersion version={session.server_version} /></td>
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
      </tr>
  )
}