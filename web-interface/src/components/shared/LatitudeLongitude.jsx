import React from "react";
import UavHorizontalAccuracy from "../uav/util/UavHorizontalAccuracy";

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
        <span>{latitude}, {longitude}</span>
    )
  }

  return (
    <span>{latitude}, {longitude} (<UavHorizontalAccuracy accuracy={accuracy} />)</span>
  )

}