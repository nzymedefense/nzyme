import React, {useContext, useEffect, useState} from "react";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import BSSIDsTable from "./BSSIDsTable";
import BSSIDAndSSIDChart from "./BSSIDAndSSIDChart";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import Filters from "../../shared/filtering/Filters";
import {useLocation} from "react-router-dom";
import {BSSID_FILTER_FIELDS} from "./BssidFilterFields";
import {queryParametersToFilters} from "../../shared/filtering/FilterQueryParameters";

const dot11Service = new Dot11Service();

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

function BSSIDsPage() {

  const urlQuery = useQuery();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [bssids, setBSSIDs] = useState(null);

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [revision, setRevision] = useState(new Date());

  const [filters, setFilters] = useState(queryParametersToFilters(urlQuery.get("filters"), BSSID_FILTER_FIELDS));

  const [orderColumn, setOrderColumn] = useState("signal_strength_average");
  const [orderDirection, setOrderDirection] = useState("DESC");

  const PER_PAGE = 75;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setBSSIDs(null);
    dot11Service.findAllBSSIDs(timeRange, filters, orderColumn, orderDirection, PER_PAGE, (page-1)*PER_PAGE, selectedTaps, setBSSIDs);
  }, [selectedTaps, filters, page, timeRange, revision, orderColumn, orderDirection]);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  const table = () => {
    if (!bssids) {
      return <LoadingSpinner />
    }

    return <BSSIDsTable bssids={bssids}
                        timeRange={timeRange}
                        setFilters={setFilters}
                        page={page}
                        orderColumn={orderColumn}
                        orderDirection={orderDirection}
                        setOrderColumn={setOrderColumn}
                        setOrderDirection={setOrderDirection}
                        setPage={setPage}
                        perPage={PER_PAGE} />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Filters"
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange} />

                <Filters filters={filters} setFilters={setFilters} fields={BSSID_FILTER_FIELDS} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Active BSSIDs" slim={true}
                                       refreshAction={() => setRevision(new Date())}
                                       timeRange={timeRange}/>

                <BSSIDAndSSIDChart parameter="bssid_count"
                                   timeRange={timeRange}
                                   filters={filters}
                                   setTimeRange={setTimeRange}
                                   revision={revision} />
              </div>
            </div>
          </div>
          <div className="col-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Active SSIDs" slim={true}
                                       refreshAction={() => setRevision(new Date())}
                                       timeRange={timeRange} />

                <BSSIDAndSSIDChart parameter="ssid_count"
                                   timeRange={timeRange}
                                   filters={filters}
                                   setTimeRange={setTimeRange}
                                   revision={revision}/>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Access Points / BSSIDs"
                                       refreshAction={() => setRevision(new Date())}
                                       fixedAppliedTimeRange={timeRange} />

                <p className="text-muted">
                  List of all access points advertised by recorded beacon or probe response frames. Click on a BSSID
                  to open a list of all advertised SSIDs and their respective channels.
                </p>

                {table()}
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default BSSIDsPage;