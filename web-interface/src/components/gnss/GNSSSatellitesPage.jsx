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
import usePageTitle from "../../util/UsePageTitle";
import {useParams} from "react-router-dom";
import TapService from "../../services/TapsService";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../misc/LoadingSpinner";
import Routes from "../../util/ApiRoutes";

const gnssService = new GnssService();
const tapService = new TapService();

export default function GNSSSatellitesPage() {

  const {tapId} = useParams();

  const [organizationId, tenantId] = useSelectedTenant();
  const tapContext = useContext(TapContext);

  const timeRange = Presets.RELATIVE_MINUTES_1;

  const [tap, setTap] = useState(null);

  const [elevationMask, setElevationMask] = useState(null);
  const [satellitesInView, setSatellitesInView] = useState(null);

  const [revision, setRevision] = useState(new Date());

  usePageTitle(tap ? "GNSS Satellites at Tap \"" + tap.name + "\"" : "GNSS Satellites");

  useEffect(() => {
    disableTapSelector(tapContext);
  }, [tapContext]);

  useEffect(() => {
    setElevationMask(null);
    setSatellitesInView(null);

    gnssService.getElevationMask(tapId, setElevationMask);
    gnssService.findAllSatellitesInView(timeRange, tapId, setSatellitesInView);
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
        <div className="col-10">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item">
                <a href={Routes.GNSS.OVERVIEW}>GNSS</a>
              </li>
              <li className="breadcrumb-item">Taps</li>
              <li className="breadcrumb-item">{tap.name}</li>
              <li className="breadcrumb-item active" aria-current="page">Satellites</li>
            </ol>
          </nav>
        </div>
        <div className="col-md-2">
          <span className="float-end">
            <a href="https://go.nzyme.org/gnss-sats" className="btn btn-outline-secondary me-1" target="_blank">Help</a>
            <a href={ApiRoutes.GNSS.OVERVIEW} className="btn btn-secondary">Back</a>
          </span>
        </div>
      </div>

      <div className="row">
        <div className="col-md-12">
          <SectionMenuBar items={GNSS_MENU_ITEMS(tapId)}
                          activeRoute={ApiRoutes.GNSS.TAP_DETAILS.SATELLITES(tapId)} />
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Sky Plot"
                                     helpLink="https://go.nzyme.org/gnss-sky-plot"
                                     fixedTimeRange={timeRange}
                                     refreshAction={() => setRevision(new Date())} />
              <div className="d-flex justify-content-center">
                <GNSSSkyPlot satellites={satellitesInView} elevationMask={elevationMask} tapId={tapId} height={800} />
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

              <GNSSSatellitesInViewTable satellites={satellitesInView} tapId={tapId} />
            </div>
          </div>
        </div>
      </div>

    </React.Fragment>
  )

}