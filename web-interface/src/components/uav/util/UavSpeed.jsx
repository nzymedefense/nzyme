import React from "react";
import numeral from "numeral";
import UavSpeedAccuracy from "./UavSpeedAccuracy";

export default function UavSpeed(props) {

  const speed = props.speed;
  const accuracy = props.accuracy;

  if (speed === null || speed === undefined) {
    return "Unknown"
  }

  return <span>{numeral(speed).format("0,0")}ft/s (<UavSpeedAccuracy accuracy={accuracy} />)</span>;

}