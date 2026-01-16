import React, {useContext, useEffect, useState} from 'react';
import {TapContext} from "../../../../App";
import {disableTapSelector, enableTapSelector} from "../../../misc/TapSelector";
import SectionMenuBar from "../../../shared/SectionMenuBar";
import ApiRoutes from "../../../../util/ApiRoutes";
import {TIME_MENU_ITEMS} from "../TimeMenuItems";
import {Presets} from "../../../shared/timerange/TimeRange";
import {useLocation} from "react-router-dom";
import TimeService from "../../../../services/ethernet/TimeService";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";
import {queryParametersToFilters} from "../../../shared/filtering/FilterQueryParameters";
import {NTP_FILTER_FIELDS} from "./NTPFilterFields";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";
import NTPTransactionsTable from "./NTPTransactionsTable";
import Filters from "../../../shared/filtering/Filters";
import NTPTransactionsHistogram from "./NTPTransactionsHistogram";
import NTPClientRequestResponseRatioHistogram from "./NTPClientRequestResponseRatioHistogram";
import NTPTopServersHistogram from "./NTPTopServersHistogram";

const timeService = new TimeService();

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

export function NTPOverviewPage() {

  const [organizationId, tenantId] = useSelectedTenant();

  const urlQuery = useQuery();
  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  const [transactions, setTransactions] = useState(null);
  const [transactionsHistogram, setTransactionsHistogram] = useState(null);

  const [orderColumn, setOrderColumn] = useState("initiated_at");
  const [orderDirection, setOrderDirection] = useState("DESC");

  const perPage = 25;
  const [page, setPage] = useState(1);

  const [revision, setRevision] = useState(new Date());

  const [filters, setFilters] = useState(
    queryParametersToFilters(urlQuery.get("filters"), NTP_FILTER_FIELDS)
  );

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setTransactions(null);
    setTransactionsHistogram(null);

    timeService.findAllNTPTransactions(
      organizationId,
      tenantId,
      timeRange,
      filters,
      orderColumn,
      orderDirection,
      selectedTaps,
      perPage,
      (page - 1) * perPage,
      setTransactions
    );

    timeService.getNTPTransactionsHistogram(timeRange, filters, selectedTaps, setTransactionsHistogram);
  }, [selectedTaps, organizationId, tenantId, timeRange, orderColumn, orderDirection, filters, page, revision]);

  return (
    <>
      <div className="row">
        <div className="col-md-12">
          <SectionMenuBar items={TIME_MENU_ITEMS} activeRoute={ApiRoutes.ETHERNET.TIME.NTP.INDEX}/>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Filters"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange} />

              <Filters filters={filters}
                       setFilters={setFilters}
                       fields={NTP_FILTER_FIELDS} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Transactions"
                                     fixedTimeRange={timeRange}
                                     refreshAction={() => setRevision(new Date())}/>

              <NTPTransactionsHistogram histogram={transactionsHistogram} setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Top Servers"
                                     fixedTimeRange={timeRange}
                                     refreshAction={() => setRevision(new Date())}/>

              <NTPTopServersHistogram filters={filters}
                                      setFilters={setFilters}
                                      timeRange={timeRange}
                                      revision={revision} />
            </div>
          </div>
        </div>
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Client Request/Response Ratio"
                                     fixedTimeRange={timeRange}
                                     refreshAction={() => setRevision(new Date())}/>

              <NTPClientRequestResponseRatioHistogram filters={filters}
                                                      setFilters={setFilters}
                                                      timeRange={timeRange}
                                                      revision={revision} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Transactions"
                                     fixedTimeRange={timeRange}
                                     refreshAction={() => setRevision(new Date())}/>

              <NTPTransactionsTable transactions={transactions}
                                    setFilters={setFilters}
                                    orderColumn={orderColumn}
                                    setOrderColumn={setOrderColumn}
                                    orderDirection={orderDirection}
                                    setOrderDirection={setOrderDirection}
                                    perPage={perPage}
                                    page={page}
                                    setPage={setPage} />
            </div>
          </div>
        </div>
      </div>
    </>
  )

}