import React, {useContext, useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import {TapContext} from "../../../App";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import SectionMenuBar from "../../shared/SectionMenuBar";
import ApiRoutes from "../../../util/ApiRoutes";
import {BSSID_MENU_ITEMS} from "./BSSIDMenuItems";
import Dot11Service from "../../../services/Dot11Service";
import LoadingSpinner from "../../misc/LoadingSpinner";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets as TimeRange} from "../../shared/timerange/TimeRange";

const dot11Service = new Dot11Service();

export default function BSSIDTimelinePage() {

  const {bssidParam} = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [bssid, setBSSID] = useState(null);
  const [timeRange, setTimeRange] = useState(TimeRange.RELATIVE_HOURS_12);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setBSSID(null);
    dot11Service.findBSSID(bssidParam, selectedTaps, setBSSID);
  }, [bssidParam, selectedTaps]);

  if (!bssid) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-12">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
              <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.NETWORKS.BSSIDS.INDEX}>Access Points</a></li>
              <li className="breadcrumb-item">{bssid.summary.bssid.address}</li>
              <li className="breadcrumb-item active" aria-current="page">Timeline</li>
            </ol>
          </nav>
        </div>
      </div>

      <div className="row">
        <div className="col-md-12">
          <SectionMenuBar items={BSSID_MENU_ITEMS(bssid.summary.bssid.address)}
                          activeRoute={ApiRoutes.DOT11.NETWORKS.BSSIDS.TIMELIME(bssid.summary.bssid.address) }/>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <h1>
            BSSID &quot;{bssid.summary.bssid.address} ({bssid.summary.bssid.oui ? bssid.summary.bssid.oui : "Unknown Vendor"})&quot;
            Timeline
          </h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Timeline"
                                     slim={true}
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}