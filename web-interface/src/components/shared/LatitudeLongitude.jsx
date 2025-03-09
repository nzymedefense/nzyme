import React from "react";
import UavHorizontalAccuracy from "../uav/util/UavHorizontalAccuracy";

import numeral from "numeral";

export default function LatitudeLongitude(props) {

  const latitude = props.latitude;
  const longitude = props.longitude;

  // Optional
  const skipAccuracy = props.skipAccuracy;
  const accuracy = props.accuracy;

  if (!latitude || !longitude) {
    return <span className="text-muted">n/a</span>
  }

  if (skipAccuracy) {
    return (
        <span>{numeral(latitude).format("0.0000")}, {numeral(longitude).format("0.0000")}</span>
    )
  }

  return (
    <span>{numeral(latitude).format("0.0000")}, {numeral(longitude).format("0.0000")} (<UavHorizontalAccuracy accuracy={accuracy} />)</span>
  )

}