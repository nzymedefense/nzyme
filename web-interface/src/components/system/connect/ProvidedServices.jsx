import React from "react";

export default function ProvidedServices(props) {

  const status = props.status;

  const serviceName = (service) => {
    switch (service) {
      case "ouis":
        return "MAC address vendor (OUI) information";
      case "bluetooth":
        return "Bluetooth vendor and device information";
      case "geoip_ipinfofree":
        return <span>GeoIP and ASN information. (Powered
          by <a href="https://ipinfo.io" target="_blank" rel="noreferrer">IPinfo.io</a>)</span>
      default:
        return service;
    }
  }

  if (!status.provided_services || status.provided_services.length === 0) {
    return <ul className="mb-0"><li>None</li></ul>
  }

  return (
      <ul className="mb-0">
        {status.provided_services.map((service, index) => {
          return <li key={index}>{serviceName(service)}</li>
        })}
      </ul>
  )

}