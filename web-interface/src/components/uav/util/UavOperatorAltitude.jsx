import React from "react";
import {metersToFeet} from "../../../util/Tools";

import numeral from "numeral";

export default function UavOperatorAltitude(props) {

  const altitude = props.altitude;

  if (altitude === null || altitude === undefined) {
    return <span className="text-muted">n/a</span>
  }

  return <span title="Geodetic Altitude">{numeral(metersToFeet(altitude)).format("0,0")}ft GA</span>;

}