import React, {useMemo} from "react";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import Plot from "react-plotly.js";

export default function GNSSPRNSkyChart({elevationHistogram, azimuthHistogram}) {

  if (!elevationHistogram || !azimuthHistogram) {
    return <GenericWidgetLoadingSpinner height={510} />;
  }

  // Build matched samples by timestamp.
  const samples = useMemo(() => {
    // Intersect timestamps present in both histograms
    const elevKeys = new Set(Object.keys(elevationHistogram || {}));
    const azKeys = new Set(Object.keys(azimuthHistogram || {}));
    const keys = [...elevKeys].filter(k => azKeys.has(k));

    // Sort by time
    keys.sort((a, b) => new Date(a) - new Date(b));

    const pts = [];
    for (const k of keys) {
      const elev = Number(elevationHistogram[k]);
      const az = Number(azimuthHistogram[k]);
      if (Number.isFinite(elev) && Number.isFinite(az)) {
        // Clamp to valid ranges
        const elevation = Math.max(0, Math.min(90, elev));
        const azimuth = ((az % 360) + 360) % 360; // normalize 0..360
        pts.push({ t: k, elevation, azimuth, r: 90 - elevation, theta: azimuth });
      }
    }
    return pts;
  }, [elevationHistogram, azimuthHistogram]);

  if (!samples.length) {
    return <div className="text-muted p-3">No matching timestamps to plot.</div>;
  }

  // Split into arrays for Plotly
  const r = samples.map(p => p.r);
  const theta = samples.map(p => p.theta);
  const text = samples.map(
    p => `${p.t}<br>Azimuth: ${p.azimuth.toFixed(1)}°<br>Elevation: ${p.elevation.toFixed(1)}°`
  );

  // Highlight the most recent point
  const last = samples[samples.length - 1];

  return (
    <Plot
      data={[
        {
          type: "scatterpolar",
          mode: "lines+markers",
          r,
          theta,
          text,
          hovertemplate: "%{text}<extra></extra>",
          marker: { size: 6 },
          line: { shape: "linear", width: 1 },
          name: "Track"
        },
        {
          type: "scatterpolar",
          mode: "markers",
          r: [last.r],
          theta: [last.theta],
          text: [`Latest: ${last.t}<br>Az: ${last.azimuth.toFixed(1)}° / El: ${last.elevation.toFixed(1)}°`],
          hovertemplate: "%{text}<extra></extra>",
          marker: { size: 10, symbol: "circle-open-dot" },
          name: "Latest"
        }
      ]}
      layout={{
        height: 510,
        margin: { l: 30, r: 30, t: 30, b: 30 },
        showlegend: false,
        polar: {
          // Theta: azimuth degrees, 0° at north (top), increasing clockwise.
          angularaxis: {
            direction: "clockwise",
            rotation: 0, // 0° at north
            tickmode: "array",
            tickvals: [0, 90, 180, 270],
            ticktext: ["N", "E", "S", "W"]
          },
          radialaxis: {
            range: [90, 0],   // 0 at center (zenith), 90 at edge (horizon)
            tickvals: [0, 30, 60, 90],
            ticktext: ["90°", "60°", "30°", "0°"], // label with elevation for intuition
            showline: false,
            gridcolor: "#eee"
          }
        }
      }}
      config={{ displayModeBar: false, responsive: true }}
      style={{ width: "100%" }}
    />
  );

}