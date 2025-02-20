import React from "react";

export default function LatitudeLongitude(props) {

  const latitude = props.latitude;
  const longitude = props.longitude;

  if (!latitude || !longitude) {
    return <span className="text-muted">n/a</span>
  }

  return (
    <span>{latitude}, {longitude}</span>
  )

}