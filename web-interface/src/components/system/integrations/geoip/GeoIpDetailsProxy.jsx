import React from "react";
import IpInfoFreeProvider from "./providers/IpInfoFreeProvider";

function GeoIpDetailsProxy(props) {

  const provider = props.provider;
  const activeProvider = props.activeProvider;
  const activateProvider = props.activateProvider;

  switch (provider) {
    case "noop":
      return null;
    case "ipinfo_free":
      return <IpInfoFreeProvider activateProvider={activateProvider} activeProvider={activeProvider} />
    default:
      return "No Provider Details";
  }

}

export default GeoIpDetailsProxy;