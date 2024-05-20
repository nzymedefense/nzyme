import React from "react";
import IPAddress from "./IPAddress";
import ApiRoutes from "../../../util/ApiRoutes";
import Port from "./Port";

export default function IPAddressLink(props) {

  const ip = props.ip;

  // Optional.
  const port = props.port;

  return (
      <a href={ApiRoutes.ETHERNET.L4.IP(ip)}>
        <IPAddress ip={ip} />{port === undefined || port === null ? null : <Port port={port} />}
      </a>
  )

}