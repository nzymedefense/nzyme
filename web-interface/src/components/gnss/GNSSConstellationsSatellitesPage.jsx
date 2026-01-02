import React, {useContext, useEffect, useState} from 'react';
import {TapContext} from "../../App";
import {Presets} from "../shared/timerange/TimeRange";
import {disableTapSelector, enableTapSelector} from "../misc/TapSelector";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import GnssService from "../../services/GnssService";
import GNSSSatellitesInViewTable from "./GNSSSatellitesInViewTable";
import GNSSSkyPlot from "./GNSSSkyPlot";
import SectionMenuBar from "../shared/SectionMenuBar";
import ApiRoutes from "../../util/ApiRoutes";
import {GNSS_MENU_ITEMS} from "./GNSSMenuItems";

const gnssService = new GnssService();

export default function GNSSConstellationsSatellitesPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const timeRange = Presets.RELATIVE_MINUTES_1;

  const [elevationMask, setElevationMask] = useState(null);
  const [satellitesInView, setSatellitesInView] = useState(null);

  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setElevationMask(null);
    setSatellitesInView(null);

    gnssService.getElevationMask(selectedTaps, setElevationMask);
    gnssService.findAllSatellitesInView(timeRange, selectedTaps, setSatellitesInView);
  }, [timeRange, selectedTaps, revision])

  return (
    <React.Fragment>

      <div className="row">
        <div className="col-md-12">
          <SectionMenuBar items={GNSS_MENU_ITEMS}
                          activeRoute={ApiRoutes.GNSS.CONSTELLATIONS.SATELLITES} />
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Sky Plot"
                                     fixedTimeRange={timeRange}
                                     refreshAction={() => setRevision(new Date())} />
              <div className="d-flex justify-content-center">
                <GNSSSkyPlot satellites={satellitesInView} elevationMask={elevationMask} height={800}/>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Satellites In View"
                                     fixedTimeRange={timeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSSatellitesInViewTable satellites={satellitesInView} />
            </div>
          </div>
        </div>
      </div>

    </React.Fragment>
  )

}