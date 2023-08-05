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

  return (
      <React.Fragment>
        alerts
      </React.Fragment>
  )

}

export default AlertsTable;
