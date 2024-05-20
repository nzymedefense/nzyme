import React from "react";
import {truncate} from "../../../util/Tools";

export default function DNSData(props) {

  const value = props.value;

  if (!value) {
    return <span className="text-muted">None</span>
  }

  return <span title={value}>{truncate(value, 50, false)}</span>

}