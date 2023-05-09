import React from "react";
import IpInfoFreeProvider from "./providers/IpInfoFreeProvider";

function GeoIpDetailsProxy(props) {

  const provider = props.provider;
  const summary = props.summary;

  switch (provider) {
    case "noop":
      return "NOOP EXPL";
    case "ipinfo_free":
      return <IpInfoFreeProvider summary={summary} />
    default:
      return "No Provider Details";
  }

}

export default GeoIpDetailsProxy;