import React from "react";
import FullCopyShortenedId from "../../shared/FullCopyShortenedId";
import ApiRoutes from "../../../util/ApiRoutes";

export default function SocksTunnelId(props) {

  const tunnelId = props.tunnelId;

  return (
    <a href={ApiRoutes.ETHERNET.TUNNELS.SOCKS.TUNNEL_DETAILS(tunnelId)}>
      <FullCopyShortenedId value={tunnelId} />
    </a>
  )

}