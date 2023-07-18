import React from "react";

function ObservedProbeRequestsList(props) {

  const probeRequests = props.probeRequests;

  if (!probeRequests || probeRequests.length === 0) {
    return <div className="alert alert-info mb-0">No probe requests observed.</div>
  }

  return (
      <ul className="mb-0">
        {probeRequests.map(function (probe, i) {
          return <li key={"pr-" + i}>{probe}</li>
        })}
      </ul>
  )

}

export default ObservedProbeRequestsList;