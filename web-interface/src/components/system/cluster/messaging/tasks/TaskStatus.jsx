import React from "react";
import numeral from "numeral";
import ClusterService from "../../../../../services/ClusterService";
import {notify} from "react-notify-toast";

const clusterService = new ClusterService();

function TaskStatus(props) {

  const task = props.task;
  const setLocalRevision = props.setLocalRevision;

  const cancel = function(e) {
    e.preventDefault();

    if (!confirm("Really acknowledge processing failure?")) {
      return;
    }

    clusterService.acknowledgeFailedTask(task.id, function() {
      notify.show("Task acknowledged.", "success");
      setLocalRevision(prevRev => prevRev + 1)
    });
  }

  if (task.status === "PROCESSED_SUCCESS") {
    return (
        <React.Fragment>
          {task.status} ({numeral(task.processing_time_ms).format()}ms)
        </React.Fragment>
    )
  }

  if (task.status === "PROCESSED_FAILURE") {
    return (
        <span className="text-danger">
          {task.status}{' '}

          <a href="" onClick={cancel}>Acknowledge</a>
        </span>
    )
  }

  if (task.status === "FAILURE_ACKNOWLEDGED") {
    return (
        <span className="text-warning">
          {task.status}
        </span>
    )
  }

  return task.status

}

export default TaskStatus;