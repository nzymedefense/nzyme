import React, {useContext} from "react";
import {UserContext} from "../../../../App";
import SSIDMonitoringDetailsLink from "./SSIDMonitoringDetailsLink";

function SSIDMonitoredInformation(props) {

  const user = useContext(UserContext);

  const ssid = props.ssid;

  if (ssid.is_monitored) {
    if (ssid.is_monitor_alerted) {
      return (
          <div className="alert alert-danger mt-2">
            <i className="fa-solid fa-triangle-exclamation"></i>&nbsp; This network is monitored and has an active alarm.{' '}

            <SSIDMonitoringDetailsLink
                uuid={ssid.monitor_uuid}
                show={user.is_orgadmin || user.is_superadmin || user.feature_permissions.includes("dot11_monitoring_manage")}
            />
          </div>
      )
    } else {
      return (
          <div className="alert alert-info mt-2">
            <i className="fa-solid fa-shield-halved"></i>&nbsp; This network is monitored and there are no active alarms.{' '}

            <SSIDMonitoringDetailsLink
                uuid={ssid.monitor_uuid}
                show={user.is_orgadmin || user.is_superadmin || user.feature_permissions.includes("dot11_monitoring_manage")}
            />
          </div>
      )
    }
  }

  return null;

}

export default SSIDMonitoredInformation;