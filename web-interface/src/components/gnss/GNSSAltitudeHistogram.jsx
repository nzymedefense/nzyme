import React from "react";
import GenericWidgetLoadingSpinner from "../widgets/GenericWidgetLoadingSpinner";
import MultiLineChart from "../widgets/charts/MultiLineChart";

export default function GNSSAltitudeHistogram(props) {

  const histogram = props.histogram;
  const setTimeRange = props.setTimeRange;

  const formatData = data => {
    const gps = {};
    const glonass = {};
    const beidou = {};
    const galileo = {};

    // Sort timestamps so the x‑axis is in order
    Object.keys(data).sort().forEach(timestamp => {
      const row = data[timestamp] || {};
      gps[timestamp] = row["gps"] ?? 0;
      glonass[timestamp] = row["glonass"] ?? 0;
      beidou[timestamp] = row["beidou"] ?? 0;
      galileo[timestamp] = row["galileo"] ?? 0;
    });

    return {
      gps: gps,
      glonass: glonass,
      beidou: beidou,
      galileo: galileo
    };
  };

  if (!histogram) {
    return <GenericWidgetLoadingSpinner height={200} />
  }

  return <MultiLineChart
    height={200}
    lineWidth={1}
    customMarginBottom={35}
    customMarginLeft={60}
    ticksuffix="m"
    scattermode="markers"
    seriesNames={{gps: "GPS", glonass: "GLONASS",  beidou: "BeiDou", galileo: "Galileo" }}
    data={formatData(histogram)}
    setTimeRange={setTimeRange}
  />

}