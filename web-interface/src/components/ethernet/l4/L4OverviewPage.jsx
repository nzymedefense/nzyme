import React, {useState} from "react";
import {Presets} from "../../shared/timerange/TimeRange";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import Filters from "../../shared/filtering/Filters";
import {queryParametersToFilters} from "../../shared/filtering/FilterQueryParameters";
import {useLocation} from "react-router-dom";
import L4SessionsTable from "./L4SessionsTable";
import {L4_SESSIONS_FILTER_FIELDS} from "./L4SessionFilterFields";

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

export default function L4OverviewPage() {

  const urlQuery = useQuery();

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [revision, setRevision] = useState(new Date());

  const [filters, setFilters] = useState(
      queryParametersToFilters(urlQuery.get("filters"), L4_SESSIONS_FILTER_FIELDS)
  );

  return (
      <React.Fragment>
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

                <L4SessionsTable filters={filters}
                                 timeRange={timeRange}
                                 setFilters={setFilters}
                                 revision={revision} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}