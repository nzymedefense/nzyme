import React from "react";
import SolutionCounter from "./layout/SolutionCounter";
import Conditional from "./layout/Conditional";
import Indicator from "./layout/Indicator";

function NodeOfflineProcedure(props) {
  return (
      <ol className="consequence-solution-procedure">
        <li>
          <SolutionCounter counter="1" /> Investigate why nodes went offline and restart them as required. Consider
          network connectivity issues. <Conditional text="if" /> <Indicator text="DB Clock" /> or
          {' '}<Indicator text="Node Clock" /> illuminated on console, fix those indicators first.
        </li>
        <li className="consequence-solution-or">
          &mdash; <Conditional text="or" /> &mdash;
        </li>
        <li>
          <SolutionCounter counter="2" /> If a node is no longer supposed to run, delete it using it&apos;s details page.
        </li>
        <li>
          <SolutionCounter counter="3" /> An offline node will no longer trigger this indicator and also disappear
          from all node management pages after being offline for 24 hours. This indicator will extinguish within
          60 seconds of problem resolution.
        </li>
      </ol>
  )
}

export default NodeOfflineProcedure;