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
import RenderConditionally from "../misc/RenderConditionally";

const detectionAlertsService = new DetectionAlertsService();

const loadData = function(setAlerts, subsystem, page, perPage) {
  detectionAlertsService.findAllAlerts(setAlerts, perPage, (page-1)*perPage, subsystem);
}

function AlertsTable(props) {

  const user = useContext(UserContext);

  const perPage = props.perPage ? props.perPage : 25;
  const hideControls = props.hideControls ? props.hideControls : false;

  // Optional.
  const subsystem = props.subsystem;

  const [alerts, setAlerts] = useState(null);
  const [isAutoRefresh, setIsAutoRefresh] = useState(false);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [selectedRows, setSelectedRows] = useState([]);
  const [allRowsSelected, setAllRowsSelected] = useState(false);

  const [page, setPage] = useState(1);

  const [revision, setRevision] = useState(0);

  const onRowSelect = (uuid) => {
    const copy = [...selectedRows];

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
      setAllRowsSelected(false);
    })
  }

  const resolveSelected = (e) => {
    e.preventDefault();
    detectionAlertsService.markAlertsAsResolved(selectedRows, () => {
      setRevision(prevRev => prevRev + 1);
      setSelectedRows([]);
      notify.show("Selected alerts marked as resolved.", "success");
      setAllRowsSelected(false);
    })
  }

  const handleAllRowsSelected = () => {
    if (allRowsSelected) {
      // Unselect
      setAllRowsSelected(false);
      setSelectedRows([]);
    } else {
      // Select
      setAllRowsSelected(true);

      const selected = [];
      alerts.alerts.forEach((alert) => {
        selected.push(alert.id);
      })

      setSelectedRows(selected);
    }
  }

  useEffect(() => {
    setAlerts(null);
    loadData(setAlerts, subsystem, page, perPage);

    const timer = setInterval(() => {
      if (isAutoRefresh) {
        loadData(setAlerts, subsystem, page, perPage);
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
                show={userHasPermission(user, "alerts_manage") && !hideControls}
                selectedRows={selectedRows}
                onDeleteSelected={deleteSelected}
                onResolveSelected={resolveSelected}/>
          </div>

          <RenderConditionally render={!hideControls}>
            <div className="float-end">
              <AutoRefreshSelector isAutoRefresh={isAutoRefresh}
                                   setIsAutoRefresh={setIsAutoRefresh}
                                   lastUpdated={lastUpdated}/>
            </div>
          </RenderConditionally>

          <div style={{clear: "both"}}/>
        </div>

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <RenderConditionally render={!hideControls}>
              <th>
                <input className="form-check-input"
                       type="checkbox"
                       checked={allRowsSelected}
                       onChange={handleAllRowsSelected}/>
              </th>
            </RenderConditionally>
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
                                   hideControls={hideControls}
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
