import React from "react";

function Dot11SecurityProtocol(props) {

  const protocol = props.protocol;

  if (!protocol) {
    return "None/Unknown"
  }

  switch (protocol) {
    case "WEP":
      return "WEP"
    case "WPA1":
      return "WPA1"
    case "WPA2Personal":
      return "WPA2-Personal"
    case "WPA2Enterprise":
      return "WPA2-Enterprise"
    case "WPA3Transition":
      return "WPA3-Transition"
    case "WPA3Personal":
      return "WPA3-Personal"
    case "WPA3Enterprise":
      return "WPA3-Enterprise"
    case "WPA3EnterpriseCNSA":
      return "WPA3-Enterprise (CNSA/192)"
    default:
      return protocol
  }

}

export default Dot11SecurityProtocol;