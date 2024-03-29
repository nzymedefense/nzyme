import React from "react";

function MonitoredNetworkStatus(props) {

  const ssid = props.ssid;

  if (ssid.is_enabled) {
    if (ssid.is_alerted) {
      return <i className="fa-solid fa-triangle-exclamation text-danger" title="Active alerts."></i>
    } else {
      return <i className="fa-solid fa-thumbs-up text-success" title="No active alerts."></i>
    }
  } else {
    return <i className="fa-solid fa-triangle-exclamation text-warning" title="Monitoring is disabled"></i>
  }

}

export default MonitoredNetworkStatus;