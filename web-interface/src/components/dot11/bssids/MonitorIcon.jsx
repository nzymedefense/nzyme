import React from "react";

function MonitorIcon(props) {

  const is_monitored = props.is_monitored;
  const is_monitor_alerted = props.is_monitor_alerted;

  if (is_monitored) {
    if (is_monitor_alerted) {
      return <i className="fa-solid fa-shield-halved text-danger blink" title={"<onitored and there is an active alert."}
                style={{marginLeft: 4}}></i>
    } else {
      return <i className="fa-solid fa-shield-halved text-muted" title={"Monitored."}
         style={{marginLeft: 4}}></i>;
    }
  } else {
    return null;
  }

}

export default MonitorIcon;