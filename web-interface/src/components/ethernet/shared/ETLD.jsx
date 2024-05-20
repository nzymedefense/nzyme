import React from "react";
import {truncate} from "../../../util/Tools";

export default function ETLD(props) {

  const etld = props.etld;

  if (!etld) {
    return <span className="text-muted">n/a</span>
  }

  return <span title={etld}>{truncate(etld, 50, false)}</span>

}