import React from "react";

export default function UavInactiveWarning(props) {

  const show = props.show;

  if (!show) {
    return null;
  }

  return <div className="alert alert-info mt-3">
    This UAV has not been observed recently. Make sure to check timestamps.
  </div>

}