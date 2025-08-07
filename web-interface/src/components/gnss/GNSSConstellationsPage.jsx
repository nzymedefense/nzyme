import React, {useContext, useEffect, useState} from 'react';
import {TapContext} from "../../App";
import {Presets} from "../shared/timerange/TimeRange";
import {disableTapSelector, enableTapSelector} from "../misc/TapSelector";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import GNSSTimeDeviationHistogram from "./GNSSTimeDeviationHistogram";
import GnssService from "../../services/GnssService";
import GNSSPdopHistogram from "./GNSSPdopHistogram";
import GNSSFixSatellitesHistogram from "./GNSSFixSatellitesHistogram";
import GNSSSatellitesInViewHistogram from "./GNSSSatellitesInViewHistogram";
import GNSSAltitudeHistogram from "./GNSSAltitudeHistogram";
import GNSSCoordinatesHeatmap from "./GNSSCoordinatesHeatmap";

const gnssService = new GnssService();

export default function GNSSConstellationsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  const [pdopHistogram, setPdopHistogram] = useState(null);
  const [timeDeviationHistogram, setTimeDeviationHistogram] = useState(null);
  const [fixSatellitesHistogram, setFixSatellitesHistogram] = useState(null);
  const [satellitesInViewHistogram, setSatellitesInViewHistogram] = useState(null);
  const [altitudeHistogram, setAltitudeHistogram] = useState(null);

  const constellationCoordinatesTimeRange = Presets.RELATIVE_HOURS_24;
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
    setTimeDeviationHistogram(null);
    setFixSatellitesHistogram(null);
    setSatellitesInViewHistogram(null);
    setAltitudeHistogram(null);
    setConstellationCoordinates(null);

    gnssService.getPdopHistogram(timeRange, selectedTaps, setPdopHistogram);
    gnssService.getTimeDeviationHistogram(timeRange, selectedTaps, setTimeDeviationHistogram);
    gnssService.getFixSatellitesHistogram(timeRange, selectedTaps, setFixSatellitesHistogram);
    gnssService.getSatellitesInViewHistogram(timeRange, selectedTaps, setSatellitesInViewHistogram);
    gnssService.getAltitudeHistogram(timeRange, selectedTaps, setAltitudeHistogram);

    gnssService.getConstellationCoordinates(
      "GPS", constellationCoordinatesTimeRange, selectedTaps, setConstellationCoordinates
    );
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
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Satellites In View"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSSatellitesInViewHistogram histogram={satellitesInViewHistogram} setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>

        <div className="col-md-6">
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
        <div className="col-md-6">
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

        <div className="col-md-6">
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

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Reported Locations"
                                     fixedAppliedTimeRange={constellationCoordinatesTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSCoordinatesHeatmap containerHeight={400} coordinates={constellationCoordinates} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}