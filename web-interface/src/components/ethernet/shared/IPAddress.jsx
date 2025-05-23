import React from "react";

export default function IPAddress(props) {

  const ip = props.ip;

  if (!ip) {
    return <span className="text-muted">n/a</span>
  }

  return (
      <span className="ip-address">
        {ip}
      </span>
  )

}