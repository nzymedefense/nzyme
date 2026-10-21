import React from "react";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import SimpleScatterChart from "../../widgets/charts/SimpleScatterChart";

export default function ActiveBeaconsHistogram({histogram, timeRange, setTimeRange}) {

  if (!histogram) {
    return <GenericWidgetLoadingSpinner height={200} />;
  }

  const series = {name: "Active beacons", x: [], y: [],
    hovertemplate: "%{x}<br>%{y:,} active beacons<extra></extra>"};

  Object.keys(histogram).sort().forEach((key) => {
    series.x.push(new Date(key));
    series.y.push(histogram[key]);
  });

  if (series.x.length === 0) {
    return <div className="alert alert-info mb-0">No beacon activity in the selected time range.</div>
  }

  return (
    <SimpleScatterChart
      height={200}
      series={[series]}
      yaxistitle="Active beacons"
      setTimeRange={setTimeRange}
      timeRange={timeRange}
      doNotPersistTimeRange={true} />
  )

}
