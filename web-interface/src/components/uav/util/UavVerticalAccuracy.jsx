import React from "react";

export default function UavVerticalAccuracy(props) {

  const accuracy = props.accuracy;

  if (accuracy === null || accuracy === undefined) {
    return <span>Unknown Accuracy</span>
  }

  switch (accuracy) {
    case 0:
      return <span>&#177;&#8805;492ft</span>
    case 1:
      return <span>&#177;&#10922;492ft</span>
    case 2:
      return <span>&#177;&#10922;148ft</span>
    case 3:
      return <span>&#177;&#10922;82ft</span>
    case 4:
      return <span>&#177;&#10922;33ft</span>
    case 5:
      return <span>&#177;&#10922;10ft</span>
    case 6:
      return <span>&#177;&#10922;3ft</span>
    default:
      return <span>Unknown Accuracy</span>
  }

}