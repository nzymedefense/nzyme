import React, {useContext} from "react";
import {TapContext} from "../../../App";
import {singleTapSelected} from "../../../util/Tools";
import SimpleLineChart from "../../widgets/charts/SimpleLineChart";

function ClientSignalStrengthChart(props) {

  const data = props.data;

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const formatData = function(data) {
    const result = {}

    data.forEach(function(key) {
      result[key["timestamp"]] = key["signal_strength"];
    })

    return result
  }

  if (!singleTapSelected(selectedTaps)) {
    return (
        <div className="alert alert-info mb-0">
          This chart appears only when a single tap is selected. Feeding data from multiple taps would not yield
          meaningful results.
        </div>
    )
  }

  if (!data || data.length === 0) {
    return (
        <div className="alert alert-info mb-0">
          No data recorded in requested time frame.
        </div>
    )
  }

  return <SimpleLineChart
      height={200}
      lineWidth={1}
      customMarginBottom={35}
      customMarginRight={20}
      scattermode="markers"
      data={formatData(data)} />

}

export default ClientSignalStrengthChart;