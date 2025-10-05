import React from "react";

export default function DNSData(props) {

  const value = props.value;

  if (!value) {
    return <span className="text-muted">None</span>
  }

  return <span title={value}>{value}</span>

}