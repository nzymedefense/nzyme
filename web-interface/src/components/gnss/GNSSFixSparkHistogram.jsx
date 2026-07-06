import React from "react";
import Store from "../../util/Store";

const FIX_COLORS = {
  light: { Fix3D: "#2da44e", Fix2D: "#bf8700", none: "#ced4da" },
  dark:  { Fix3D: "#3fb950", Fix2D: "#d29922", none: "#484f58" },
};

function fixColor(value, palette) {
  return palette[value] ?? palette.none;
}

export default function GNSSFixSparkHistogram({histogram, constellation, tickWidth = 3, gap = 1, height = 18}) {
  const palette = FIX_COLORS[Store.get("dark_mode") ? "dark" : "light"];
  const track = Store.get("dark_mode") ? "#2a2a33" : "#e6e6e6";

  const entries = Object.keys(histogram || {})
    .sort((a, b) => new Date(a) - new Date(b))
    .map((ts) => ({ ts, value: histogram[ts]?.[constellation] }));

  if (!entries.length) return <span className="text-muted">n/a</span>;

  const step = tickWidth + gap;
  const width = entries.length * step - gap;

  return (
    <svg
      width={width}
      height={height}
      viewBox={`0 0 ${width} ${height}`}
      style={{ display: "block" }}
      shapeRendering="crispEdges"
    >
      {entries.map(({ ts, value }, i) => (
        <rect
          key={i}
          x={i * step}
          y={0}
          width={tickWidth}
          height={height}
          rx={1}
          fill={value ? fixColor(value, palette) : track}
        >
          <title>{`${new Date(ts).toLocaleString()} — ${value ?? "No fix"}`}</title>
        </rect>
      ))}
    </svg>
  );
}