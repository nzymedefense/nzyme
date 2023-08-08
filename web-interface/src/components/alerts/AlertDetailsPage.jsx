import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import DetectionAlertsService from "../../services/DetectionAlertsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import ApiRoutes from "../../util/ApiRoutes";
import ToggleMonitoringStatusButton from "../dot11/monitoring/ToggleMonitoringStatusButton";
import RefreshGears from "../misc/RefreshGears";
import moment from "moment/moment";
import Subsystem from "../misc/Subsystem";
import AlertActiveIndicator from "./AlertActiveIndicator";

const alertsService = new DetectionAlertsService();

function AlertDetailsPage() {

  const {uuid} = useParams();

  const [alert, setAlert] = useState(null);

  useEffect(() => {
    alertsService.findAlert(uuid, setAlert);
  }, [uuid]);

  if (!alert) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-9">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.ALERTS.INDEX}>Alerts</a></li>
                <li className="breadcrumb-item active" aria-current="page">{alert.id}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-primary" href={ApiRoutes.ALERTS.INDEX}>Back</a>
            </span>
          </div>

          <div className="col-md-12">
            <h1>
              Alert {alert.id.substr(0, 8)}
            </h1>
          </div>
        </div>

        <div className="row mt-2">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <AlertActiveIndicator active={alert.is_active} />{' '}&nbsp;
                {alert.details}
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Alert Details</h3>

                <dl className="mb-0">
                  <dt>Detection Type</dt>
                  <dd>{alert.detection_type}</dd>
                  <dt>Subsystem</dt>
                  <dd><Subsystem subsystem={alert.subsystem} /></dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Metadata</h3>

                <dl className="mb-0">
                  <dt>First Seen</dt>
                  <dd>{moment(alert.created_at).format()}</dd>
                  <dt>Last Seen</dt>
                  <dd>{moment(alert.last_seen).format()}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Parameters</h3>

                <pre>
                  {JSON.stringify(alert.attributes, null, 2)}
                </pre>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default AlertDetailsPage;