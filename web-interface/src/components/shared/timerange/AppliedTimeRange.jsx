import React from "react";
import TimeRangeDescription from "./TimeRangeDescription";

function AppliedTimeRange(props) {

  const timeRange = props.timeRange;

  return (
      <div className="row mb-3">
        <div className="col-12">
          Time Range: <TimeRangeDescription timeRange={timeRange} />
        </div>
      </div>
  )

}

export default AppliedTimeRange;