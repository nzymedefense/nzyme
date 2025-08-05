import React, {useContext, useEffect, useState} from 'react';
import {TapContext} from "../../App";
import {Presets} from "../shared/timerange/TimeRange";
import {disableTapSelector, enableTapSelector} from "../misc/TapSelector";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import GNSSTimeDeviationHistogram from "./GNSSTimeDeviationHistogram";
import GnssService from "../../services/GnssService";

const gnssService = new GnssService();

export default function GNSSConstellationsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  const [timeDeviationHistogram, setTimeDeviationHistogram] = useState(null);

  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setTimeDeviationHistogram(null);
    gnssService.getTimeDeviationHistogram(timeRange, selectedTaps, setTimeDeviationHistogram);
  }, [timeRange, selectedTaps, revision])

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-12">
          <h1>GNSS Constellations</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Time Deviation"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSTimeDeviationHistogram histogram={timeDeviationHistogram} setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}