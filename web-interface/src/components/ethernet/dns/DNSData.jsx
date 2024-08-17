import React from "react";
import HostnameLink from "../shared/HostnameLink";

export default function DNSData(props) {

  const value = props.value;

  if (!value) {
    return <span className="text-muted">None</span>
  }

  return <span title={value}><HostnameLink hostname={value} /></span>

}