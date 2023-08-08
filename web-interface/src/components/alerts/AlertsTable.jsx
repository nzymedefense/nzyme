import React, {useEffect, useState} from "react";

import DetectionAlertsService from "../../services/DetectionAlertsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import AlertsTableRow from "./AlertsTableRow";
import AutoRefreshSelector from "../misc/AutoRefreshSelector";

const detectionAlertsService = new DetectionAlertsService();

const loadData = function(setAlerts) {
  detectionAlertsService.findAllAlerts(setAlerts);
}

function AlertsTable() {

  const [alerts, setAlerts] = useState(null);
  const [isAutoRefresh, setIsAutoRefresh] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);

  useEffect(() => {
    loadData(setAlerts);

    const timer = setInterval(() => {
      if (isAutoRefresh) {
        loadData(setAlerts);
      }
    }, 15000);

    return () => clearInterval(timer);
  }, [isAutoRefresh])



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
        <div className="float-end">
          <AutoRefreshSelector isAutoRefresh={isAutoRefresh}
                               setIsAutoRefresh={setIsAutoRefresh}
                               lastUpdated={lastUpdated} />
        </div>

        <table className="table table-sm table-hover table-striped mb-0">
          <thead>
          <tr>
            <th>&nbsp;</th>
            <th>Details</th>
            <th>Type</th>
            <th>Subsystem</th>
            <th>First seen</th>
            <th>Last seen</th>
          </tr>
          </thead>
          <tbody>
          {alerts.map(function(alert, i){
            return <AlertsTableRow key={"alert-" + i} alert={alert} />
          })}
          </tbody>
        </table>
      </React.Fragment>
  )

}

export default AlertsTable;
