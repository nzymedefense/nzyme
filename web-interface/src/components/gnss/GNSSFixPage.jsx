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
import usePageTitle from "../../util/UsePageTitle";
import {timeRangeFromURLOrDefault} from "../shared/timerange/TimeRangeSelector";
import {useParams} from "react-router-dom";
import TapService from "../../services/TapsService";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../misc/LoadingSpinner";
import Routes from "../../util/ApiRoutes";

const gnssService = new GnssService();
const tapService = new TapService();

export default function GNSSFixPage() {

  const {tapId} = useParams();

  const [organizationId, tenantId] = useSelectedTenant();
  const tapContext = useContext(TapContext);

  const [timeRange, setTimeRange] = useState(() => timeRangeFromURLOrDefault(Presets.RELATIVE_HOURS_24, "primary"));

  const [tap, setTap] = useState(null);

  const [pdopHistogram, setPdopHistogram] = useState(null);
  const [fixSatellitesHistogram, setFixSatellitesHistogram] = useState(null);
  const [fixStatusHistogram, setFixStatusHistogram] = useState(null);
  const [altitudeHistogram, setAltitudeHistogram] = useState(null);
  const [distances, setDistances] = useState(null);

  const [constellationCoordinatesConstellation, setConstellationCoordinatesConstellation] = useState("GPS");
  const [constellationCoordinatesTimeRange, setConstellationCoordinatesTimeRange] =
    useState(() => timeRangeFromURLOrDefault(Presets.RELATIVE_MINUTES_15, "locations"));
  const [constellationCoordinates, setConstellationCoordinates] = useState(null);

  const [revision, setRevision] = useState(new Date());

  usePageTitle(tap ? "GNSS Fix at Tap \"" + tap.name + "\"" : "GNSS Fix");

  useEffect(() => {
    disableTapSelector(tapContext);
  }, [tapContext]);

  useEffect(() => {
    setPdopHistogram(null);
    setFixSatellitesHistogram(null);
    setFixStatusHistogram(null);
    setAltitudeHistogram(null);
    setConstellationCoordinates(null);

    gnssService.getPdopHistogram(timeRange, tapId, setPdopHistogram);
    gnssService.getFixSatellitesHistogram(timeRange, tapId, setFixSatellitesHistogram);
    gnssService.getFixStatusHistogram(timeRange, tapId, setFixStatusHistogram);
    gnssService.getAltitudeHistogram(timeRange, tapId, setAltitudeHistogram);
  }, [timeRange, tapId, revision])

  useEffect(() => {
    setTap(null);

    tapService.findTapHighLevel(tapId, organizationId, tenantId, setTap);
  }, [tapId, organizationId, tenantId])

  useEffect(() => {
    setConstellationCoordinates(null);
    setDistances(null);

    gnssService.getConstellationCoordinates(
      constellationCoordinatesConstellation,
      constellationCoordinatesTimeRange,
      tapId,
      setConstellationCoordinates
    );

    gnssService.getDistances(constellationCoordinatesTimeRange, tapId, setDistances);
  }, [constellationCoordinatesConstellation, constellationCoordinatesTimeRange, timeRange, tapId, revision])

  if (!tap) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>

      <div className="row">
        <div className="col-12">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item">
                <a href={Routes.GNSS.OVERVIEW}>GNSS</a>
              </li>
              <li className="breadcrumb-item">Taps</li>
              <li className="breadcrumb-item">{tap.name}</li>
              <li className="breadcrumb-item active">Fix</li>
            </ol>
          </nav>
        </div>
      </div>

      <div className="row">
        <div className="col-md-10">
          <SectionMenuBar items={GNSS_MENU_ITEMS(tapId)}
                          activeRoute={ApiRoutes.GNSS.TAP_DETAILS.FIX(tapId)} />
        </div>
        <div className="col-md-2">
          <span className="float-end">
            <a href="https://go.nzyme.org/gnss-fix" className="btn btn-outline-secondary me-1" target="_blank">Help</a>
            <a href={ApiRoutes.GNSS.OVERVIEW} className="btn btn-secondary">Back</a>
          </span>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-7">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Reported Locations"
                                     urlKey="locations"
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
                                         urlKey="locations"
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
                                         urlKey="primary"
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
                                     urlKey="primary"
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
                                     urlKey="primary"
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
                                     urlKey="primary"
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