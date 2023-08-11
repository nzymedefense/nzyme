import React, {useEffect, useState} from "react";

import DetectionAlertsService from "../../services/DetectionAlertsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import AlertsTableRow from "./AlertsTableRow";
import AutoRefreshSelector from "../misc/AutoRefreshSelector";
import Paginator from "../misc/Paginator";

const detectionAlertsService = new DetectionAlertsService();

const loadData = function(setAlerts, page, perPage) {
  detectionAlertsService.findAllAlerts(setAlerts, perPage, (page-1)*perPage);
}

function AlertsTable() {

  const [alerts, setAlerts] = useState(null);
  const [isAutoRefresh, setIsAutoRefresh] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);

  const perPage = 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setAlerts(null);
    loadData(setAlerts, page, perPage);

    const timer = setInterval(() => {
      if (isAutoRefresh) {
        loadData(setAlerts, page, perPage);
      }
    }, 15000);

    return () => clearInterval(timer);
  }, [isAutoRefresh, page])

  if (!alerts) {
    return <LoadingSpinner />
  }

  if (alerts.alerts.length === 0) {
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

        <table className="table table-sm table-hover table-striped">
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
          {alerts.alerts.map(function(alert, i){
            return <AlertsTableRow key={"alert-" + i} alert={alert} />
          })}
          </tbody>
        </table>

        <Paginator itemCount={alerts.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default AlertsTable;
