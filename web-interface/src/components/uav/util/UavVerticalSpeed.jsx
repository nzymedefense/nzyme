import React from "react";
import numeral from "numeral";
import {metersToFeet} from "../../../util/Tools";

export default function UavVerticalSpeed(props) {

  const verticalSpeed = props.verticalSpeed;

  const prefix = (verticalSpeed) => {
    if (verticalSpeed <= 0.0) {
      return null;
    }

    return "+";
  }

  if (verticalSpeed === null || verticalSpeed === undefined) {
    return "Unknown"
  }

  return <span>{prefix(verticalSpeed)}{numeral(metersToFeet(verticalSpeed)).format("0,0")} ft/s</span>

}