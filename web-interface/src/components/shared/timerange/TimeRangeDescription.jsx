import React from "react";
import moment from "moment";

function TimeRangeDescription(props) {

  const range = props.timeRange;

  if (range.type === "relative") {
    return range.name;
  }

  if (range.type === "named") {
    switch (range.name) {
      case "today": return "Today";
      case "yesterday": return "Yesterday";
      case "week_to_date": return "Week to date"
      case "month_to_date": return "Month to date";
      case "all_time": return "All Time";
    }
  }

  if (range.type === "absolute") {
    return <span>
      <i>{moment(range.from).format()}</i>&nbsp;&nbsp;
      <i className="fa-solid fa-arrow-right-long text-muted"></i>&nbsp;&nbsp;
      <i>{moment(range.to).format()}</i>
    </span>
  }

  return "Unknown time range type: " + JSON.stringify(range);

}

export default TimeRangeDescription;