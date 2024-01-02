import React from "react";
import GenericAlertDetails from "./GenericAlertDetails";
import MonitorChannelAlertDetails from "./dot11/MonitorChannelAlertDetails";
import MonitorBSSIDAlertDetails from "./dot11/MonitorBSSIDAlertDetails";
import MonitorFingerprintAlertDetails from "./dot11/MonitorFingerprintAlertDetails";
import MonitorSecuritySuiteAlertDetails from "./dot11/MonitorSecuritySuiteAlertDetails";
import MonitorSignalTrackAlertDetails from "./dot11/MonitorSignalTrackAlertDetails";
import BanditContactAlertDetails from "./dot11/BanditContactAlertDetails";
import BanditContactPwnagotchiAlertDetails from "./dot11/BanditContactPwnagotchiAlertDetails";
import MonitorDiscoAnomalyAlertDetails from "./dot11/MonitorDiscoAnomalyAlertDetails";
import SimilarLookingSSIDAlertDetails from "./dot11/SimilarLookingSSIDAlertDetails";
import RestrictedSSIDSubstringAlertDetails from "./dot11/RestrictedSSIDSubstringAlertDetails";

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
    case "DOT11_MONITOR_DISCO_ANOMALIES":
      return <MonitorDiscoAnomalyAlertDetails alert={alert} />
    case "DOT11_BANDIT_CONTACT":
      if (alert.attributes.bandit_name === "Pwnagotchi") {
        return <BanditContactPwnagotchiAlertDetails alert={alert} />
      } else {
        return <BanditContactAlertDetails alert={alert} />
      }
    case "DOT11_MONITOR_SIMILAR_LOOKING_SSID":
      return <SimilarLookingSSIDAlertDetails alert={alert} />
    case "DOT11_MONITOR_SSID_SUBSTRING":
      return <RestrictedSSIDSubstringAlertDetails alert={alert} />
    default:
      return <GenericAlertDetails alert={alert} />
  }

}

export default AlertDetails;