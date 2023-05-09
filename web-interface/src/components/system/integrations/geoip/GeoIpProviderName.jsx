import React from "react";

function GeoIpProviderName(props) {

  const id = props.id;

  switch (id) {
    case "noop":
      return "None";
    case "ipinfo_free":
      return "IPinfo.io Free"
    default:
      return "Unknown (" + id + ")";
  }

}

export default GeoIpProviderName;