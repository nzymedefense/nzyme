import React from "react";

function MonitoredNetworkSingleAlertStatus(props) {

  const ssid_is_enabled = props.ssid_is_enabled;
  const status = props.status;

  if (ssid_is_enabled) {
    if (status) {
      return <i className="fa-solid fa-triangle-exclamation text-danger" title="Active alerts."></i>
    } else {
      return <i className="fa-solid fa-thumbs-up text-success" title="No active alerts."></i>
    }
  } else {
    return <i className="fa-solid fa-triangle-exclamation text-warning" title="Monitoring is disabled"></i>
  }

}

export default MonitoredNetworkSingleAlertStatus;