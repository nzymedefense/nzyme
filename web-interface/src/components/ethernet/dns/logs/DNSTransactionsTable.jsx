import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../../App";
import DNSService from "../../../../services/ethernet/DNSService";
import GenericWidgetLoadingSpinner from "../../../widgets/GenericWidgetLoadingSpinner";
import Paginator from "../../../misc/Paginator";
import DNSTransactionLogTableRow from "./DNSTransactionLogTableRow";

import numeral from "numeral";

const dnsService = new DNSService();

export default function DNSTransactionsTable(props) {

  const timeRange = props.timeRange;
  const filters = props.filters;

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [data, setData] = useState(null);

  const perPage = 100;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setData(null);
    dnsService.findAllTransactions(timeRange, selectedTaps, perPage, (page-1)*perPage, setData);
  }, [selectedTaps, timeRange, filters, page]);

  if (!data) {
    return <GenericWidgetLoadingSpinner height={500} />
  }

  if (data.logs.length === 0) {
    return <div className="mb-0 alert alert-info">No DNS transactions were recorded during selected time range.</div>
  }

  return (
      <React.Fragment>
        <p className="mb-2 mt-2">
          <strong>Total:</strong> {numeral(data.total).format("0,0")}
        </p>

        <table className="table table-sm table-hover table-striped mb-4">
          <thead>
          <tr>
            <th>Query</th>
            <th>Type</th>
            <th>Timestamp</th>
            <th>eTLD</th>
            <th>Client</th>
            <th>Server</th>
          </tr>
          </thead>
          <tbody>
          {data.logs.map((log, i) => {
            return <DNSTransactionLogTableRow log={log} key={i} />
          })}
          </tbody>
        </table>

        <Paginator itemCount={data.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}