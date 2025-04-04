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

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_MINUTES_15);
  const [revision, setRevision] = useState(new Date());

  const [bssidTimeRange, setBssidTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [bssidChartRevision, setBssidChartRevision] = useState(new Date());

  const [ssidTimeRange, setSsidTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [ssidChartRevision, setSsidChartRevision] = useState(new Date());

  const [filters, setFilters] = useState(queryParametersToFilters(urlQuery.get("filters"), BSSID_FILTER_FIELDS));

  const PER_PAGE = 75;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setBSSIDs(null);
    dot11Service.findAllBSSIDs(timeRange, filters, PER_PAGE, (page-1)*PER_PAGE, selectedTaps, setBSSIDs);
  }, [selectedTaps, filters, page, timeRange, revision])

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
                        setPage={setPage}
                        perPage={PER_PAGE} />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Active BSSIDs" slim={true}
                                       refreshAction={() => setBssidChartRevision(new Date())}
                                       timeRange={bssidTimeRange}
                                       setTimeRange={setBssidTimeRange} />

                <BSSIDAndSSIDChart parameter="bssid_count" timeRange={bssidTimeRange} revision={bssidChartRevision} />
              </div>
            </div>
          </div>
          <div className="col-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Active SSIDs" slim={true}
                                       refreshAction={() => setSsidChartRevision(new Date())}
                                       timeRange={ssidTimeRange} setTimeRange={setSsidTimeRange} />

                <BSSIDAndSSIDChart parameter="ssid_count" timeRange={ssidTimeRange} revision={ssidChartRevision}/>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
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