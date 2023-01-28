import React from "react";
import SimpleRangeIndicator from "../../../../charts/SimpleRangeIndicator";

function CpuLoadChart(props) {

  return <SimpleRangeIndicator value={Math.round(props.load)} range={[0,100]} suffix="%" />

}

export default CpuLoadChart