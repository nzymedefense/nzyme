import React, {useContext, useEffect, useState} from 'react';
import {TapContext} from "../../App";
import {disableTapSelector, enableTapSelector} from "../misc/TapSelector";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import GnssService from "../../services/GnssService";
import GNSSPdopHistogram from "./GNSSPdopHistogram";
import GNSSFixSatellitesHistogram from "./GNSSFixSatellitesHistogram";
import GNSSAltitudeHistogram from "./GNSSAltitudeHistogram";
import GNSSCoordinatesHeatmap from "./GNSSCoordinatesHeatmap";
import GNSSFixStatusHistogram from "./GNSSFixStatusHistogram";
import GNSSDistancesTable from "./GNSSDistancesTable";
import SectionMenuBar from "../shared/SectionMenuBar";
import ApiRoutes from "../../util/ApiRoutes";
import {GNSS_MENU_ITEMS} from "./GNSSMenuItems";
import {Presets} from "../shared/timerange/TimeRange";

const gnssService = new GnssService();

export default function GNSSConstellationsFixPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  const [pdopHistogram, setPdopHistogram] = useState(null);
  const [fixSatellitesHistogram, setFixSatellitesHistogram] = useState(null);
  const [fixStatusHistogram, setFixStatusHistogram] = useState(null);
  const [altitudeHistogram, setAltitudeHistogram] = useState(null);
  const [distances, setDistances] = useState(null);

  const [constellationCoordinatesConstellation, setConstellationCoordinatesConstellation] = useState("GPS");
  const [constellationCoordinatesTimeRange, setConstellationCoordinatesTimeRange] = useState(Presets.RELATIVE_MINUTES_15);
  const [constellationCoordinates, setConstellationCoordinates] = useState(null);

  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setPdopHistogram(null);
    setFixSatellitesHistogram(null);
    setFixStatusHistogram(null);
    setAltitudeHistogram(null);
    setConstellationCoordinates(null);

    gnssService.getPdopHistogram(timeRange, selectedTaps, setPdopHistogram);
    gnssService.getFixSatellitesHistogram(timeRange, selectedTaps, setFixSatellitesHistogram);
    gnssService.getFixStatusHistogram(timeRange, selectedTaps, setFixStatusHistogram);
    gnssService.getAltitudeHistogram(timeRange, selectedTaps, setAltitudeHistogram);

  }, [timeRange, selectedTaps, revision])

  useEffect(() => {
    setConstellationCoordinates(null);
    setDistances(null);

    gnssService.getConstellationCoordinates(
      constellationCoordinatesConstellation,
      constellationCoordinatesTimeRange,
      selectedTaps,
      setConstellationCoordinates
    );

    gnssService.getDistances(constellationCoordinatesTimeRange, selectedTaps, setDistances);
  }, [constellationCoordinatesConstellation, constellationCoordinatesTimeRange, timeRange, selectedTaps, revision])

  return (
    <React.Fragment>

      <div className="row">
        <div className="col-md-12">
          <SectionMenuBar items={GNSS_MENU_ITEMS}
                          activeRoute={ApiRoutes.GNSS.CONSTELLATIONS.FIX} />
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-7">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Reported Locations"
                                     timeRange={constellationCoordinatesTimeRange}
                                     setTimeRange={setConstellationCoordinatesTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <select className="form-select form-select-sm mb-3" style={{width: 250}}
                      onChange={(e) => { setConstellationCoordinatesConstellation(e.target.value) }}
                      value={constellationCoordinatesConstellation}>
                <option value="GPS">GPS</option>
                <option value="GLONASS">GLONASS</option>
                <option value="BeiDou">BeiDou</option>
                <option value="Galileo">Galileo</option>
              </select>

              <GNSSCoordinatesHeatmap containerHeight={400} coordinates={constellationCoordinates} />
            </div>
          </div>
        </div>
        <div className="col-md-5">
          <div className="row">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="Maximum Distances From Tap"
                                         timeRange={constellationCoordinatesTimeRange}
                                         setTimeRange={setConstellationCoordinatesTimeRange}
                                         refreshAction={() => setRevision(new Date())} />

                  <GNSSDistancesTable distances={distances}/>
                </div>
              </div>
            </div>
          </div>
          <div className="row mt-3">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="Fix Status"
                                         timeRange={timeRange}
                                         setTimeRange={setTimeRange}
                                         refreshAction={() => setRevision(new Date())} />


                  <GNSSFixStatusHistogram fixStatusHistogram={fixStatusHistogram} setTimeRange={setTimeRange} />
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
              <CardTitleWithControls title="Satellites Used For Fix"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSFixSatellitesHistogram histogram={fixSatellitesHistogram} setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Dilution of Precision"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSPdopHistogram histogram={pdopHistogram} setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Altitude"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSAltitudeHistogram histogram={altitudeHistogram} setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}