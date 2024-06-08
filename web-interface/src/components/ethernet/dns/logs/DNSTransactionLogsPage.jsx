import React, {useContext, useEffect, useState} from 'react';
import ApiRoutes from "../../../../util/ApiRoutes";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";
import {Presets} from "../../../shared/timerange/TimeRange";
import DNSTransactionsTable from "./DNSTransactionsTable";
import {TapContext} from "../../../../App";
import {disableTapSelector, enableTapSelector} from "../../../misc/TapSelector";
import Filters, {FILTER_TYPE} from "../../../shared/filtering/Filters";
import DNSTransactionCountChart from "./widgets/DNSTransactionCountChart";

export default function DNSTransactionLogsPage() {

  const tapContext = useContext(TapContext);

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [filters, setFilters] = useState([]);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.DNS.INDEX}>DNS</a></li>
                <li className="breadcrumb-item active" aria-current="page">Transaction Log</li>
              </ol>
            </nav>
          </div>

          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="DNS Transaction Logs"
                                       hideTimeRange={true}
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange}
                                       slim={true}/>

                <Filters filters={filters} setFilters={setFilters} fields={{
                  query_value: {title: "Query Value", type: FILTER_TYPE.STRING},
                  query_type: { title: "Query Type", type: FILTER_TYPE.STRING},
                  query_etld: { title: "Query eTLD", type: FILTER_TYPE.STRING},
                  response_value: { title: "Response Value", type: FILTER_TYPE.STRING},
                  response_type: { title: "Response Type", type: FILTER_TYPE.STRING},
                  response_etld: { title: "Response eTLD", type: FILTER_TYPE.STRING},
                  client_address: { title: "Client Address", type: FILTER_TYPE.IP_ADDRESS},
                  server_address: { title: "Server Address", type: FILTER_TYPE.IP_ADDRESS},
                  server_port: {title: "Server Port", type: FILTER_TYPE.NUMERIC}
                }} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Transaction Count"
                                       fixedAppliedTimeRange={timeRange}/>

                <DNSTransactionCountChart timeRange={timeRange} filters={filters} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Transactions"
                                       fixedAppliedTimeRange={timeRange}/>

                <DNSTransactionsTable timeRange={timeRange} filters={filters} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
)

}