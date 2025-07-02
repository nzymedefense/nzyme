import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../../App";
import SSHService from "../../../../services/ethernet/SSHService";
import GenericWidgetLoadingSpinner from "../../../widgets/GenericWidgetLoadingSpinner";
import Paginator from "../../../misc/Paginator";
import SSHSessionsTableRow from "./SSHSessionsTableRow";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";

const sshService = new SSHService();

export default function SSHSessionsTable(props) {

  const [organizationId, tenantId] = useSelectedTenant();

  const timeRange = props.timeRange;
  const [data, setData] = useState(null);

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const perPage = 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setData(null);
    sshService.findAllTunnels(organizationId, tenantId, timeRange, selectedTaps, perPage, (page-1)*perPage, setData);
  }, [organizationId, tenantId, selectedTaps, timeRange, page]);

  if (!data) {
    return <GenericWidgetLoadingSpinner height={150} />
  }

  if (data.sessions.length === 0) {
    return <div className="mb-0 alert alert-info">No SSH sessions were observed during selected time range.</div>
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped mb-4 mt-3">
          <thead>
          <tr>
            <th>Session ID</th>
            <th>Client</th>
            <th>Server</th>
            <th>Status</th>
            <th>Bytes</th>
            <th>Duration</th>
            <th>Established At</th>
            <th>Terminated At</th>
          </tr>
          </thead>
          <tbody>
          {data.sessions.map((session, i) => {
            return <SSHSessionsTableRow session={session} key={i} />
          })}
          </tbody>
        </table>

        <Paginator itemCount={data.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}