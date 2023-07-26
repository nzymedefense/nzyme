import React from "react";

function SSIDMonitoredInformation(props) {

  const show = props.show;

  if (show) {
    return (
        <div className="alert alert-info mt-2">
          <i className="fa-solid fa-shield-halved"></i>&nbsp; This network is monitored.
        </div>
    )
  }

  return null;

}

export default SSIDMonitoredInformation;