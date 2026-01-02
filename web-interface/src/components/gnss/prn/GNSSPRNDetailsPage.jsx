import React, {useContext, useState, useEffect} from "react";
import {useParams} from "react-router-dom";
import {TapContext} from "../../../App";
import {Presets} from "../../shared/timerange/TimeRange";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import GnssService from "../../../services/GnssService";
import ApiRoutes from "../../../util/ApiRoutes";
import GNSSPRNSnoHistogram from "./GNSSPRNSnoHistogram";
import GNSSPRNDegreesHistogram from "./GNSSPRNDegreesHistogram";
import GNSSPRNSkyPlot from "./GNSSPRNSkyPlot";
import GNSSPRN from "../GNSSPRN";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import GNSSPRNDopplerHistogram from "./GNSSPRNDopplerHistogram";

const gnssService = new GnssService();

export default function GNSSPRNDetailsPage(props) {

  const {constellation} = useParams();
  const {prn} = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [revision, setRevision] = useState(new Date());

  const [snoHistogram, setSnoHistogram] = useState(null);
  const [elevationHistogram, setElevationHistogram] = useState(null);
  const [azimuthHistogram, setAzimuthHistogram] = useState(null);
  const [dopplerHistogram, setDopplerHistogram] = useState(null);
  const [elevationMask, setElevationMask] = useState(null);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setSnoHistogram(null);
    setElevationHistogram(null);
    setAzimuthHistogram(null);
    setDopplerHistogram(null);
    setElevationMask(null);

    gnssService.getPrnSnoHistogram(constellation, prn, timeRange, selectedTaps, setSnoHistogram);
    gnssService.getPrnElevationHistogram(constellation, prn, timeRange, selectedTaps, setElevationHistogram);
    gnssService.getPrnAzimuthHistogram(constellation, prn, timeRange, selectedTaps, setAzimuthHistogram);
    gnssService.getPrnDopplerHistogram(constellation, prn, timeRange, selectedTaps, setDopplerHistogram);
    gnssService.getElevationMask(selectedTaps, setElevationMask);
  }, [constellation, prn, selectedTaps, timeRange, revision]);

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-10">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item">GNSS</li>
              <li className="breadcrumb-item"><a href={ApiRoutes.GNSS.CONSTELLATIONS.SATELLITES}>Satellites</a></li>
              <li className="breadcrumb-item">{constellation} PRN <span className="machine-data">{prn}</span></li>
              <li className="breadcrumb-item active" aria-current="page">Details</li>
            </ol>
          </nav>
        </div>

        <div className="col-2">
          <a href={ApiRoutes.GNSS.CONSTELLATIONS.SATELLITES} className="btn btn-secondary float-end">Back</a>
        </div>
      </div>

      <div className="row mt-2">
        <div className="col-12">
          <h1>{constellation} PRN <GNSSPRN constellation={constellation} prn={prn}/></h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Carrier-to-Noise Density Ratio (C/N&#8320;)"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSPRNSnoHistogram histogram={snoHistogram} setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Mulipath Index"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

            </div>
          </div>
        </div>
        <div className="col-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Doppler Shift"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSPRNDopplerHistogram histogram={dopplerHistogram} setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Sky Plot"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <div className="d-flex justify-content-center">
                <GNSSPRNSkyPlot elevationHistogram={elevationHistogram}
                                azimuthHistogram={azimuthHistogram}
                                snoHistogram={snoHistogram}
                                elevationMask={elevationMask} />
              </div>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="row">
            <div className="col-12">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="Elevation &deg;"
                                         timeRange={timeRange}
                                         setTimeRange={setTimeRange}
                                         refreshAction={() => setRevision(new Date())} />

                  <GNSSPRNDegreesHistogram histogram={elevationHistogram} setTimeRange={setTimeRange} />
                </div>
              </div>
            </div>
          </div>

          <div className="row">
            <div className="col-12">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="Azimuth &deg;"
                                         timeRange={timeRange}
                                         setTimeRange={setTimeRange}
                                         refreshAction={() => setRevision(new Date())} />

                  <GNSSPRNDegreesHistogram histogram={azimuthHistogram} setTimeRange={setTimeRange} />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}