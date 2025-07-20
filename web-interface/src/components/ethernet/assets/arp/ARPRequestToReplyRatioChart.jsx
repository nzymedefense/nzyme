import React from "react";
import GenericWidgetLoadingSpinner from "../../../widgets/GenericWidgetLoadingSpinner";
import SimpleLineChart from "../../../widgets/charts/SimpleLineChart";

export default function ARPRequestToReplyRatioChart(props) {

  const statistics = props.statistics;
  const setTimeRange = props.setTimeRange;

  if (!statistics) {
    return <GenericWidgetLoadingSpinner height={200} />
  }

  const formatData = function(data) {
    const result = {}

    Object.keys(data).sort().forEach(function(key) {
      result[key] = data[key]["request_to_reply_ratio"];
    })

    return result
  }

  return <SimpleLineChart
      height={200}
      lineWidth={1}
      scattermode="markers"
      data={formatData(statistics)}
      setTimeRange={setTimeRange}
  />

}