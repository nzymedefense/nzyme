import React, {useEffect, useMemo, useRef, useState} from "react";
import * as d3 from "d3";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import Store from "../../../util/Store";

export default function GNSSPRNSkyPlot({ elevationHistogram, azimuthHistogram, snoHistogram, elevationMask }) {
  const containerRef = useRef(null);
  const tooltipRef = useRef(null);
  const [darkMode, setDarkMode] = useState(Store.get('dark_mode'));

  // Check for theme changes periodically.
  useEffect(() => {
    const checkDarkMode = () => {
      const currentDarkMode = Store.get('dark_mode');
      if (currentDarkMode !== darkMode) {
        setDarkMode(currentDarkMode);
      }
    };

    const interval = setInterval(checkDarkMode, 100);
    return () => clearInterval(interval);
  }, [darkMode]);

  // Normalize elevationMask input.
  const elevationMaskArray = useMemo(() => {
    if (elevationMask == null) return [];

    // already array
    if (Array.isArray(elevationMask)) return elevationMask;

    // common "object keyed by azimuth bucket" case
    if (typeof elevationMask === "object") {
      return Object.values(elevationMask || {})
        .filter(Boolean)
        .map((b) => ({
          azimuth_bucket: b.azimuth_bucket ?? b.azimuthBucket ?? null,
          skyline_elevation: b.skyline_elevation ?? b.skylineElevation ?? null,
          skyline_elevation_best_effort:
            b.skyline_elevation_best_effort ?? b.skylineElevationBestEffort ?? null,
          used_fallback: b.used_fallback ?? b.usedFallback ?? false,
          sample_count: b.sample_count ?? b.sampleCount ?? null,
        }))
        .filter((b) => b.azimuth_bucket != null);
    }

    return [];
  }, [elevationMask]);

  // Match timestamps across elevation, azimuth, and SNO.
  const samples = useMemo(() => {
    if (!elevationHistogram || !azimuthHistogram || !snoHistogram) return [];

    const elevKeys = new Set(Object.keys(elevationHistogram || {}));
    const azKeys = new Set(Object.keys(azimuthHistogram || {}));
    const snoKeys = new Set(Object.keys(snoHistogram || {}));
    const keys = [...elevKeys].filter(k => azKeys.has(k) && snoKeys.has(k));

    keys.sort((a, b) => new Date(a) - new Date(b));

    const pts = [];
    for (const k of keys) {
      const elev = Number(elevationHistogram[k]);
      const az = Number(azimuthHistogram[k]);
      const sno = Number(snoHistogram[k]);

      if (Number.isFinite(elev) && Number.isFinite(az) && Number.isFinite(sno)) {
        const elevation = Math.max(0, Math.min(90, elev));
        const azimuth = ((az % 360) + 360) % 360;
        pts.push({
          t: k,
          elevation,
          azimuth,
          sno: sno,
        });
      }
    }
    return pts;
  }, [elevationHistogram, azimuthHistogram, snoHistogram]);

  // Prepare mask.
  const mask = useMemo(() => {
    const binSize = 5;

    // Map by bucket.
    const byAz = new Map();
    for (const b of elevationMaskArray) {
      const az = Number(b.azimuth_bucket);
      if (!Number.isFinite(az)) continue;

      const skyline = b.skyline_elevation;
      const best = b.skyline_elevation_best_effort;

      const val =
        skyline != null ? Number(skyline) : best != null ? Number(best) : null;

      byAz.set(az, {
        az,
        valueDeg: Number.isFinite(val) ? val : null,
        usedFallback: !!b.used_fallback,
        sampleCount: b.sample_count != null ? Number(b.sample_count) : null,
      });
    }

    // Build a full 0..355 series.
    const series = [];
    for (let az = 0; az < 360; az += binSize) {
      const e = byAz.get(az);
      series.push(
        e ?? {
          az,
          valueDeg: null,
          usedFallback: false,
          sampleCount: null,
        }
      );
    }

    // Clamp elevations to [0..90].
    for (const s of series) {
      if (s.valueDeg != null) {
        s.valueDeg = Math.max(0, Math.min(90, s.valueDeg));
      }
    }

    return { binSize, series };
  }, [elevationMaskArray]);

  useEffect(() => {
    const container = containerRef.current;
    const tooltip = tooltipRef.current;
    if (!container) return;

    // If loading, clear any existing SVG.
    if (!elevationHistogram || !azimuthHistogram || !snoHistogram) {
      d3.select(container).selectAll("svg").remove();
      return;
    }

    if (samples.length === 0) {
      d3.select(container).selectAll("svg").remove();
      return;
    }

    const renderChart = () => {
      d3.select(container).selectAll("svg").remove();

      const w = container.clientWidth || 800;
      const h = 510;
      const size = Math.min(w, h);
      const margin = 34;
      const R = size / 2 - margin;

      const svg = d3.select(container).append("svg").attr("width", w).attr("height", h);
      const g = svg.append("g").attr("transform", `translate(${w / 2},${h / 2})`);

      // Colors.
      const colors = {};
      if (Store.get('dark_mode')) {
        colors.background = '#262626';
        colors.grid = 'rgba(255,255,255,0.10)';
        colors.ring = 'rgba(255,255,255,0.08)';
        colors.stroke = 'rgba(255,255,255,0.12)';
        colors.label = 'rgba(255,255,255,0.55)';
        colors.labelStrong = 'rgba(255,255,255,0.70)';
        colors.skyGradStart = '#262626';
        colors.skyGradMid = '#2d2d2d';
        colors.skyGradEnd = '#333333';
        colors.obstructionFill = 'rgba(0,0,0,0.3)';
        colors.obstructionStroke = 'rgba(255,255,255,0.15)';
        colors.skylineBoundary = 'rgba(255,255,255,0.3)';
        colors.fallbackStroke = 'rgba(248,113,113,0.75)';
        colors.pointStroke = '#262626';
        colors.latestPointRing = 'rgba(255,255,255,0.3)';
      } else {
        colors.background = '#f9f9f9';
        colors.grid = 'rgba(0,0,0,0.10)';
        colors.ring = 'rgba(0,0,0,0.08)';
        colors.stroke = 'rgba(0,0,0,0.12)';
        colors.label = 'rgba(0,0,0,0.55)';
        colors.labelStrong = 'rgba(0,0,0,0.70)';
        colors.skyGradStart = '#ffffff';
        colors.skyGradMid = '#f3f4f6';
        colors.skyGradEnd = '#eef2f7';
        colors.obstructionFill = 'rgba(0,0,0,0.07)';
        colors.obstructionStroke = 'rgba(0,0,0,0.10)';
        colors.skylineBoundary = 'rgba(0,0,0,0.22)';
        colors.fallbackStroke = 'rgba(220,38,38,0.65)';
        colors.pointStroke = '#fff';
        colors.latestPointRing = 'rgba(0,0,0,0.18)';
      }

      // 0 degrees at North, clockwise.
      const toRad = (deg) => ((deg - 90) * Math.PI) / 180;

      const project = (az, el) => {
        const rr = ((90 - el) / 90) * R;
        const a = toRad(az);
        return [Math.cos(a) * rr, Math.sin(a) * rr];
      };

      // Sky gradient.
      const defs = svg.append("defs");
      const skyGrad = defs
        .append("radialGradient")
        .attr("id", "skyGrad")
        .attr("cx", "50%")
        .attr("cy", "50%")
        .attr("r", "60%");
      skyGrad.append("stop").attr("offset", "0%").attr("stop-color", colors.skyGradStart);
      skyGrad.append("stop").attr("offset", "70%").attr("stop-color", colors.skyGradMid);
      skyGrad.append("stop").attr("offset", "100%").attr("stop-color", colors.skyGradEnd);

      g.append("circle")
        .attr("r", R)
        .attr("fill", "url(#skyGrad)")
        .attr("stroke", colors.stroke)
        .attr("stroke-width", 1);

      // Elevation rings.
      const elevTicks = [15, 30, 45, 60, 75];
      g.append("g")
        .selectAll("circle")
        .data(elevTicks)
        .enter()
        .append("circle")
        .attr("r", (d) => ((90 - d) / 90) * R)
        .attr("fill", "none")
        .attr("stroke", colors.ring)
        .attr("stroke-width", 1);

      g.append("g")
        .selectAll("text")
        .data(elevTicks)
        .enter()
        .append("text")
        .attr("x", 0)
        .attr("y", (d) => -(((90 - d) / 90) * R))
        .attr("dy", "-0.35em")
        .attr("text-anchor", "middle")
        .attr("fill", colors.label)
        .attr("font-size", 11)
        .text((d) => `${d}°`);

      // Azimuth spokes every 45 degrees.
      const azTicks = d3.range(0, 360, 45);
      g.append("g")
        .selectAll("line")
        .data(azTicks)
        .enter()
        .append("line")
        .attr("x1", 0)
        .attr("y1", 0)
        .attr("x2", (d) => Math.cos(toRad(d)) * R)
        .attr("y2", (d) => Math.sin(toRad(d)) * R)
        .attr("stroke", colors.grid)
        .attr("stroke-width", 1);

      // Cardinal labels.
      const card = [
        { label: "N", az: 0 },
        { label: "E", az: 90 },
        { label: "S", az: 180 },
        { label: "W", az: 270 },
      ];
      g.append("g")
        .selectAll("text")
        .data(card)
        .enter()
        .append("text")
        .attr("x", (d) => Math.cos(toRad(d.az)) * (R + 18))
        .attr("y", (d) => Math.sin(toRad(d.az)) * (R + 18))
        .attr("text-anchor", "middle")
        .attr("dominant-baseline", "middle")
        .attr("fill", colors.labelStrong)
        .attr("font-size", 12)
        .attr("font-weight", 650)
        .text((d) => d.label);

      // Degree labels at 45 degrees intervals (skip cardinals).
      const degrees = [45, 135, 225, 315];
      g.append("g")
        .selectAll("text")
        .data(degrees)
        .enter()
        .append("text")
        .attr("x", (d) => Math.cos(toRad(d)) * (R + 18))
        .attr("y", (d) => Math.sin(toRad(d)) * (R + 18))
        .attr("text-anchor", "middle")
        .attr("dominant-baseline", "middle")
        .attr("fill", colors.label)
        .attr("font-size", 10)
        .text((d) => `${d}°`);

      // Elevation mask polygon.
      const maskG = g.append("g").attr("class", "elevation-mask");

      const maskSeries = mask?.series ?? [];

      if (maskSeries.length > 0) {
        const safeMask = maskSeries.map((d) => ({
          ...d,
          valueDeg: d.valueDeg == null ? 0 : d.valueDeg,
        }));

        const wrapped = [...safeMask, { ...safeMask[0], az: 360 }];

        const outerPts = wrapped.map((d) => {
          const a = toRad(d.az);
          return {
            az: d.az,
            x: Math.cos(a) * R,
            y: Math.sin(a) * R,
            usedFallback: d.usedFallback,
          };
        });

        const skylinePts = wrapped.map((d) => {
          const rr = ((90 - d.valueDeg) / 90) * R;
          const a = toRad(d.az);
          return {
            az: d.az,
            x: Math.cos(a) * rr,
            y: Math.sin(a) * rr,
            usedFallback: d.usedFallback,
            valueDeg: d.valueDeg,
          };
        });

        const blockedPoly = [...outerPts, ...skylinePts.slice().reverse()];

        const polyLine = d3
          .line()
          .x((p) => p.x)
          .y((p) => p.y)
          .curve(d3.curveLinearClosed);

        // Main obstruction fill.
        maskG
          .append("path")
          .datum(blockedPoly)
          .attr("d", polyLine)
          .attr("fill", colors.obstructionFill)
          .attr("stroke", colors.obstructionStroke)
          .attr("stroke-width", 1);

        // Skyline boundary line.
        maskG
          .append("path")
          .datum(skylinePts)
          .attr("d", d3.line().x((p) => p.x).y((p) => p.y).curve(d3.curveLinearClosed))
          .attr("fill", "none")
          .attr("stroke", colors.skylineBoundary)
          .attr("stroke-width", 1.4);

        // Fallback segments with dashed overlay.
        const fallbackPts = skylinePts.map((p, idx) => ({ ...p, idx }));

        const fallbackSegments = [];
        let current = [];
        for (let i = 0; i < fallbackPts.length; i++) {
          const p = fallbackPts[i];
          if (p.usedFallback) {
            current.push(p);
          } else if (current.length) {
            fallbackSegments.push(current);
            current = [];
          }
        }
        if (current.length) fallbackSegments.push(current);

        for (const seg of fallbackSegments) {
          maskG
            .append("path")
            .datum(seg)
            .attr("d", d3.line().x((p) => p.x).y((p) => p.y).curve(d3.curveLinear))
            .attr("fill", "none")
            .attr("stroke", colors.fallbackStroke)
            .attr("stroke-width", 1.6)
            .attr("stroke-dasharray", "4 3");
        }
      }

      // Draw sample points with color based on SNO.
      const snoColorScale = d3.scaleLinear()
        .domain([0, 25, 50])
        .range(["#ef4444", "#eab308", "#22c55e"])
        .clamp(true);

      const pointsG = g.append("g").attr("class", "sample-points");

      const pointData = samples.map(s => {
        const [x, y] = project(s.azimuth, s.elevation);
        return { x, y, ...s };
      });

      // Draw all historical points. (smaller)
      pointsG
        .selectAll(".history-point")
        .data(pointData.slice(0, -1))
        .enter()
        .append("circle")
        .attr("class", "history-point")
        .attr("cx", d => d.x)
        .attr("cy", d => d.y)
        .attr("r", 3)
        .attr("fill", d => snoColorScale(d.sno))
        .attr("stroke", colors.pointStroke)
        .attr("stroke-width", 1)
        .attr("opacity", 0.6)
        .on("mousemove", (event, d) => {
          if (!tooltip) return;
          tooltip.style.opacity = "1";
          tooltip.style.left = `${event.clientX}px`;
          tooltip.style.top = `${event.clientY}px`;
          tooltip.innerHTML = `
          <div style="font-weight: bold;">${escapeHtml(d.t)}</div>
          <hr style="margin: 4px; width: 100%;" />
          Azimuth: ${d.azimuth.toFixed(1)}°<br>
          Elevation: ${d.elevation.toFixed(1)}°<br>
          C/N&#8320;: ${d.sno.toFixed(1)} dB-Hz
        `;
        })
        .on("mouseleave", () => {
          if (!tooltip) return;
          tooltip.style.opacity = "0";
        });

      // Draw latest point. (larger with ring)
      if (pointData.length > 0) {
        const latest = pointData[pointData.length - 1];

        // Outer ring.
        pointsG
          .append("circle")
          .attr("cx", latest.x)
          .attr("cy", latest.y)
          .attr("r", 8)
          .attr("fill", "none")
          .attr("stroke", colors.latestPointRing)
          .attr("stroke-width", 1.2);

        // Latest point.
        pointsG
          .append("circle")
          .attr("class", "latest-point")
          .attr("cx", latest.x)
          .attr("cy", latest.y)
          .attr("r", 5)
          .attr("fill", snoColorScale(latest.sno))
          .attr("stroke", colors.pointStroke)
          .attr("stroke-width", 2)
          .style("cursor", "pointer")
          .on("mousemove", (event) => {
            if (!tooltip) return;
            tooltip.style.opacity = "1";
            tooltip.style.left = `${event.clientX}px`;
            tooltip.style.top = `${event.clientY}px`;
            tooltip.innerHTML = `
            <div style="font-weight: bold;">Latest: ${escapeHtml(latest.t)}</div>
            <hr style="margin: 4px; width: 100%;" />
            Azimuth: ${latest.azimuth.toFixed(1)}°<br>
            Elevation: ${latest.elevation.toFixed(1)}°<br>
            C/N&#8320;: ${latest.sno.toFixed(1)} dB-Hz
          `;
          })
          .on("mouseleave", () => {
            if (!tooltip) return;
            tooltip.style.opacity = "0";
          });
      }
    };

    renderChart();

    // Add resize listener.
    const handleResize = () => {
      renderChart();
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      d3.select(container).selectAll("svg").remove();
    };
  }, [elevationHistogram, azimuthHistogram, snoHistogram, samples, mask, elevationMaskArray, darkMode]);

  // Render decision.
  if (!elevationHistogram || !azimuthHistogram || !snoHistogram) {
    return <GenericWidgetLoadingSpinner height={510} />;
  }

  if (samples.length === 0) {
    return <div className="text-muted p-3">No matching timestamps to plot.</div>;
  }

  const containerBg = Store.get('dark_mode') ? '#262626' : '#f9f9f9';

  return (
    <div
      ref={containerRef}
      style={{
        width: "100%",
        height: 510,
        position: "relative",
        background: containerBg,
        overflow: "hidden",
      }}
    >
      <div
        ref={tooltipRef}
        style={{
          position: "fixed",
          pointerEvents: "none",
          background: "rgba(0,0,0,1.0)",
          color: "#fff",
          padding: "8px 10px",
          borderRadius: 10,
          fontSize: 12,
          lineHeight: 1.25,
          whiteSpace: "nowrap",
          opacity: 0,
          transform: "translate(-50%, -120%)",
          border: "1px solid rgba(255,255,255,0.12)",
          zIndex: 9999,
        }}
      />
    </div>
  );
}

function escapeHtml(str) {
  return String(str).replace(/[&<>"']/g, (m) => {
    switch (m) {
      case "&":
        return "&amp;";
      case "<":
        return "&lt;";
      case ">":
        return "&gt;";
      case '"':
        return "&quot;";
      case "'":
        return "&#039;";
      default:
        return m;
    }
  });
}