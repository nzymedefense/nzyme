import React from "react";
import SolutionCounter from "./layout/SolutionCounter";
import Conditional from "./layout/Conditional";

function TapDropProcedure(props) {
  return (
      <ol className="consequence-solution-procedure">
        <li>
          <SolutionCounter counter="1" /> Identify taps with captures dropping packets using the tap details pages.
        </li>
        <li><SolutionCounter counter="2" /> <Conditional text="For each" /> tap with captures dropping packets, perform:</li>
        <li className="consequence-solution-sublist">
          <ol>
            <li>
              <SolutionCounter counter="2.1" /> Evaluate if the machine the tap is running on
              might need more resources. If it is using excessive amounts of CPU or RAM, the issue is not the buffer size
              but insufficient computing power.
            </li>
            <li>
              <SolutionCounter counter="2.2" /> <Conditional text="If" /> capture reports dropped packets on the interface,
              your system might not be able to handle the load of incoming data. Try increasing resources or system
              interface buffers. (this is specific to your operating system)
            </li>
            <li>
              <SolutionCounter counter="2.3" /> <Conditional text="If" /> capture reports dropped packets in the buffer,
              the buffer might be too small. Try increasing the capture buffer size in the tap configuration file.
            </li>
            <li>
              <SolutionCounter counter="2.3" /> Restart tap to reset dropped package counters
            </li>
          </ol>
        </li>
        <li>
          <SolutionCounter counter={"3"} /> Indicator will extinguish within 60 seconds after problem resolution
        </li>
      </ol>
  )
}

export default TapDropProcedure;