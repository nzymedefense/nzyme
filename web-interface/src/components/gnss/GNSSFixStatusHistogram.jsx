import React, {useMemo, useState, useEffect} from "react";
import GenericWidgetLoadingSpinner from "../widgets/GenericWidgetLoadingSpinner";
import Plot from "react-plotly.js";
import moment from "moment";
import Store from "../../util/Store";

const STATE_MAP = { NoFix: 0, Fix2D: 1, Fix3D: 2 };
const STATE_LABELS = ["NoFix", "Fix2D", "Fix3D"];

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

export default function GNSSFixStatusHistogram({fixStatusHistogram}) {
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

  const { x, yLabels, z, textMatrix } = useMemo(() => {
    if (!fixStatusHistogram) {
      return { x: [], yLabels: [], z: [], textMatrix: [] };
    }

    const rows = Object.entries(fixStatusHistogram).map(([ts, vals]) => ({
      t: new Date(ts).getTime(),
      gps: vals.gps,
      glonass: vals.glonass,
      beidou: vals.beidou,
      galileo: vals.galileo
    })).sort((a, b) => a.t - b.t);

    if (rows.length === 0) {
      return { x: [], yLabels: [], z: [], textMatrix: [] };
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

    const constellations = [
      { key: "gps", label: "GPS" },
      { key: "glonass", label: "GLONASS" },
      { key: "beidou", label: "BeiDou" },
      { key: "galileo", label: "Galileo" }
    ];

    const z = constellations.map(c =>
      buckets.map(({samples}) => {
        if (samples.length === 0) return null;
        let maxState = 0;
        for (const s of samples) {
          const v = STATE_MAP[s[c.key]] ?? 0;
          if (v > maxState) maxState = v;
        }
        return maxState;
      })
    );

    const yLabels = constellations.map(c => c.label);
    const x = buckets.map(({bStart}) => moment(bStart + bucketMs / 2).format("YYYY-MM-DD HH:mm"));

    const textMatrix = z.map((row, rowIdx) =>
      row.map((val, colIdx) => {
        const label = yLabels[rowIdx];
        const ts = moment(x[colIdx]).format("MMM D, YYYY, HH:mm");
        const stateLabel = (val == null) ? "No data" : (STATE_LABELS[val] ?? "NoFix");
        return `${label}<br>${stateLabel}<br>${ts}`;
      })
    );

    return { x, yLabels, z, textMatrix };
  }, [fixStatusHistogram]);

  const colors = useMemo(() => {
    if (darkMode) {
      return {
        background: '#1c1c22',
        text: '#c4c4d4',
        lines: '#33333d'
      };
    } else {
      return {
        background: '#f9f9f9',
        text: '#111111',
        lines: '#373737'
      };
    }
  }, [darkMode]);

  if (!fixStatusHistogram) {
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
          y: yLabels,
          z,
          text: textMatrix,
          hoverinfo: "text",
          zmin: 0,
          zmax: 2,
          colorscale: [
            [0.00, "#dc3545"], // NoFix
            [0.33, "#dc3545"],
            [0.34, "#f0ad4e"], // Fix2D
            [0.66, "#f0ad4e"],
            [0.67, "#198754"], // Fix3D
            [1.00, "#198754"]
          ],
          zauto: false,
          connectgaps: false,
          showscale: true,
          colorbar: {
            tickmode: "array",
            tickvals: [0, 1, 2],
            ticktext: ["NoFix", "Fix2D", "Fix3D"],
            thickness: 12,
            tickfont: { color: colors.text }
          },
          xgap: 0,
          ygap: 1
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