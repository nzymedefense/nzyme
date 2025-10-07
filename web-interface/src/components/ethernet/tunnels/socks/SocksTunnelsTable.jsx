import React, {useContext, useEffect, useState} from "react";
import SocksService from "../../../../services/ethernet/SocksService";
import {TapContext} from "../../../../App";
import GenericWidgetLoadingSpinner from "../../../widgets/GenericWidgetLoadingSpinner";
import Paginator from "../../../misc/Paginator";
import SocksTunnelsTableRow from "./SocksTunnelsTableRow";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";
import numeral from "numeral";

const socksService = new SocksService();

export default function SocksTunnelsTable(props) {

  const [organizationId, tenantId] = useSelectedTenant();

  const timeRange = props.timeRange;
  const filters = props.filters;
  const setFilters = props.setFilters;

  const [data, setData] = useState(null);

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const perPage = 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setData(null);
    socksService.findAllTunnels(organizationId, tenantId, timeRange, filters, selectedTaps, perPage, (page-1)*perPage, setData);
  }, [organizationId, tenantId, selectedTaps, filters, timeRange, page]);

  if (!data) {
    return <GenericWidgetLoadingSpinner height={150} />
  }

  if (data.tunnels.length === 0) {
    return <div className="mb-0 alert alert-info">No SOCKS tunnels were observed during selected time range.</div>
  }

  return (
      <React.Fragment>
        <strong>Total:</strong> {numeral(data.total).format("0,0")}

        <table className="table table-sm table-hover table-striped mb-4 mt-3">
          <thead>
          <tr>
            <th>Tunnel ID</th>
            <th>Client</th>
            <th>Tunnel Server</th>
            <th>Tunnel Destination</th>
            <th>Type</th>
            <th>Status</th>
            <th>Bytes</th>
            <th>Duration</th>
            <th>Established At</th>
            <th>Terminated At</th>
          </tr>
          </thead>
          <tbody>
          {data.tunnels.map((tunnel, i) => {
            return <SocksTunnelsTableRow tunnel={tunnel} key={i} setFilters={setFilters} />
          })}
          </tbody>
        </table>

        <Paginator itemCount={data.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}