import React from "react";

export default function IPAddress(props) {

  const ip = props.ip;

  return (
      <span className="ip-address">
        {ip}
      </span>
  )

}