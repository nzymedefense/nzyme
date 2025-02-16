import React, {useContext, useEffect, useState} from "react";
import UavService from "../../services/UavService";
import {Presets} from "../shared/timerange/TimeRange";
import {TapContext} from "../../App";
import {disableTapSelector, enableTapSelector} from "../misc/TapSelector";
import AlphaFeatureAlert from "../shared/AlphaFeatureAlert";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import UavsTable from "./UavsTable";

const uavService = new UavService();

export default function UavsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [uavs, setUavs] = useState(null);
  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [page, setPage] = useState(1);

  const perPage = 25;

  useEffect(() => {
    uavService.findAllUav(setUavs, timeRange, selectedTaps, perPage, (page-1)*perPage)
  }, [selectedTaps, timeRange, page]);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  return (
      <React.Fragment>
        <AlphaFeatureAlert />

        <div className="row">
          <div className="col-md-12">
            <h1>Unmanned Aerial Vehicles (UAVs)</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="UAVs"
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange} />

                <UavsTable uavs={uavs}
                                       page={page}
                                       perPage={perPage}
                                       setPage={setPage} />
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
  )

}