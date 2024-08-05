import React from "react";
import {truncate} from "../../../util/Tools";

export default function Hostname(props) {

  const hostname = props.hostname;

  return (
      <span className="hostname" title={hostname}>{truncate(hostname, 50, false)}</span>
  )

}