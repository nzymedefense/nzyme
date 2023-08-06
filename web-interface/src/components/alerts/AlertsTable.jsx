import React, {useEffect, useState} from "react";

import DetectionAlertsService from "../../services/DetectionAlertsService";
import LoadingSpinner from "../misc/LoadingSpinner";

const detectionAlertsService = new DetectionAlertsService();

function AlertsTable() {

  const [alerts, setAlerts] = useState(null);

  useEffect(() => {
    detectionAlertsService.findAllAlerts(setAlerts);
  }, [])

  if (!alerts) {
    return <LoadingSpinner />
  }

  if (alerts.length === 0) {
    return (
        <div className="alert alert-info mb-0">
          No alerts recorded yet.
        </div>
    )
  }

  return (
      <React.Fragment>
        <table>
          <thead>
          <tr>
            <th>Type</th>
            <th>Subsystem</th>
            <th>Details</th>
            <th>First seen</th>
            <th>Last seen</th>
          </tr>
          </thead>
        </table>
      </React.Fragment>
  )

}

export default AlertsTable;
