import React from "react";
import numeral from "numeral";
import moment from "moment";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import SimpleScatterChart from "../../widgets/charts/SimpleScatterChart";

export default function BeaconCheckinScatterCharts({points, timeRange, setTimeRange}) {

  if (!points) {
    return <GenericWidgetLoadingSpinner height={500} />;
  }

  const bySource = {};
  points.points.forEach((p) => {
    if (!bySource[p.source_uuid]) {
      bySource[p.source_uuid] = {name: p.source_address, points: []};
    }
    bySource[p.source_uuid].points.push(p);
  });

  const intervalSeries = [];
  const bytesSeries = [];
  Object.keys(bySource).sort((a, b) => bySource[a].name.localeCompare(bySource[b].name)).forEach((key) => {
    const s = bySource[key];

    const interval = {name: s.name, x: [], y: [], text: [],
      hovertemplate: "%{text}<br>%{y:,.1f} seconds since previous check-in<extra></extra>"};
    const bytes = {name: s.name, x: [], y: [], text: [],
      hovertemplate: "%{text}<br>%{y:,} bytes<extra></extra>"};

    s.points.forEach((p) => {
      const label = s.name + " at " + moment(p.timestamp).format();

      if (p.interval_seconds !== null && p.interval_seconds !== undefined) {
        interval.x.push(new Date(p.timestamp));
        interval.y.push(p.interval_seconds);
        interval.text.push(label);
      }

      if (p.bytes_total !== null && p.bytes_total !== undefined) {
        bytes.x.push(new Date(p.timestamp));
        bytes.y.push(p.bytes_total);
        bytes.text.push(label);
      }
    });

    intervalSeries.push(interval);
    bytesSeries.push(bytes);
  });

  const empty = (series) => series.every((s) => s.x.length === 0);

  return (
    <React.Fragment>
      <h6>Interval (Seconds)</h6>
      {empty(intervalSeries) ?
        <div className="alert alert-info mb-0">No check-ins with a known interval in the selected time range.</div>
        : <SimpleScatterChart height={250}
                              series={intervalSeries}
                              yaxistitle="Seconds since previous check-in"
                              timeRange={timeRange}
                              setTimeRange={setTimeRange}
                              doNotPersistTimeRange={true} />}

      <h6 className="mt-3">Size (Bytes)</h6>
      {empty(bytesSeries) ?
        <div className="alert alert-info mb-0">
          No check-ins with a known size in the selected time range. Sizes are recorded for TCP, TLS and SOCKS
          beacons.
        </div>
        : <SimpleScatterChart height={250}
                              series={bytesSeries}
                              yaxistitle="Bytes transferred"
                              timeRange={timeRange}
                              setTimeRange={setTimeRange}
                              doNotPersistTimeRange={true} />}

      <p className="text-muted mt-2 mb-0">
        {points.truncated ?
          " Only the most recent " + numeral(points.limit).format("0,0") + " check-ins in this time range are plotted."
          : null}
      </p>
    </React.Fragment>
  )

}
