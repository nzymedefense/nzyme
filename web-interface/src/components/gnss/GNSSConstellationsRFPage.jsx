import React, {useContext, useEffect, useState} from 'react';
import {TapContext} from "../../App";
import {Presets} from "../shared/timerange/TimeRange";
import {disableTapSelector, enableTapSelector} from "../misc/TapSelector";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import GnssService from "../../services/GnssService";
import GNSSRfMonJammingIndicatorHistogram from "./GNSSRfMonJammingIndicatorHistogram";
import GNSSRfMonAgcCountHistogram from "./GNSSRfMonAgcCountHistogram";
import GNSSRfMonNoiseHistogram from "./GNSSRfMonNoiseHistogram";
import SectionMenuBar from "../shared/SectionMenuBar";
import ApiRoutes from "../../util/ApiRoutes";
import {GNSS_MENU_ITEMS} from "./GNSSMenuItems";

const gnssService = new GnssService();

export default function GNSSConstellationsRFPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  const [rfMonJammingIndicatorHistogram, setRfMonJammingIndicatorHistogram] = useState(null);
  const [rfMonAgcCountHistogram, setRfMonAgcCountHistogram] = useState(null);
  const [rfMonNoiseHistogram, setRfMonNoiseHistogram] = useState(null);

  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setRfMonJammingIndicatorHistogram(null);
    setRfMonAgcCountHistogram(null);
    setRfMonNoiseHistogram(null);

    gnssService.getRfMonJammingIndicatorHistogram(timeRange, selectedTaps, setRfMonJammingIndicatorHistogram)
    gnssService.getRfMonAgcCountHistogram(timeRange, selectedTaps, setRfMonAgcCountHistogram)
    gnssService.getRfMonNoiseHistogram(timeRange, selectedTaps, setRfMonNoiseHistogram)

  }, [timeRange, selectedTaps, revision])

  return (
    <React.Fragment>

      <div className="row">
        <div className="col-md-12">
          <SectionMenuBar items={GNSS_MENU_ITEMS}
                          activeRoute={ApiRoutes.GNSS.CONSTELLATIONS.RF} />
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Noise"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />


              <GNSSRfMonNoiseHistogram histogram={rfMonNoiseHistogram} setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Jamming Indicator"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />


              <GNSSRfMonJammingIndicatorHistogram histogram={rfMonJammingIndicatorHistogram}
                                                  setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="AGC Adjustment Counts"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />


              <GNSSRfMonAgcCountHistogram histogram={rfMonAgcCountHistogram}
                                          setTimeRange={setTimeRange}/>
            </div>
          </div>
        </div>
      </div>

    </React.Fragment>
  )

}