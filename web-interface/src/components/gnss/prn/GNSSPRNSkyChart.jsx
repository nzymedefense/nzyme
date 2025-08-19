import React, {useMemo} from "react";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import Plot from "react-plotly.js";
import Store from "../../../util/Store";

export default function GNSSPRNSkyChart({ elevationHistogram, azimuthHistogram, snrHistogram }) {
  if (!elevationHistogram || !azimuthHistogram || !snrHistogram) {
    return <GenericWidgetLoadingSpinner height={510} />;
  }

  // Match timestamps across elevation, azimuth, and snr
  const samples = useMemo(() => {
    const elevKeys = new Set(Object.keys(elevationHistogram || {}));
    const azKeys = new Set(Object.keys(azimuthHistogram || {}));
    const snrKeys = new Set(Object.keys(snrHistogram || {}));
    const keys = [...elevKeys].filter(k => azKeys.has(k) && snrKeys.has(k));

    keys.sort((a, b) => new Date(a) - new Date(b));

    const pts = [];
    for (const k of keys) {
      const elev = Number(elevationHistogram[k]);
      const az = Number(azimuthHistogram[k]);
      const snr = Number(snrHistogram[k]);
      if (Number.isFinite(elev) && Number.isFinite(az) && Number.isFinite(snr)) {
        const elevation = Math.max(0, Math.min(90, elev));
        const azimuth = ((az % 360) + 360) % 360;
        pts.push({
          t: k,
          elevation,
          azimuth,
          snr,
          r: 90 - elevation,
          theta: azimuth
        });
      }
    }
    return pts;
  }, [elevationHistogram, azimuthHistogram, snrHistogram]);

  if (!samples.length) {
    return <div className="text-muted p-3">No matching timestamps to plot.</div>;
  }

  const r = samples.map(p => p.r);
  const theta = samples.map(p => p.theta);
  const snrValues = samples.map(p => p.snr);
  const text = samples.map(
      p => `${p.t}<br>Azimuth: ${p.azimuth.toFixed(1)}°<br>Elevation: ${p.elevation.toFixed(1)}°<br>SNR: ${p.snr}`
  );

  const last = samples[samples.length - 1];

  const colors = {}
  if (Store.get('dark_mode')) {
    colors.background = '#262626'
    colors.text = '#f9f9f9'
    colors.lines = '#373737'
    colors.grid = '#373737'
  } else {
    colors.background = '#f9f9f9'
    colors.text = '#111111'
    colors.lines = '#373737'
    colors.grid = '#e6e6e6'
  }

  return (
      <Plot
          data={[
            {
              type: "scatterpolar",
              mode: "markers",
              r,
              theta,
              text,
              hovertemplate: "%{text}<extra></extra>",
              marker: {
                size: 8,
                color: snrValues,
                colorscale: [
                  [0, "red"],
                  [0.5, "yellow"],
                  [1, "green"]
                ],
                cmin: 0,
                cmax: 50,
                colorbar: {
                  title: "SNR",
                  titleside: "right"
                }
              },
              name: "Samples"
            },
            {
              type: "scatterpolar",
              mode: "markers",
              r: [last.r],
              theta: [last.theta],
              text: [
                `Latest: ${last.t}<br>Az: ${last.azimuth.toFixed(1)}° / El: ${last.elevation.toFixed(
                    1
                )}°<br>SNR: ${last.snr}`
              ],
              hovertemplate: "%{text}<extra></extra>",
              marker: {
                size: 12,
                symbol: "circle-open-dot",
                line: { color: colors.lines, width: 2 }
              },
              name: "Latest"
            }
          ]}
          layout={{
            height: 510,
            margin: { l: 30, r: 30, t: 30, b: 30 },
            paper_bgcolor: colors.background,
            plot_bgcolor: colors.background,
            showlegend: false,
            polar: {
              bgcolor: colors.background,
              angularaxis: {
                color: colors.text,
                direction: "clockwise",
                rotation: 90, // North at top
                tickmode: "array",
                tickvals: [0, 90, 180, 270],
                ticktext: ["N", "E", "S", "W"]
              },
              radialaxis: {
                color: colors.text,
                range: [90, 0],
                tickvals: [0, 30, 60, 90],
                ticktext: ["90°", "60°", "30°", "0°"]
              }
            }
          }}
          config={{ displayModeBar: false, responsive: true }}
          style={{ width: "100%" }}
      />
  );
}