import React, {useContext, useEffect, useState} from "react";
import AlphaFeatureAlert from "../../shared/AlphaFeatureAlert";
import L4Service from "../../../services/ethernet/L4Service";
import {TapContext} from "../../../App";
import {Presets} from "../../shared/timerange/TimeRange";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import Filters from "../../shared/filtering/Filters";
import {queryParametersToFilters} from "../../shared/filtering/FilterQueryParameters";
import {useLocation} from "react-router-dom";
import L4SessionsTable from "./L4SessionsTable";
import {L4_SESSIONS_FILTER_FIELDS} from "./L4SessionFilterFields";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";

const l4Service = new L4Service();

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

export default function L4OverviewPage() {

  const urlQuery = useQuery();
  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;
  const [organizationId, tenantId] = useSelectedTenant();

  const [sessions, setSessions] = useState(null);

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  const [orderColumn, setOrderColumn] = useState("most_recent_segment_time");
  const [orderDirection, setOrderDirection] = useState("DESC");

  const perPage = 25;
  const [page, setPage] = useState(1);

  const [revision, setRevision] = useState(new Date());

  const [filters, setFilters] = useState(
      queryParametersToFilters(urlQuery.get("filters"), L4_SESSIONS_FILTER_FIELDS)
  );

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setSessions(null);
    l4Service.findAllSessions(organizationId, tenantId, selectedTaps, filters, timeRange, orderColumn, orderDirection, perPage, (page-1)*perPage, setSessions)
  }, [organizationId, tenantId, selectedTaps, filters, timeRange, page, perPage, orderColumn, orderDirection, revision]);

  return (
      <React.Fragment>
        <AlphaFeatureAlert />

        <div className="row">
          <div className="col-md-12">
            <h1>TCP/UDP</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Session Filters"
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange} />

                <Filters filters={filters}
                         setFilters={setFilters}
                         fields={L4_SESSIONS_FILTER_FIELDS} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Sessions"
                                       refreshAction={() => setRevision(new Date())} />

                <L4SessionsTable sessions={sessions}
                                 page={page}
                                 setPage={setPage}
                                 perPage={perPage}
                                 orderColumn={orderColumn}
                                 orderDirection={orderDirection}
                                 setOrderColumn={setOrderColumn}
                                 setOrderDirection={setOrderDirection}
                                 setFilters={setFilters} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}