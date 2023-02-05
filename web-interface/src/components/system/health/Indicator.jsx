import React from "react";

function Indicator(props) {

  const indicator = props.indicator;

  if (!indicator || indicator.expired) {
    return <div className="hc-indicator hc-invalid">{props.name}</div>
  }

  return <div className={"hc-indicator hc-" + indicator.level.toLowerCase()}>{props.name}</div>

}

export default Indicator;