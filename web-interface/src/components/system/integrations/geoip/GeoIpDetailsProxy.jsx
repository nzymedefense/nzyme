import React from "react";
import IpInfoFreeProvider from "./providers/IpInfoFreeProvider";
import NoOpProvider from "./providers/NoOpProvider";

function GeoIpDetailsProxy(props) {

  const provider = props.provider;
  const activeProvider = props.activeProvider;
  const activateProvider = props.activateProvider;

  switch (provider) {
    case "noop":
      return <NoOpProvider activateProvider={activateProvider} activeProvider={activeProvider} />
    case "ipinfo_free":
      return <IpInfoFreeProvider activateProvider={activateProvider} activeProvider={activeProvider} />
    default:
      return "No Provider Details";
  }

}

export default GeoIpDetailsProxy;