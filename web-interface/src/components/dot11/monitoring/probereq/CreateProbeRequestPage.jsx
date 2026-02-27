import React, {useState} from 'react';
import {Navigate} from "react-router-dom";
import {notify} from "react-notify-toast";
import ApiRoutes from "../../../../util/ApiRoutes";
import Dot11Service from "../../../../services/Dot11Service";
import ProbeRequestForm from "./ProbeRequestForm";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";

const dot11Service = new Dot11Service();

export default function CreateProbeRequestPage() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [redirect, setRedirect] = useState(false);

  const create = (ssid, notes) => {
    dot11Service.createMonitoredProbeRequest(organizationId, tenantId, ssid, notes, () => {
      notify.show('Monitored probe request created.', 'success');
      setRedirect(true);
    });
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.INDEX} />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-7">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.INDEX}>Monitoring</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.INDEX}>Probe Requests</a></li>
                <li className="breadcrumb-item active" aria-current="page">Create</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-5">
            <span className="float-end">
              <a className="btn btn-primary" href={ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.INDEX}>Back</a>
            </span>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>
              Create new Monitored Probe Request
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>New Monitored Probe Request</h3>

                <ProbeRequestForm submitText="Create Monitored Probe Request" onSubmit={create} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}