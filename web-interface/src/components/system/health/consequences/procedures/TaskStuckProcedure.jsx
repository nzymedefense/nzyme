import React from "react";
import SolutionCounter from "./layout/SolutionCounter";
import Conditional from "./layout/Conditional";
import ApiRoutes from "../../../../../util/ApiRoutes";

function TaskStuckProcedure(props) {
  return (
      <ol className="consequence-solution-procedure">
        <li>
          <SolutionCounter counter="1" /> Identify stuck cluster tasks using
          the <a href={ApiRoutes.SYSTEM.CLUSTER.MESSAGING.INDEX}>Cluster Messaging</a> page. A task is considered stuck
          if it has been acknowledged but did not report a success or failure result.
        </li>
        <li>
          <SolutionCounter counter="2" /> <Conditional text="For each" /> stuck cluster task perform:
        </li>
        <li className="consequence-solution-sublist">
          <ol>
            <li><SolutionCounter counter="2.1" /> Review stuck tasks and it's consequences. Review the
            logs of the nzyme node that acknowledged/handled it to find the source of the error.</li>
            <li><SolutionCounter counter="2.2" /> Retry or cancel the task.</li>
          </ol>
        </li>
        <li>
          <SolutionCounter counter="3" /> The indicator will extinguish within 60 seconds after no stuck tasks are
          found.
        </li>
      </ol>
  )
}

export default TaskStuckProcedure;