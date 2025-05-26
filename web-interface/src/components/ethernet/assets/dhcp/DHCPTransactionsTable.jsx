import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../../App";
import AssetsService from "../../../../services/ethernet/AssetsService";
import GenericWidgetLoadingSpinner from "../../../widgets/GenericWidgetLoadingSpinner";
import Paginator from "../../../misc/Paginator";
import IPAddress from "../../shared/IPAddress";
import DHCPTransactionNotesCount from "./DHCPTransactionNotesCount";
import DHCPTransactionId from "./DHCPTransactionId";
import DHCPDuration from "./DHCPDuration";
import moment from "moment";
import EthernetMacAddress from "../../../shared/context/macs/EthernetMacAddress";
import DHCPTransactionSuccess from "./DHCPTransactionSuccess";
import numeral from "numeral";
import ApiRoutes from "../../../../util/ApiRoutes";

const assetsService = new AssetsService();

export default function DHCPTransactionsTable(props) {

  const timeRange = props.timeRange;
  const [data, setData] = useState(null);

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const perPage = 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setData(null);
    assetsService.findAllDHCPTransactions(timeRange, selectedTaps,  perPage, (page-1)*perPage, setData);
  }, [selectedTaps, timeRange, page]);

  if (!data) {
    return <GenericWidgetLoadingSpinner height={150} />
  }

  if (data.transactions.length === 0) {
    return <div className="mb-0 alert alert-info">No DHCP transactions were observed during selected time range.</div>
  }

  return (
      <React.Fragment>
        <p className="mb-2 mt-0">
          <strong>Total:</strong> {numeral(data.total).format("0,0")}
        </p>

        <table className="table table-sm table-hover table-striped mb-4 mt-3">
          <thead>
          <tr>
            <th style={{width: 100}} title="Transaction ID">ID</th>
            <th style={{width: 200}}>Initiated At</th>
            <th>Type</th>
            <th>Client MAC</th>
            <th>Server MAC</th>
            <th>Requested IP</th>
            <th>Success</th>
            <th>Complete</th>
            <th>Duration</th>
            <th>Notes</th>
          </tr>
          </thead>
          <tbody>
          {data.transactions.map((t, i) => {
            return (
                <tr key={i}>
                  <td>
                    <a href={ApiRoutes.ETHERNET.ASSETS.DHCP.TRANSACTION_DETAILS(t.transaction_id) + "?transaction_time=" + encodeURIComponent(t.first_packet)}>
                      <DHCPTransactionId id={t.transaction_id} />
                    </a>
                  </td>
                  <td>{moment(t.first_packet).format()}</td>
                  <td>{t.transaction_type}</td>
                  <td><EthernetMacAddress addressWithContext={t.client_mac} /></td>
                  <td>{t.server_mac ? <EthernetMacAddress addressWithContext={t.server_mac} /> : <span className="text-muted">n/a</span>}</td>
                  <td><IPAddress ip={t.requested_ip_address} /></td>
                  <td><DHCPTransactionSuccess success={t.is_successful} /></td>
                  <td>{t.is_complete ? <span className="text-success">Complete</span>
                      : <span className="text-warning">Incomplete</span>}</td>
                  <td><DHCPDuration duration={t.duration_ms} /></td>
                  <td><DHCPTransactionNotesCount notes={t.notes} /></td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={data.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  );

}