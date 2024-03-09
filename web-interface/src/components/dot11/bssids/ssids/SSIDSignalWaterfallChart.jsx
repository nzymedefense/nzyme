import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../../App";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import Dot11Service from "../../../../services/Dot11Service";
import HeatmapWaterfallChart from "../../../widgets/charts/HeatmapWaterfallChart";
import SignalLegendHelper from "../../../widgets/charts/SignalLegendHelper";
import {singleTapSelected} from "../../../../util/Tools";
import TrackDetectorConfigModal from "./TrackDetectorConfigModal";
import WithMinimumRole from "../../../misc/WithMinimumRole";

const dot11Service = new Dot11Service();

function SSIDSignalWaterfallChart(props) {

  const HEIGHT = 450;

  const bssid = props.bssid;
  const ssid = props.ssid;
  const frequency = props.frequency;
  const timeRange = props.timeRange;

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [waterfall, setWaterfall] = useState(null);
  const [revision, setRevision] = useState(0);

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

  const formatTracks = function(data, tracks) {
    const shapes = [];

    const firstDate = new Date(data.y[0]);
    const lastDate = new Date(data.y[data.y.length-1]);

    // Tracks.
    if(tracks) {
      Object.keys(tracks).forEach(function(t) {
        const track = tracks[t];

        // Left.
        shapes.push(
            {
              type: "line",
              visible: true,
              x0: track.min_signal,
              x1: track.min_signal,
              y0: new Date(track.start),
              y1: new Date(track.end),
              line: {
                color: "#ff0000",
                dash: "dashdot",
                width: 2,
              }
            }
        );

        // Right.
        shapes.push(
            {
              type: "line",
              visible: true,
              x0: track.max_signal,
              x1: track.max_signal,
              y0: new Date(track.start),
              y1: new Date(track.end),
              line: {
                color: "#ff0000",
                dash: "dashdot",
                width: 2,
              }
            }
        );

        // Top.
        if (new Date(track.end).getTime() !== lastDate.getTime()) {
          shapes.push(
              {
                type: "line",
                visible: true,
                x0: track.min_signal,
                x1: track.max_signal,
                y0: new Date(track.end),
                y1: new Date(track.end),
                line: {
                  color: "#ff0000",
                  dash: "dashdot",
                  width: 2,
                }
              }
          );
        }

        // Bottom.
        if (new Date(track.start).getTime() !== firstDate.getTime()) {
          shapes.push(
              {
                type: "line",
                visible: true,
                x0: track.min_signal,
                x1: track.max_signal,
                y0: new Date(track.start),
                y1: new Date(track.start),
                line: {
                  color: "#ff0000",
                  dash: "dashdot",
                  width: 2,
                }
              }
          );
        }
      });
    }


    return {shapes: shapes};
  }

  useEffect(() => {
    if (singleTapSelected(selectedTaps)) {
      setWaterfall(null);
      dot11Service.getSSIDOfBSSIDSignalWaterfall(bssid, ssid, frequency, timeRange, selectedTaps, setWaterfall);
    }
  }, [bssid, ssid, frequency, timeRange, selectedTaps, revision])

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
          data={formatData(waterfall)}
          layers={formatTracks(waterfall, waterfall.tracks)} />

        <WithMinimumRole role="ORGADMIN">
          <TrackDetectorConfigModal
              bssid={bssid}
              ssid={ssid}
              frequency={frequency}
              tapUUID={selectedTaps[0]}
              config={waterfall.detector_configuration}
              setRevision={setRevision} />

          <button className="btn btn-sm btn-outline-secondary"
                  data-bs-toggle="modal"
                  data-bs-target="#track-detector-config">
            Configure Track Detector
          </button>
        </WithMinimumRole>
      </React.Fragment>
  )

}

export default SSIDSignalWaterfallChart;