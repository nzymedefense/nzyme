import Port from "./Port";
import React from "react";
import Hostname from "./Hostname";

export default function HostnameLink(props) {

  const hostname = props.hostname;

  // Optional.
  const port = props.port;

  return (
      <a href="#">
        <Hostname hostname={hostname} />{port === undefined || port === null ? null : <Port port={port} />}
      </a>
  )

}