import React from "react";
import Dot11SecurityProtocolList from "../shared/Dot11SecurityProtocolList";

function BSSIDSecurityProtocols(props) {

  const bssid = props.bssid;

  if (bssid.security_protocols.length === 1 && bssid.security_protocols[0] === "") {
    return "None";
  }

  if (bssid.security_protocols.length === 1 && bssid.security_protocols[0] === "None") {
    if (bssid.has_hidden_ssid_advertisements && bssid.advertised_ssid_names.length === 0) {
      // Only hidden SSIDs.
      return <span className="text-muted">Unknown/Hidden</span>;
    } else {
      return "None";
    }
  }

  return <Dot11SecurityProtocolList protocols={bssid.security_protocols} />

}

export default BSSIDSecurityProtocols;