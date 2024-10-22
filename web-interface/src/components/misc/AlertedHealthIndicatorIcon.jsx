import React from "react";

export default function AlertedHealthIndicatorIcon(props) {

  const iconClass = props.icon;
  const healthIndicatorLevel = props.healthIndicatorLevel;

  const icon = (additionalClass) => {
    return <i className={"sidebar-icon " + iconClass + " " + (additionalClass ? additionalClass : "")}/>
  }

  if (healthIndicatorLevel) {
    if (healthIndicatorLevel === "ORANGE") {
      return icon("text-warning blink");
    }

    if (healthIndicatorLevel === "RED") {
      return icon("text-danger blink");
    }
  }

  return icon();

}