import React, {useContext, useEffect, useState} from "react";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import BSSIDsTable from "./BSSIDsTable";
import BSSIDAndSSIDChart from "./BSSIDAndSSIDChart";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";

const dot11Service = new Dot11Service();

function BSSIDsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [bssids, setBSSIDs] = useState(null);

  const [bssidTimeRange, setBssidTimeRange] = useState(Presets.RELATIVE_MINUTES_15);

  const [bssidChartTimeRange, setBssidChartTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [ssidChartTimeRange, setSsidChartTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  useEffect(() => {
    setBSSIDs(null);
    dot11Service.findAllBSSIDs(bssidTimeRange, selectedTaps, setBSSIDs);
  }, [selectedTaps, bssidTimeRange])

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

    return <BSSIDsTable bssids={bssids} timeRange={bssidTimeRange} />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Access Points</h1>
          </div>

          <div className="row mt-3">
            <div className="col-md-12">
              <div className="row">
                <div className="col-md-6">
                  <div className="card">
                    <div className="card-body">
                      <CardTitleWithControls title="Active BSSIDs" slim={true}
                                             timeRange={bssidChartTimeRange}
                                             setTimeRange={setBssidChartTimeRange} />

                      <BSSIDAndSSIDChart parameter="bssid_count" timeRange={bssidChartTimeRange} />
                    </div>
                  </div>
                </div>
                <div className="col-md-6">
                  <div className="card">
                    <div className="card-body">
                      <CardTitleWithControls title="Active SSIDs" slim={true}
                                             timeRange={ssidChartTimeRange}
                                             setTimeRange={setSsidChartTimeRange} />

                      <BSSIDAndSSIDChart parameter="ssid_count" timeRange={ssidChartTimeRange} />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="Access Points / BSSIDs"
                                         timeRange={bssidTimeRange}
                                         setTimeRange={setBssidTimeRange} />

                  <p className="text-muted">
                    List of all access points advertised by recorded beacon or probe response frames. Click on a BSSID
                    to open a list of all advertised SSIDs and their respective channels.
                  </p>

                  {table()}
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default BSSIDsPage;