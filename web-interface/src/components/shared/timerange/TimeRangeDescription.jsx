import React from "react";

function TimeRangeDescription(props) {

  const range = props.timeRange;

  if (range.type === "relative") {
    switch (range.minutes) {
      case 1:     return "Last 1 Minute";
      case 5:     return "Last 5 Minutes";
      case 15:    return "Last 15 Minutes";
      case 30:    return "Last 30 Minutes";
      case 60:    return "Last 60 Minutes";
      case 240:   return "Last 4 Hours";
      case 1440:  return "Last 24 Hours";
      case 2880:  return "Last 2 Days";
      case 10080: return "Last 7 Days";
    }
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
    return "From [" + range.from + "] to [" + range.to + "]";
  }

  return "Unknown time range type: " + JSON.stringify(range);

}

export default TimeRangeDescription;