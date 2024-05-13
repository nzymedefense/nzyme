import React from "react";
import IPAddress from "./IPAddress";
import ApiRoutes from "../../../util/ApiRoutes";

export default function IPAddressLink(props) {

  const ip = props.ip;

  return (
      <a href={ApiRoutes.ETHERNET.L4.IP(ip)}>
        <IPAddress ip={ip} />
      </a>
  )

}