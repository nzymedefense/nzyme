import React from "react";
import FilterValueIcon from "../../../shared/filtering/FilterValueIcon";
import IPAddressLink from "../../shared/IPAddressLink";
import {SOCKS_FILTER_FIELDS} from "./SOCKSFilterFields";
import Hostname from "../../shared/Hostname";
import Port from "../../shared/Port";

export default function SOCKSTunnelDestination({tunnel, setFilters}) {

  if (tunnel.tunneled_destination_address) {
    return (
        <>
          <IPAddressLink ip={tunnel.tunneled_destination_address} port={tunnel.tunneled_destination_port} />

          {setFilters ? <FilterValueIcon setFilters={setFilters}
                                         fields={SOCKS_FILTER_FIELDS}
                                         field="tunneled_destination_address"
                                         value={tunnel.tunneled_destination_address}  /> : null }
        </>
    )
  }

  if (tunnel.tunneled_destination_host) {
    return (
        <>
          <Hostname hostname={tunnel.tunneled_destination_host} />
          {tunnel.tunneled_destination_port === undefined || tunnel.tunneled_destination_port === null ? null
              : <Port port={tunnel.tunneled_destination_port} />}

          {setFilters ? <FilterValueIcon setFilters={setFilters}
                                         fields={SOCKS_FILTER_FIELDS}
                                         field="tunneled_destination_host"
                                         value={tunnel.tunneled_destination_host}  /> : null }
        </>
    )
  }

  return "[Invalid]"

}