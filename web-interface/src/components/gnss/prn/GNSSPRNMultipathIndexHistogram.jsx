import React, { useMemo, useState, useEffect } from "react";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import Plot from "react-plotly.js";
import moment from "moment";
import Store from "../../../util/Store";

const MULTIPATH_MAP = { 1: 0, 2: 1, 3: 2 };
const MULTIPATH_LABELS = ["Low", "Medium", "High"];

const NICE_BUCKETS_MIN = [1, 2, 5, 10, 15, 30, 60, 120, 240];

function pickBucketMinutes(spanMs, targetCols = 400) {
  const approxBucketMs = Math.ceil(spanMs / targetCols);
  const approxMin = Math.max(1, Math.round(approxBucketMs / 60000));
  for (const m of NICE_BUCKETS_MIN) {
    if (m >= approxMin) return m;
  }
  return NICE_BUCKETS_MIN[NICE_BUCKETS_MIN.length - 1];
}

function startOfBucket(tsMs, bucketMs) {
  return Math.floor(tsMs / bucketMs) * bucketMs;
}

export default function GNSSPRNMultipathIndexHistogram({ histogram, setTimeRange }) {
  const [darkMode, setDarkMode] = useState(Store.get('dark_mode'));

  useEffect(() => {
    const interval = setInterval(() => {
      const currentDarkMode = Store.get('dark_mode');
      if (currentDarkMode !== darkMode) {
        setDarkMode(currentDarkMode);
      }
    }, 100);

    return () => {
      clearInterval(interval);
    };
  }, [darkMode]);

  const { x, z, textArray } = useMemo(() => {
    if (!histogram) {
      return { x: [], z: [[]], textArray: [[]] };
    }

    const rows = Object.entries(histogram).map(([ts, value]) => ({
      t: new Date(ts).getTime(),
      value: value
    })).sort((a, b) => a.t - b.t);

    if (rows.length === 0) {
      return { x: [], z: [[]], textArray: [[]] };
    }

    const spanMs = rows[rows.length - 1].t - rows[0].t || 1;
    const bucketMinutes = pickBucketMinutes(spanMs, 400);
    const bucketMs = bucketMinutes * 60_000;

    const firstBucket = startOfBucket(rows[0].t, bucketMs);
    const lastBucket = startOfBucket(rows[rows.length - 1].t, bucketMs);
    const bucketStarts = [];
    for (let b = firstBucket; b <= lastBucket; b += bucketMs) {
      bucketStarts.push(b);
    }

    let idx = 0;
    const buckets = bucketStarts.map((bStart) => {
      const bEnd = bStart + bucketMs;
      const samples = [];
      while (idx < rows.length && rows[idx].t < bEnd) {
        if (rows[idx].t >= bStart) samples.push(rows[idx]);
        idx++;
      }
      return { bStart, samples };
    });

    const z = [buckets.map(({ samples }) => {
      if (samples.length === 0) return null;

      let maxIndex = 1;
      for (const s of samples) {
        if (s.value > maxIndex) maxIndex = s.value;
      }

      return MULTIPATH_MAP[maxIndex] ?? 0;
    })];

    const x = buckets.map(({ bStart }) =>
      moment(bStart + bucketMs / 2).format("YYYY-MM-DD HH:mm")
    );

    const textArray = [z[0].map((val, colIdx) => {
      const ts = moment(x[colIdx]).format("MMM D, YYYY, HH:mm");
      const label = (val == null) ? "No data" : (MULTIPATH_LABELS[val] ?? "Low");
      return `Multipath: ${label}<br>${ts}`;
    })];

    return { x, z, textArray };
  }, [histogram]);

  const colors = useMemo(() => {
    if (darkMode) {
      return {
        background: '#262626',
        text: '#f9f9f9',
        lines: '#373737'
      };
    } else {
      return {
        background: '#f9f9f9',
        text: '#111111',
        lines: '#373737'
      };
    }
  }, [darkMode]);

  if (!histogram) {
    return <GenericWidgetLoadingSpinner height={200} />;
  }

  return (
    <Plot
      style={{ width: "100%", height: 200 }}
      useResizeHandler
      data={[
        {
          type: "heatmap",
          x,
          y: ["Multipath"],
          z,
          text: textArray,
          hoverinfo: "text",
          zmin: 0,
          zmax: 2,
          colorscale: [
            [0.00, "#198754"],
            [0.33, "#198754"],
            [0.34, "#f0ad4e"],
            [0.66, "#f0ad4e"],
            [0.67, "#dc3545"],
            [1.00, "#dc3545"]
          ],
          zauto: false,
          connectgaps: false,
          showscale: true,
          colorbar: {
            tickmode: "array",
            tickvals: [0, 1, 2],
            ticktext: ["Low", "Medium", "High"],
            thickness: 12,
            tickfont: { color: colors.text }
          },
          xgap: 0,
          ygap: 0
        }
      ]}
      layout={{
        margin: { l: 80, r: 10, t: 10, b: 40 },
        font: {
          size: 12,
          color: colors.text
        },
        paper_bgcolor: colors.background,
        plot_bgcolor: colors.background,
        yaxis: {
          type: "category",
          tickfont: { size: 12, color: colors.text },
          automargin: true,
          linecolor: colors.lines,
          linewidth: 1
        },
        xaxis: {
          type: "date",
          fixedrange: true,
          linecolor: colors.lines,
          linewidth: 1,
          tickfont: { color: colors.text }
        }
      }}
      config={{
        displayModeBar: false,
        responsive: true,
        scrollZoom: false
      }}
    />
  );
}