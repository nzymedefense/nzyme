import React from "react";
import ApiRoutes from "../../../../../util/ApiRoutes";
import SolutionCounter from "./layout/SolutionCounter";
import Conditional from "./layout/Conditional";
import Indicator from "./layout/Indicator";

function NodeClockProcedure(props) {
  return (
      <ol className="consequence-solution-procedure">
        <li>
          <SolutionCounter counter="1" /> Identify nzyme nodes with unsafe clock drift on the{' '}
          <a href={ApiRoutes.SYSTEM.CLUSTER.INDEX}>nodes overview</a> page
        </li>

        <li>
          <SolutionCounter counter="2" /> <Conditional text="if" /> <Indicator text="DB Clock" />{' '}
          illuminated on console, fix that indicator first
        </li>
        <li>
          <SolutionCounter counter="3" /> <Conditional text="if" /> problem persists
          after <Indicator text="DB Clock" /> extinguishes:
        </li>
        <li className="consequence-solution-sublist">
          <ol>
            <li><SolutionCounter counter="3.1" /> For each nzyme node with clock drift, perform:</li>
            <li className="consequence-solution-sublist">
              <ol>
                <li>
                  <SolutionCounter counter="3.1.1" /> Install and configure <code>ntpd</code> or equivalent time
                  synchronization service
                </li>
                <li className="consequence-solution-or">
                  &mdash; <Conditional text="or" /> &mdash;
                </li>
                <li>
                  <SolutionCounter counter="3.1.2" /> Manually adjust the time and ensure it stays synchronized
                </li>
              </ol>
            </li>
          </ol>
        </li>
        <li>
          <SolutionCounter counter={"4"} /> Indicator will extinguish within 60 seconds after problem resolution
        </li>
      </ol>
  )
}

export default NodeClockProcedure;