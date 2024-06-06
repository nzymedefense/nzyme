import React, {useContext, useEffect, useState} from 'react';
import ApiRoutes from "../../../../util/ApiRoutes";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";
import {Presets} from "../../../shared/timerange/TimeRange";
import DNSTransactionsTable from "./DNSTransactionsTable";
import {TapContext} from "../../../../App";
import {disableTapSelector, enableTapSelector} from "../../../misc/TapSelector";
import Filters from "../../../shared/filtering/Filters";

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
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange}
                                       slim={true} />

              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Transactions"
                                       fixedAppliedTimeRange={timeRange} />

                <Filters filters={filters} setFilters={setFilters} />

                <DNSTransactionsTable timeRange={timeRange} filters={filters} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}