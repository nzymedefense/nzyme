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
import usePageTitle from "../../util/UsePageTitle";
import {timeRangeFromURLOrDefault} from "../shared/timerange/TimeRangeSelector";
import {useParams} from "react-router-dom";
import TapService from "../../services/TapsService";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../misc/LoadingSpinner";
import Routes from "../../util/ApiRoutes";

const gnssService = new GnssService();
const tapService = new TapService();

export default function GNSSRFPage() {

  const {tapId} = useParams();

  const [organizationId, tenantId] = useSelectedTenant();
  const tapContext = useContext(TapContext);

  const [timeRange, setTimeRange] = useState(() => timeRangeFromURLOrDefault(Presets.RELATIVE_HOURS_24));

  const [tap, setTap] = useState(null);

  const [rfMonJammingIndicatorHistogram, setRfMonJammingIndicatorHistogram] = useState(null);
  const [rfMonAgcCountHistogram, setRfMonAgcCountHistogram] = useState(null);
  const [rfMonNoiseHistogram, setRfMonNoiseHistogram] = useState(null);

  const [revision, setRevision] = useState(new Date());

  usePageTitle(tap ? "GNSS RF at Tap \"" + tap.name + "\"" : "GNSS RF");

  useEffect(() => {
    disableTapSelector(tapContext);
  }, [tapContext]);

  useEffect(() => {
    setRfMonJammingIndicatorHistogram(null);
    setRfMonAgcCountHistogram(null);
    setRfMonNoiseHistogram(null);

    gnssService.getRfMonJammingIndicatorHistogram(timeRange, tapId, setRfMonJammingIndicatorHistogram)
    gnssService.getRfMonAgcCountHistogram(timeRange, tapId, setRfMonAgcCountHistogram)
    gnssService.getRfMonNoiseHistogram(timeRange, tapId, setRfMonNoiseHistogram)

  }, [timeRange, tapId, revision])

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
        <div className="col-12">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item">
                <a href={Routes.GNSS.OVERVIEW}>GNSS</a>
              </li>
              <li className="breadcrumb-item">Taps</li>
              <li className="breadcrumb-item">{tap.name}</li>
              <li className="breadcrumb-item active">RF</li>
            </ol>
          </nav>
        </div>
      </div>

      <div className="row">
        <div className="col-md-10">
          <SectionMenuBar items={GNSS_MENU_ITEMS(tapId)}
                          activeRoute={ApiRoutes.GNSS.TAP_DETAILS.RF(tapId)} />
        </div>
        <div className="col-md-2">
          <span className="float-end">
            <a href="https://go.nzyme.org/gnss-rf" className="btn btn-outline-secondary me-1" target="_blank">Help</a>
            <a href={ApiRoutes.GNSS.OVERVIEW} className="btn btn-secondary">Back</a>
          </span>
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