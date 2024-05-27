import React, {useContext, useEffect, useState} from "react";
import DNSService from "../../../../services/ethernet/DNSService";
import GenericWidgetLoadingSpinner from "../../../widgets/GenericWidgetLoadingSpinner";
import {TapContext} from "../../../../App";
import DNSEntropyLogTableRow from "./DNSEntropyLogTableRow";
import Paginator from "../../../misc/Paginator";

const dnsService = new DNSService()

export default function DNSEntropyLogTable(props) {

  const timeRange = props.timeRange;
  const [data, setData] = useState(null);

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const perPage = 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setData(null);
    dnsService.getGlobalEntropyLog(timeRange, selectedTaps, perPage, (page-1)*perPage, setData);
  }, [selectedTaps, timeRange, page]);

  if (!data) {
    return <GenericWidgetLoadingSpinner height={500} />
  }

  if (data.logs.length === 0) {
    return <div className="mb-0 alert alert-info">No outliers during selected time range.</div>
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped mb-4 mt-3">
          <thead>
          <tr>
            <th>Query</th>
            <th>Type</th>
            <th>Timestamp</th>
            <th>eTLD</th>
            <th>Client</th>
            <th>Server</th>
            <th>Entropy / Mean</th>
            <th>Z-Score</th>
          </tr>
          </thead>
          <tbody>
          {data.logs.map((log, i) => {
            return <DNSEntropyLogTableRow log={log} key={i} />
          })}
          </tbody>
        </table>

        <Paginator itemCount={data.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}