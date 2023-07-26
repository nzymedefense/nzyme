import React from "react";

function MonitoringDisabledWarning(props) {

  const show = props.show;

  if (show) {
    return (
        <div className="alert alert-warning mt-2">
          Monitoring for this network is currently disabled. Please remember to enable it once you've completed its
          configuration.
        </div>
    )
  }

  return null;

}

export default MonitoringDisabledWarning;