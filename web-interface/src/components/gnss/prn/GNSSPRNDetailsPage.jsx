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
import GNSSPRNMultipathIndexHistogram from "./GNSSPRNMultipathIndexHistogram";
import usePageTitle from "../../../util/UsePageTitle";
import TapService from "../../../services/TapsService";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../../misc/LoadingSpinner";

const gnssService = new GnssService();
const tapService = new TapService();

export default function GNSSPRNDetailsPage(props) {

  const {constellation} = useParams();
  const {prn} = useParams();
  const {tapId} = useParams();

  const [organizationId, tenantId] = useSelectedTenant();
  const tapContext = useContext(TapContext);

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [revision, setRevision] = useState(new Date());

  const [tap, setTap] = useState(null);

  const [snoHistogram, setSnoHistogram] = useState(null);
  const [elevationHistogram, setElevationHistogram] = useState(null);
  const [azimuthHistogram, setAzimuthHistogram] = useState(null);
  const [dopplerHistogram, setDopplerHistogram] = useState(null);
  const [multipathHistogram, setMultipathHistogram] = useState(null);
  const [elevationMask, setElevationMask] = useState(null);

  usePageTitle((prn && constellation && tap) ? `GNSS ${constellation} PRN: ${prn} (at Tap ${tap.name})` : "GNSS PRN Details");

  useEffect(() => {
    disableTapSelector(tapContext);
  }, [tapContext]);

  useEffect(() => {
    setSnoHistogram(null);
    setElevationHistogram(null);
    setAzimuthHistogram(null);
    setDopplerHistogram(null);
    setMultipathHistogram(null);
    setElevationMask(null);

    gnssService.getPrnSnoHistogram(constellation, prn, timeRange, tapId, setSnoHistogram);
    gnssService.getPrnElevationHistogram(constellation, prn, timeRange, tapId, setElevationHistogram);
    gnssService.getPrnAzimuthHistogram(constellation, prn, timeRange, tapId, setAzimuthHistogram);
    gnssService.getPrnDopplerHistogram(constellation, prn, timeRange, tapId, setDopplerHistogram);
    gnssService.getPrnMultipathHistogram(constellation, prn, timeRange, tapId, setMultipathHistogram);
    gnssService.getElevationMask(tapId, setElevationMask);
  }, [constellation, prn, tapId, timeRange, revision]);

  useEffect(() => {
    setTap(null);

    tapService.findTapHighLevel(tapId, organizationId, tenantId, setTap);
  }, [tapId, organizationId, tenantId])

  if (!tap) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-10">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item">GNSS</li>
              <li className="breadcrumb-item">Taps</li>
              <li className="breadcrumb-item">{tap.name}</li>
              <li className="breadcrumb-item"><a href={ApiRoutes.GNSS.TAP_DETAILS.SATELLITES(tapId)}>Satellites</a></li>
              <li className="breadcrumb-item active">{constellation} PRN <span className="machine-data">{prn}</span></li>
            </ol>
          </nav>
        </div>

        <div className="col-2">
          <a href={ApiRoutes.GNSS.TAP_DETAILS.SATELLITES(tapId)} className="btn btn-secondary float-end">Back</a>
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

              <GNSSPRNMultipathIndexHistogram histogram={multipathHistogram} />
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