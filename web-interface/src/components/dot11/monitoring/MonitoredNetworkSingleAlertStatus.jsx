import React from "react";

function MonitoredNetworkSingleAlertStatus(props) {

  const ssid = props.ssid;
  const alerted = props.alerted;

  if (ssid.is_enabled) {
    if (alerted) {
      return <i className="fa-solid fa-triangle-exclamation text-danger" title="Active alerts."></i>
    } else {
      return <i className="fa-solid fa-thumbs-up text-success" title="No active alerts."></i>
    }
  } else {
    return <i className="fa-solid fa-triangle-exclamation text-warning" title="Monitoring is disabled"></i>
  }

}

export default MonitoredNetworkSingleAlertStatus;