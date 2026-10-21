import React from "react";

export const beaconTypeToName = (type) => {
  switch (type) {
    case "TLS": return "TLS Beacon";
    case "HTTP": return "HTTP Beacon";
    case "SSH": return "SSH Beacon";
    case "SOCKS": return "SOCKS Beacon";
    case "DNS": return "DNS Beacon";
    case "TCP": return "TCP Beacon";
    case "UDP": return "UDP Beacon";
    default: return type;
  }
}

export default function BeaconType({type}) {
  return <span title={type}>{beaconTypeToName(type)}</span>
}
