import React from "react";
import numeral from "numeral";
import {metersToFeet} from "../../../util/Tools";

export default function UavOperatorDistanceToUav(props) {

  const distance = props.distance;

  if (distance == undefined || distance == null) {
    return <span className="text-muted">n/a</span>
  }

  return <span>{numeral(metersToFeet(distance)).format('0,0')}ft</span>;

}