import React, {useState} from "react";
import GenericWidgetLoadingSpinner from "../widgets/GenericWidgetLoadingSpinner";
import SimpleLineChart from "../widgets/charts/SimpleLineChart";

export default  function GNSSTimeDeviationHistogram(props) {

  const histogram = props.histogram;
  const setTimeRange = props.setTimeRange;

  const formatData = function(data) {
    const result = {}

    Object.keys(data).sort().forEach(function(key) {
      result[key] = data[key];
    })

    return result
  }

  if (!histogram) {
    return <GenericWidgetLoadingSpinner height={200} />
  }

  return <SimpleLineChart
    height={200}
    lineWidth={1}
    customMarginBottom={35}
    setTimeRange={setTimeRange}
    data={formatData(histogram)}
  />

}