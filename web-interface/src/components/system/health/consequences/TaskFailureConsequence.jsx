import React from "react";
import Consequence from "../Consequence";
import TaskFailureProcedure from "./procedures/TaskFailureProcedure";

function TaskFailureConsequence(props) {

  if (!props.show) {
    return null
  }

  return (
      <Consequence
          indicator="Task Failure"
          color="red"
          problem="At least one cluster task has failed. Example cluster tasks are detection runs or delivery of alert
                  notifications."
          acceptableRange={[
            "No failed tasks in the last 24 hours."
          ]}
          consequences={[
            "An expected background action of the program was not performed"
          ]}
          procedure={<TaskFailureProcedure />}
      />
  )

}

export default TaskFailureConsequence;