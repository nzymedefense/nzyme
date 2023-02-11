import React from "react";

function Indicator(props) {

  const indicator = props.indicator;

  if (!indicator || indicator.expired || indicator.level === "UNAVAILABLE") {
    return <div className="hc-indicator hc-invalid">{props.name}</div>
  }

  return <div className={"hc-indicator hc-" + indicator.level.toLowerCase()}>{props.name}</div>

}

export default Indicator;