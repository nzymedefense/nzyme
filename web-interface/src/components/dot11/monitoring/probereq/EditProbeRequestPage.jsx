import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import {notify} from "react-notify-toast";
import ApiRoutes from "../../../../util/ApiRoutes";
import ProbeRequestForm from "./ProbeRequestForm";
import Dot11Service from "../../../../services/Dot11Service";
import LoadingSpinner from "../../../misc/LoadingSpinner";

const dot11Service = new Dot11Service();

export default function EditProbeRequestPage() {

  const { id } = useParams();
  const { organizationId } = useParams();
  const { tenantId } = useParams();

  const [probeRequest, setProbeRequest] = useState(null);

  const [redirect, setRedirect] = useState(false);

  const create = (ssid, notes) => {
    dot11Service.updateMonitoredProbeRequest(organizationId, tenantId, id, ssid, notes, () => {
      notify.show('Monitored probe request updated.', 'success');
      setRedirect(true);
    });
  }

  useEffect(() => {
    dot11Service.findMonitoredProbeRequest(organizationId, tenantId, id, setProbeRequest);
  }, [id, organizationId, tenantId]);

  if (redirect) {
    return <Navigate to={ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.INDEX} />
  }

  if (!probeRequest) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-7">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.INDEX}>Monitoring</a></li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.INDEX}>Probe Requests</a>
                </li>
                <li className="breadcrumb-item">{probeRequest.ssid}</li>
                <li className="breadcrumb-item active" aria-current="page">Edit</li>
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
              Edit Monitored Probe Request &quot;{probeRequest.ssid}&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Edit Monitored Probe Request</h3>

                <ProbeRequestForm ssid={probeRequest.ssid} notes={probeRequest.notes} submitText="Edit Monitored Probe Request" onSubmit={create} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}