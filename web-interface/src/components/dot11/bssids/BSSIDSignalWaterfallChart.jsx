import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Dot11Service from "../../../services/Dot11Service";
import HeatmapWaterfallChart from "../../widgets/charts/HeatmapWaterfallChart";
import SignalLegendHelper from "../../widgets/charts/SignalLegendHelper";
import {singleTapSelected} from "../../../util/Tools";

const dot11Service = new Dot11Service();

function BSSIDSignalWaterfallChart(props) {

  const HEIGHT = 450;

  const bssid = props.bssid;
  const minutes = props.minutes;

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [waterfall, setWaterfall] = useState(null);

  const formatData = function(data) {
    const yDates = [];

    Object.keys(data.y).forEach(function(point) {
      yDates.push(new Date(data.y[point]));
    });

    return {
      "z": data.z,
      "x": data.x,
      "y": yDates
    };
  }

  useEffect(() => {
    if (singleTapSelected(selectedTaps)) {
      setWaterfall(null);
      dot11Service.getBSSIDSignalWaterfall(bssid, minutes, selectedTaps, setWaterfall);
    }
  }, [bssid, minutes, selectedTaps])

  if (!singleTapSelected(selectedTaps)) {
    return (
        <div className="alert alert-info mb-0">
          This chart appears only when a single tap is selected. Feeding data from multiple taps would not yield
          meaningful results.
        </div>
    )
  }

  if (!waterfall) {
    return <div style={{height: HEIGHT}}><LoadingSpinner /></div>
  }

  return (
      <React.Fragment>
        <HeatmapWaterfallChart
            height={HEIGHT}
            xaxistitle="Signal Strength (dBm)"
            yaxistitle="Time"
            hovertemplate="Signal Strength: %{x} dBm, %{z} frames at %{y}<extra></extra>"
            annotations={SignalLegendHelper.DEFAULT}
            data={formatData(waterfall)} />
      </React.Fragment>
  )

}

export default BSSIDSignalWaterfallChart;