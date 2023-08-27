import React from "react";

function MonitoredNetworkSingleAlertStatus(props) {

  const ssid = props.ssid;
  const parameter = props.parameter;

  if (ssid.is_enabled && ssid["enabled_" + parameter]) {
    if (ssid["status_" + parameter]) {
      return <i className="fa-solid fa-triangle-exclamation text-danger" title="Active alerts."></i>
    } else {
      return <i className="fa-solid fa-thumbs-up text-success" title="No active alerts."></i>
    }
  } else {
    return <i className="fa-solid fa-triangle-exclamation text-warning" title="Monitoring is disabled"></i>
  }

}

export default MonitoredNetworkSingleAlertStatus;