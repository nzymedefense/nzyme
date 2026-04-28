import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import SectionMenuBar from "../../shared/SectionMenuBar";
import ApiRoutes from "../../../util/ApiRoutes";
import {BSSID_MENU_ITEMS} from "./BSSIDMenuItems";
import Dot11Service from "../../../services/Dot11Service";
import LoadingSpinner from "../../misc/LoadingSpinner";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import TimelinesService from "../../../services/TimelinesService";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import usePageTitle from "../../../util/UsePageTitle";
import Timeline from "../../shared/timelines/Timeline";
import {timeRangeFromURLOrDefault} from "../../shared/timerange/TimeRangeSelector";

const dot11Service = new Dot11Service();
const timelinesService = new TimelinesService();

export default function BSSIDTimelinePage() {

  const {bssidParam} = useParams();
  const [organizationId, tenantId] = useSelectedTenant();

  const [bssid, setBSSID] = useState(null);
  const [timeRange, setTimeRange] = useState(() => timeRangeFromURLOrDefault(Presets.RELATIVE_DAYS_7))

  const [events, setEvents] = useState(null);

  const [page, setPage] = useState(1);
  const perPage = 50;

  const [revision, setRevision] = useState(new Date());

  usePageTitle(bssid ? `BSSID: ${bssid.summary.bssid.address} Timeline` : "BSSID Timeline");

  useEffect(() => {
    setBSSID(null);
    dot11Service.findBSSID(bssidParam, "*", setBSSID);
  }, [bssidParam]);

  useEffect(() => {
    if (bssid) {
      setEvents(null);
      timelinesService.findAllOfAddress(
        "DOT11_BSSID",
        bssid.summary.bssid.address,
        organizationId,
        tenantId,
        timeRange,
        perPage,
        (page-1)*perPage,
        setEvents
      );
    }
  }, [bssid, organizationId, tenantId, timeRange, perPage, page, revision])

  const summarize = () => {
    timelinesService.summarizeTimelineOfAddress(
      "DOT11_BSSID",
      bssid.summary.bssid.address,
      organizationId,
      tenantId,
      timeRange,
      perPage,
      (page-1)*perPage,
      ()=>{}
    );
  }

  if (!bssid || !events) {
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
                                     refreshAction={() => setRevision(new Date())}
                                     timeRange={timeRange}
                                     setTimeRange={setTimeRange} />

              <button className="btn btn-secondary" onClick={summarize}>Summarize</button>

              <Timeline events={events}
                        addressLastSeen={bssid.summary.last_seen}
                        page={page}
                        setPage={setPage}
                        perPage={perPage }/>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}