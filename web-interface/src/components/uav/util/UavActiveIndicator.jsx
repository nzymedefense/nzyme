import React from "react";

export default function UavActiveIndicator(props) {

  const active = props.active;

  if (active) {
    return <i className="fa-solid fa-circle text-success blink"
              title="UAV is active and has been recorded recently."></i>
  } else {
    return <i className="fa-solid fa-circle text-muted"
              title="UAV is currently not active, has not been recorded recently."></i>
  }

}