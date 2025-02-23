import React from "react";

export default function UavSpeedAccuracy(props) {

  const accuracy = props.accuracy;

  if (accuracy === null || accuracy === undefined) {
    return <span>Unknown Accuracy</span>
  }

  switch (accuracy) {
    case 0:
      return <span>&#177;&#8805;33ft/s</span>
    case 1:
      return <span>&#177;&#10922;33ft/s</span>
    case 2:
      return <span>&#177;&#10922;10ft/s</span>
    case 3:
      return <span>&#177;&#10922;3ft/s</span>
    case 4:
      return <span>&#177;&#10922;1ft/s</span>
    default:
      return <span>Unknown Accuracy</span>
  }


}