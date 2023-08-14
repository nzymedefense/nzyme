import React from "react";
import GenericAlertDetails from "./GenericAlertDetails";
import MonitorChannelAlertDetails from "./dot11/MonitorChannelAlertDetails";

function AlertDetails(props) {

  const alert = props.alert;

  switch (alert.detection_type) {
    case "DOT11_MONITOR_CHANNEL":
      return <MonitorChannelAlertDetails alert={alert} />
    default:
      return <GenericAlertDetails alert={alert} />
  }

}

export default AlertDetails;