import React from "react";
import SimpleRangeIndicator from "../../../../charts/SimpleRangeIndicator";

function MemoryLoadIndicator(props) {

  return <SimpleRangeIndicator value={props.used} range={[0, props.total]} valueformat=".3s" tickformat=".1s" />

}

export default MemoryLoadIndicator
