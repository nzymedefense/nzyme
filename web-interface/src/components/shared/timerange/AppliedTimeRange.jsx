import React from "react";
import TimeRangeDescription from "./TimeRangeDescription";

function AppliedTimeRange(props) {

  const timeRange = props.timeRange;

  return (
      <span className="applied-timerange">
        <i className="applied-timerange-icon fa-regular fa-clock"></i> <TimeRangeDescription timeRange={timeRange}/>
      </span>
  )

}

export default AppliedTimeRange;