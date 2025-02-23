import React from "react";

export default function UavHorizontalAccuracy(props) {

  const accuracy = props.accuracy;

  if (accuracy === null || accuracy === undefined) {
    return <span>Unknown Accuracy</span>
  }

  switch (accuracy) {
    case 0:
      return <span>&#177;&#8805;11mi</span>
    case 1:
      return <span>&#177;&#10922;11mi</span>
    case 2:
      return <span>&#177;&#10922;4.6mi</span>
    case 3:
      return <span>&#177;&#10922;2.3mi</span>
    case 4:
      return <span>&#177;&#10922;1.1mi</span>
    case 5:
      return <span>&#177;&#10922;0.6mi</span>
    case 6:
      return <span>&#177;&#10922;0.3mi</span>
    case 7:
      return <span>&#177;&#10922;600ft</span>
    case 8:
      return <span>&#177;&#10922;300ft</span>
    case 9:
      return <span>&#177;&#10922;100ft</span>
    case 10:
      return <span>&#177;&#10922;33ft</span>
    case 11:
      return <span>&#177;&#10922;10ft</span>
    case 12:
      return <span>&#177;&#10922;3ft</span>
    default:
      return <span>Unknown Accuracy</span>
  }


}