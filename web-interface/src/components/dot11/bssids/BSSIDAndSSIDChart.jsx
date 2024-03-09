import React, {useContext, useEffect, useState} from "react";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import SimpleLineChart from "../../widgets/charts/SimpleLineChart";

const dot11Service = new Dot11Service();

function BSSIDAndSSIDChart(props) {

  const parameter = props.parameter;
  const timeRange = props.timeRange;

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [histogram, setHistogram] = useState(null);

  useEffect(() => {
    setHistogram(null);
    dot11Service.getBSSIDAndSSIDHistogram(timeRange, selectedTaps, setHistogram);
  }, [selectedTaps, timeRange])

  const formatData = function(data) {
    const result = {}

    Object.keys(data).sort().forEach(function(key) {
      result[key] = data[key][parameter];
    })

    return result
  }

  if (!histogram) {
    return <LoadingSpinner />
  }

  return <SimpleLineChart
          height={200}
          lineWidth={1}
          customMarginBottom={35}
          data={formatData(histogram.values)}
      />

}

export default BSSIDAndSSIDChart;