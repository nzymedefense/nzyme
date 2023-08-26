import React, {useContext, useEffect, useState} from "react";

import DetectionAlertsService from "../../services/DetectionAlertsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import AlertsTableRow from "./AlertsTableRow";
import AutoRefreshSelector from "../misc/AutoRefreshSelector";
import Paginator from "../misc/Paginator";
import {notify} from "react-notify-toast";
import AlertActionMultiSelector from "./AlertActionMultiSelector";
import {userHasPermission} from "../../util/Tools";
import {UserContext} from "../../App";

const detectionAlertsService = new DetectionAlertsService();

const loadData = function(setAlerts, page, perPage) {
  detectionAlertsService.findAllAlerts(setAlerts, perPage, (page-1)*perPage);
}

function AlertsTable() {

  const user = useContext(UserContext);

  const [alerts, setAlerts] = useState(null);
  const [isAutoRefresh, setIsAutoRefresh] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [selectedRows, setSelectedRows] = useState([]);

  const perPage = 25;
  const [page, setPage] = useState(1);

  const [revision, setRevision] = useState(0);

  const onRowSelect = (uuid) => {
    let copy = [...selectedRows];

    if (copy.includes(uuid)) {
      // Unselect.
      const idx = copy.indexOf(uuid);
      if (idx > -1) {
        copy.splice(idx, 1);
      }
    } else {
      copy.push(uuid);
    }

    setSelectedRows(copy);
  }

  const deleteSelected = (e) => {
    e.preventDefault();

    if (!confirm("Really delete alerts?")) {
      return;
    }

    detectionAlertsService.deleteAlerts(selectedRows, () => {
      setRevision(prevRev => prevRev + 1);
      setSelectedRows([]);
      notify.show("Selected alerts deleted.", "success");
    })
  }

  const resolveSelected = (e) => {
    e.preventDefault();
    detectionAlertsService.markAlertsAsResolved(selectedRows, () => {
      setRevision(prevRev => prevRev + 1);
      setSelectedRows([]);
      notify.show("Selected alerts marked as resolved.", "success");
    })
  }

  useEffect(() => {
    setAlerts(null);
    loadData(setAlerts, page, perPage);

    const timer = setInterval(() => {
      if (isAutoRefresh) {
        loadData(setAlerts, page, perPage);
      }
    }, 15000);

    return () => clearInterval(timer);
  }, [isAutoRefresh, page, revision])

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
        <div className="mb-2">
          <div className="float-start">
            <AlertActionMultiSelector
                show={userHasPermission(user, "alerts_manage")}
                selectedRows={selectedRows}
                onDeleteSelected={deleteSelected}
                onResolveSelected={resolveSelected} />
          </div>

          <div className="float-end">
            <AutoRefreshSelector isAutoRefresh={isAutoRefresh}
                                 setIsAutoRefresh={setIsAutoRefresh}
                                 lastUpdated={lastUpdated} />
          </div>

          <div style={{clear: "both"}} />
        </div>

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>&nbsp;</th>
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
            return <AlertsTableRow key={"alert-" + i}
                                   alert={alert}
                                   onSelect={onRowSelect}
                                   isSelected={selectedRows.includes(alert.id)} />
          })}
          </tbody>
        </table>

        <Paginator itemCount={alerts.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default AlertsTable;
