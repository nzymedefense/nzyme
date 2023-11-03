import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import SimpleLineChart from "../../widgets/charts/SimpleLineChart";

function ClientHistogram(props) {

  const histograms = props.histograms;
  const param = props.param;

  const formatData = function(data) {
    const result = {}

    Object.keys(data[param]).sort().forEach(function(key) {
      result[key] = data[param][key]["client_count"];
    })

    return result
  }

  if (!histograms) {
    return <LoadingSpinner />
  }

  return <SimpleLineChart
      height={200}
      lineWidth={1}
      customMarginBottom={35}
      data={formatData(histograms)}
  />

}

export default ClientHistogram;