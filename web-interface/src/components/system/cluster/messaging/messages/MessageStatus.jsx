import React from "react";
import ClusterService from "../../../../../services/ClusterService";
import numeral from "numeral";

const clusterService = new ClusterService();

function MessageStatus(props) {

  const message = props.message;
  const setLocalRevision = props.setLocalRevision;

  const cancel = function(e) {
    e.preventDefault();

    if (!confirm("Really acknowledge processing failure?")) {
      return;
    }

    clusterService.acknowledgeFailedMessage(message.id, function() {
      setLocalRevision(prevRev => prevRev + 1)
    });
  }

  if (message.status === "PROCESSED_SUCCESS") {
    return (
        <React.Fragment>
          {message.status} ({numeral(message.processing_time_ms).format()}ms)
        </React.Fragment>
    )
  }

  if (message.status === "PROCESSED_FAILURE") {
    return (
        <span className="text-danger">
          {message.status}{' '}

          <a href="" onClick={cancel}>Acknowledge</a>
        </span>
    )
  }

  if (message.status === "FAILURE_ACKNOWLEDGED") {
    return (
        <span className="text-warning">
          {message.status}
        </span>
    )
  }

  return message.status;

}

export default MessageStatus;