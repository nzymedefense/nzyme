import React from "react";

export default function SocksTunnelId(props) {

  const tunnelId = props.tunnelId;

  return (
      <span className="socks-tunnel-id" title={tunnelId}>
        {tunnelId.substring(0, 6).toUpperCase()}
      </span>
  )

}