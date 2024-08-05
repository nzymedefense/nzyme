import React from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import SocksTunnelId from "./SocksTunnelId";

export default function SocksTunnelIdLink(props) {

  const tunnelId = props.tunnelId;

  return (
      <a href={ApiRoutes.ETHERNET.TUNNELS.SOCKS.TUNNEL_DETAILS(tunnelId)}>
        <SocksTunnelId tunnelId={tunnelId} />
      </a>
  )

}