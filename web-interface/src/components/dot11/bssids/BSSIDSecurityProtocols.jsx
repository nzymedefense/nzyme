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
      return <span className="hidden-ssid">Unknown/Hidden</span>;
    } else {
      return "None";
    }
  }

  // No need to show "none" for the hidden/broadcast announcements. Caught above if purely hidden/broadcast.
  const filtered = bssid.security_protocols.filter(function (sp) {
    return sp !== "None";
  });

  return <Dot11SecurityProtocolList protocols={filtered} />

}

export default BSSIDSecurityProtocols;