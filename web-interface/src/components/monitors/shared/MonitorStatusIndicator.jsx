import React from "react";

export default function MonitorStatusIndicator({monitor}) {

  if (!monitor.enabled) {
    return <i className="fa-solid fa-circle text-muted" title="Monitor is disabled."></i>
  } else {
    if (monitor.alerted) {
      return <i className="fa-solid fa-circle text-danger blink" title="Monitor is currently alerted."></i>
    } else {
      return <i className="fa-solid fa-circle text-success" title="Monitor is not currently alerted."></i>
    }
  }

}