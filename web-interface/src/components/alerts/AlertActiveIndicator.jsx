import React from "react";

function AlertActiveIndicator(props) {

  const active = props.active;

  if (active) {
    return <i className="fa-solid fa-circle text-danger blink" title="Alert is active."></i>
  } else {
    return <i className="fa-solid fa-circle text-muted" title="Alert is not active."></i>
  }

}

export default AlertActiveIndicator;