import React, {useEffect, useState} from "react";
import AlertsTable from "./AlertsTable";
import DetectionAlertsService from "../../services/DetectionAlertsService";

import numeral from "numeral";
import LoadingSpinner from "../misc/LoadingSpinner";
import NumberCard from "../widgets/presentation/NumberCard";

const alertsService = new DetectionAlertsService();

function AlertsPage() {

  const [alerts, setAlerts] = useState(null);

  useEffect(() => {
    alertsService.findAllAlerts(setAlerts, 0, 0);

    const timer = setInterval(() => {
      alertsService.findAllAlerts(setAlerts, 0, 0)
    }, 15000);

    return () => clearInterval(timer);
  }, []);

  if (!alerts) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <h1>Alerts</h1>
          </div>

          <div className="col-md-2">
            <a href="https://go.nzyme.org/detection-alerts" className="btn btn-secondary float-end">Help</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-6">
            <NumberCard title="Active Alerts" value={alerts.total_active} numberFormat="0,0" />
          </div>

          <div className="col-6">
            <NumberCard title="Total Alerts" value={alerts.total} numberFormat="0,0" />
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <h3>All Alerts</h3>

                <p className="text-muted">
                  Alerts are marked as active if they have been seen in the previous 5 minutes. Existing alerts can
                  re-activate if they are considered to be triggered from the same source or for the same reason.
                </p>

                <AlertsTable/>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default AlertsPage;