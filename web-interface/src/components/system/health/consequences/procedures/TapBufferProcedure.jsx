import React from "react";
import SolutionCounter from "./layout/SolutionCounter";
import Conditional from "./layout/Conditional";

function TapBufferProcedure(props) {
  return (
      <ol className="consequence-solution-procedure">
        <li>
          <SolutionCounter counter="1" /> Identify taps with channels that have buffers filled more than 75% using the tap details pages.
        </li>
        <li><SolutionCounter counter="2" /> <Conditional text="For each" /> tap with dangerously full channel buffers, perform:</li>
        <li className="consequence-solution-sublist">
          <ol>
            <li>
              <SolutionCounter counter="2.1" /> Evaluate if the machine the tap is running on might need more resources. If it
              is using excessive amounts of CPU or RAM, the issue is not the buffer size but insufficient computing power.
            </li>
            <li>
              <SolutionCounter counter="2.2" /> Increase channel buffer size in tap configuration file.
            </li>
          </ol>
        </li>
        <li>
          <SolutionCounter counter={"3"} /> Indicator will extinguish within 60 seconds after problem resolution
        </li>
      </ol>
  )
}

export default TapBufferProcedure;