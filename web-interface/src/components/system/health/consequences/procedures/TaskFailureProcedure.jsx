import React from "react";
import SolutionCounter from "./layout/SolutionCounter";
import Conditional from "./layout/Conditional";
import ApiRoutes from "../../../../../util/ApiRoutes";

function TaskFailureProcedure(props) {
  return (
      <ol className="consequence-solution-procedure">
        <li>
          <SolutionCounter counter="1" /> Identify failed cluster tasks using
          the <a href={ApiRoutes.SYSTEM.CLUSTER.MESSAGING.INDEX}>Cluster Messaging</a> page.
        </li>
        <li>
          <SolutionCounter counter="2" /> <Conditional text="For each" /> failed cluster task perform:
        </li>
        <li className="consequence-solution-sublist">
          <ol>
            <li><SolutionCounter counter="2.1" /> Review failed tasks and it's consequences. Either retry the task or
            mark the failure as acknowledged.</li>
          </ol>
        </li>
        <li>
          <SolutionCounter counter="3" /> The indicator will extinguish within 60 seconds after no failed tasks are
          found.
        </li>
      </ol>
  )
}

export default TaskFailureProcedure;