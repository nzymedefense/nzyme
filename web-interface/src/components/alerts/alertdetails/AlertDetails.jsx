import React from "react";
import GenericAlertDetails from "./GenericAlertDetails";
import MonitorChannelAlertDetails from "./dot11/MonitorChannelAlertDetails";
import MonitorBSSIDAlertDetails from "./dot11/MonitorBSSIDAlertDetails";
import MonitorFingerprintAlertDetails from "./dot11/MonitorFingerprintAlertDetails";
import MonitorSecuritySuiteAlertDetails from "./dot11/MonitorSecuritySuiteAlertDetails";
import MonitorSignalTrackAlertDetails from "./dot11/MonitorSignalTrackAlertDetails";

function AlertDetails(props) {

  const alert = props.alert;

  switch (alert.detection_type) {
    case "DOT11_MONITOR_BSSID":
      return <MonitorBSSIDAlertDetails alert={alert} />
    case "DOT11_MONITOR_CHANNEL":
      return <MonitorChannelAlertDetails alert={alert} />
    case "DOT11_MONITOR_FINGERPRINT":
      return <MonitorFingerprintAlertDetails alert={alert} />
    case "DOT11_MONITOR_SECURITY_SUITE":
      return <MonitorSecuritySuiteAlertDetails alert={alert} />
    case "DOT11_MONITOR_SIGNAL_TRACK":
      return <MonitorSignalTrackAlertDetails alert={alert} />
    default:
      return <GenericAlertDetails alert={alert} />
  }

}

export default AlertDetails;