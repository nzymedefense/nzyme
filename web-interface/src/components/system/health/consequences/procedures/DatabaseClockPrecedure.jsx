import React from "react";
import SolutionCounter from "./layout/SolutionCounter";
import Conditional from "./layout/Conditional";

function DatabaseClockProcedure(props) {
  return (
      <ol className="consequence-solution-procedure">
        <li>
          <li><SolutionCounter counter="1" /> <Conditional text="For each" /> database server, perform:</li>
          <li className="consequence-solution-sublist">
            <ol>
              <li>
                <SolutionCounter counter="1.1" /> Install and configure <code>ntpd</code> or equivalent time
                synchronization service
              </li>
              <li className="consequence-solution-or">
                &mdash; <Conditional text="or" /> &mdash;
              </li>
              <li>
                <SolutionCounter counter="1.2" /> Manually adjust the time and ensure it stays synchronized
              </li>
            </ol>
          </li>
        </li>
        <li>
          <SolutionCounter counter={"2"} /> Indicator will extinguish within 60 seconds after problem resolution
        </li>
      </ol>
  )
}

export default DatabaseClockProcedure;