import React, {useEffect, useState} from "react";
import usePageTitle from "../../util/UsePageTitle";
import {Navigate, useParams} from "react-router-dom";
import ApiRoutes from "../../util/ApiRoutes";
import LoadingSpinner from "../misc/LoadingSpinner";
import MonitorsService from "../../services/MonitorsService";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import MonitorStatusIndicator from "./shared/MonitorStatusIndicator";
import MonitorType from "./shared/MonitorType";
import numeral from "numeral";
import moment from "moment";
import MonitorTapsTable from "./shared/MonitorTapsTable";
import TapsService from "../../services/TapsService";
import AppliedFilterList from "../shared/filtering/AppliedFilterList";
import reconstructFromNodeData from "../shared/filtering/FilterReconstructor";
import monitorTypeToFilterFields from "./shared/MonitorTools";
import {notify} from "react-notify-toast";

const monitorsService = new MonitorsService();
const tapsService = new TapsService();

export default function MonitorDetailsPage() {

  const {id} = useParams();

  const [organizationId, tenantId] = useSelectedTenant();

  const [monitor, setMonitor] = useState(null);
  const [taps, setTaps] = useState(null);

  const [redirect, setRedirect] = useState(false);

  usePageTitle(monitor ? `Monitor: ${monitor.name}` : "Monitor Details");

  useEffect(() => {
    monitorsService.findOne(id, organizationId, tenantId, setMonitor);
  }, [id])

  useEffect(() => {
    if (monitor) {
      tapsService.findAllTapsHighLevel(organizationId, tenantId, (r) => setTaps(r.data.taps));
    }
  }, [monitor]);

  const breadcrumbs = () => {
    switch (monitor.type) {
      case "DOT11_BSSID":
        return (
          <ol className="breadcrumb">
            <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
            <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.INDEX}>Monitoring</a></li>
            <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.MONITORS.INDEX}>Monitors</a></li>
            <li className="breadcrumb-item active" aria-current="page">{monitor.name}</li>
          </ol>
        )
    }
  }
  const backLink = () => {
    switch (monitor.type) {
      case "DOT11_BSSID":
        return ApiRoutes.DOT11.MONITORING.MONITORS.INDEX
    }
  }

  const onDelete = (e) => {
    e.preventDefault();

    if (!confirm("Really delete this monitor?")) {
      return;
    }

    monitorsService.deleteMonitor(monitor.uuid, () => {
      notify.show('Monitor deleted.', 'success');
      setRedirect(true);
    });
  }

  if (redirect) {
    return <Navigate to={backLink()} />
  }

  if (!monitor || taps === null) {
    return <LoadingSpinner />;
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-7">
          <nav aria-label="breadcrumb">
            {breadcrumbs()}
          </nav>
        </div>

        <div className="col-md-5">
            <span className="float-end">
              <a className="btn btn-secondary" href={backLink()}>Back</a>{' '}
              <a className="btn btn-danger" href="" onClick={onDelete}>Delete</a>{' '}
              <a className="btn btn-primary" href="">Edit</a>
            </span>
        </div>
      </div>

      <div className="row">
        <div className="col-md-12">
          <h1>
            <MonitorStatusIndicator monitor={monitor} /> Monitor &quot;{monitor.name}&quot;
          </h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Details" />

              <dl className="mb-0">
                <dt>Type</dt>
                <dd><MonitorType type={monitor.type} /></dd>
                <dt>Description</dt>
                <dd>{monitor.description ? monitor.description : <span className="text-muted">n/a</span> }</dd>
                <dt>Interval</dt>
                <dd>{numeral(monitor.interval).format("0,0")} {monitor.interval === 1 ? "Minute" : "Minutes"}</dd>
                <dt>Trigger Condition</dt>
                <dd>More than {numeral(monitor.trigger_condition).format("0,0")} {monitor.trigger_condition === 1 ? "result" : "results"}</dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="col-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Metadata" />

              <dl className="mb-0">
                <dt>Created at</dt>
                <dd title={moment(monitor.created_at).format()}>{moment(monitor.created_at).fromNow()}</dd>
                <dt>Updated at</dt>
                <dd title={moment(monitor.updated_at).format()}>{moment(monitor.updated_at).fromNow()}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Taps" />

              <MonitorTapsTable allTaps={taps} selectedTaps={monitor.taps} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Filters" />

              <AppliedFilterList
                filters={reconstructFromNodeData(JSON.parse(monitor.filters).filters, monitorTypeToFilterFields(monitor.type))}
                hideHeadline={true} />
            </div>
          </div>
        </div>
      </div>

    </React.Fragment>
  )

}