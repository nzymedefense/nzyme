import React, {useContext, useState, useEffect} from "react";
import {useParams} from "react-router-dom";
import {TapContext} from "../../../App";
import {Presets} from "../../shared/timerange/TimeRange";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import GnssService from "../../../services/GnssService";
import ApiRoutes from "../../../util/ApiRoutes";
import GNSSPRNSNRHistogram from "./GNSSPRNSNRHistogram";
import GNSSPRNDegreesHistogram from "./GNSSPRNDegreesHistogram";
import GNSSPRNSkyChart from "./GNSSPRNSkyChart";
import GNSSPRN from "../GNSSPRN";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";

const gnssService = new GnssService();

export default function GNSSPRNDetailsPage(props) {

  const {constellation} = useParams();
  const {prn} = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [revision, setRevision] = useState(new Date());

  const [snrHistogram, setSnrHistogram] = useState(null);
  const [elevationHistogram, setElevationHistogram] = useState(null);
  const [azimuthHistogram, setAzimuthHistogram] = useState(null);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setSnrHistogram(null);
    setElevationHistogram(null);
    setAzimuthHistogram(null);

    gnssService.getPrnSnrHistogram(constellation, prn, timeRange, selectedTaps, setSnrHistogram);
    gnssService.getPrnElevationHistogram(constellation, prn, timeRange, selectedTaps, setElevationHistogram);
    gnssService.getPrnAzimuthHistogram(constellation, prn, timeRange, selectedTaps, setAzimuthHistogram);

  }, [constellation, prn, selectedTaps, timeRange, revision]);

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-10">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item"><a href={ApiRoutes.GNSS.CONSTELLATIONS}>GNSS</a></li>
              <li className="breadcrumb-item">{constellation} PRN <span className="machine-data">{prn}</span></li>
              <li className="breadcrumb-item active" aria-current="page">Details</li>
            </ol>
          </nav>
        </div>

        <div className="col-2">
          <a href={ApiRoutes.GNSS.CONSTELLATIONS} className="btn btn-secondary float-end">Back</a>
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
              <CardTitleWithControls title="Signal-to-Noise Ratio (SNR)"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSPRNSNRHistogram histogram={snrHistogram} setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Sky Chart"
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange}
                                     refreshAction={() => setRevision(new Date())} />

              <GNSSPRNSkyChart elevationHistogram={elevationHistogram}
                               azimuthHistogram={azimuthHistogram}
                               snrHistogram={snrHistogram} />
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