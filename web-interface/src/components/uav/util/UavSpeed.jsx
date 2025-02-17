import React from "react";
import numeral from "numeral";

export default function UavSpeed(props) {

  const speed = props.speed;

  if (speed === null || speed === undefined) {
    return "Unknown"
  }

  return <span>{numeral(speed).format("0,0")} ft/s</span>;

}