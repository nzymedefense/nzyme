import React from "react";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import SimpleLineChart from "../../widgets/charts/SimpleLineChart";

export default function GNSSPRNSnoHistogram({histogram, setTimeRange}) {

  if (!histogram) {
    return <GenericWidgetLoadingSpinner height={200} />;
  }

  const formatData = function(data) {
    const result = {}

    Object.keys(data).sort().forEach(function(key) {
      result[key] = data[key];
    })

    return result
  }

  return (
    <React.Fragment>
      <SimpleLineChart
        height={200}
        lineWidth={1}
        scattermode="markers"
        setTimeRange={setTimeRange}
        data={formatData(histogram)} />
    </React.Fragment>
  )

}