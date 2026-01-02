import React, { useEffect, useMemo, useRef } from "react";
import * as d3 from "d3";
import GenericWidgetLoadingSpinner from "../widgets/GenericWidgetLoadingSpinner";
import ApiRoutes from "../../util/ApiRoutes";
import Store from "../../util/Store";

// TODO this has a good amount of overlap with the individual PRN sky plot. We may eventually want to share more.

export default function GNSSSkyPlot({ satellites, elevationMask, height = 520 }) {
  const containerRef = useRef(null);
  const tooltipRef = useRef(null);
  const [darkMode, setDarkMode] = React.useState(Store.get('dark_mode'));

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

  // Normalize satellites input.
  const satellitesArray = useMemo(() => {
    if (satellites == null) return [];
    if (Array.isArray(satellites)) return satellites;
    if (satellites && Array.isArray(satellites.satellites)) return satellites.satellites;
    return [];
  }, [satellites]);

  // Normalize elevationMask input.
  const elevationMaskArray = useMemo(() => {
    if (elevationMask == null) return [];

    // already array
    if (Array.isArray(elevationMask)) return elevationMask;

    // common "object keyed by azimuth bucket" case (your example)
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

  // Filter to satellites with az/el present.
  const sats = useMemo(() => {
    return satellitesArray
      .filter((s) => s?.azimuth_degrees != null && s?.elevation_degrees != null)
      .map((s) => ({
        constellation: s.constellation ?? "Unknown",
        prn: s.prn,
        az: Number(s.azimuth_degrees),
        el: Number(s.elevation_degrees),
        sno: Number(s.average_sno),
        used: !!s.used_for_fix,
        doppler_hz: Number(s.average_doppler_hz),
        pseudorange_rms_error: Number(s.average_pseudorange_rms_error),
        maximum_multipath_indicator: Number(s.maximum_multipath_indicator),
        track_points: s.track_points || [],
      }));
  }, [satellitesArray]);

  // Process track points for each satellite.
  const tracks = useMemo(() => {
    return sats.map((sat) => {
      if (!sat.track_points || sat.track_points.length === 0) {
        return { ...sat, trackSegments: [] };
      }

      // Filter valid points, excluding 0 degree values which are likely collection artifacts.
      const validPoints = sat.track_points
        .filter((tp) =>
          tp?.azimuth_degrees != null &&
          tp?.elevation_degrees != null &&
          tp.azimuth_degrees !== 0 &&
          tp.elevation_degrees !== 0
        )
        .map((tp) => ({
          az: Number(tp.azimuth_degrees),
          el: Number(tp.elevation_degrees),
          timestamp: tp.timestamp,
          sno: tp.average_sno != null ? Number(tp.average_sno) : null,
        }));

      if (validPoints.length === 0) {
        return { ...sat, trackSegments: [] };
      }

      // Split track into segments at wraparound boundaries only.
      const segments = [];
      let currentSegment = [validPoints[0]];

      for (let i = 1; i < validPoints.length; i++) {
        const prev = validPoints[i - 1];
        const curr = validPoints[i];
        const azDiff = Math.abs(curr.az - prev.az);

        if (azDiff > 180) {
          if (currentSegment.length >= 2) {
            segments.push(currentSegment);
          }
          currentSegment = [curr];
        } else {
          currentSegment.push(curr);
        }
      }

      // Add final segment.
      if (currentSegment.length >= 2) {
        segments.push(currentSegment);
      }

      return { ...sat, trackSegments: segments };
    });
  }, [sats]);

  // Prepare mask at a fixed step (use your 5 degree bins by default).
  const mask = useMemo(() => {
    const binSize = 5;

    // Map by bucket
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

    // Build a full 0..355 series so the polygon is stable even if some bins are missing.
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

    // Clamp elevations to a sensible [0..90] range.
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

    // If we're "loading", clear any existing SVG and do nothing else.
    if (satellites == null || elevationMask == null) {
      d3.select(container).selectAll("svg").remove();
      return;
    }

    const renderChart = () => {
      d3.select(container).selectAll("svg").remove();

      const w = container.clientWidth || 800;
      const h = height;
      const size = Math.min(w, h);
      const margin = 34;
      const R = size / 2 - margin;

      const svg = d3.select(container).append("svg").attr("width", w).attr("height", h);
      const g = svg.append("g").attr("transform", `translate(${w / 2},${h / 2})`);

      // Theme colors,
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
        colors.trackColor = 'rgba(255,255,255,0.25)';
        colors.satRing = 'rgba(255,255,255,0.3)';
        colors.bubbleBackground = '#404040';
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
        colors.trackColor = 'rgba(0,0,0,0.25)';
        colors.satRing = 'rgba(0,0,0,0.18)';
        colors.bubbleBackground = '#fff';
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

      // Degree labels at 45 degree intervals. (skip cardinals)
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

      /*
       * Elevation mask / obstruction polygon:
       *
       *   skyline_elevation (or best_effort) is the minimum elevation required at that azimuth to see satellites.
       *   Everything BELOW that elevation is "blocked", meaning the band between the outer circle and the skyline curve.
       */
      const maskG = g.append("g").attr("class", "elevation-mask");

      const maskSeries = mask?.series ?? [];

      // If a bin has null value, treat it as 0 degrees (no obstruction) so the polygon remains well-formed.
      const safeMask = maskSeries.map((d) => ({
        ...d,
        valueDeg: d.valueDeg == null ? 0 : d.valueDeg,
      }));

      const wrapped = [...safeMask, { ...safeMask[0], az: 360 }];

      // Build a closed polygon for "blocked" area: outer arc + skyline arc. (reversed)
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

      // Emphasize fallback bins with a dashed overlay. (so "best effort" is obvious)
      const fallbackPts = skylinePts.map((p, idx) => ({
        ...p,
        idx,
      }));

      // Build dashed segments only where usedFallback=true.
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
        const extended = seg.length >= 2 ? seg : seg;
        maskG
          .append("path")
          .datum(extended)
          .attr("d", d3.line().x((p) => p.x).y((p) => p.y).curve(d3.curveLinear))
          .attr("fill", "none")
          .attr("stroke", colors.fallbackStroke)
          .attr("stroke-width", 1.6)
          .attr("stroke-dasharray", "4 3");
      }

      /*
       * Satellite Tracks
       *
       * Draw tracks before satellite icons so they appear behind
       * Simple straight dotted line from first to last position
       */
      const tracksG = g.append("g").attr("class", "satellite-tracks");

      const iconRadius = 9; // Half of iconSize.

      tracks.forEach((sat) => {
        if (!sat.trackSegments || sat.trackSegments.length === 0) return;

        // Draw each segment separately.
        sat.trackSegments.forEach((segment, segIdx) => {
          if (segment.length < 2) return;

          // First point is newest (current position), last point is oldest.
          const currentPt = segment[0];
          const oldestPt = segment[segment.length - 1];

          const [xCurrent, yCurrent] = project(currentPt.az, currentPt.el);
          const [xOldest, yOldest] = project(oldestPt.az, oldestPt.el);

          // Calculate direction vector from current to oldest.
          const dx = xOldest - xCurrent;
          const dy = yOldest - yCurrent;
          const distance = Math.sqrt(dx * dx + dy * dy);

          // If points are too close, skip drawing.
          if (distance < iconRadius + 2) return;

          // Offset start point to begin at edge of current icon circle.
          const offsetRatio = iconRadius / distance;
          const startX = xCurrent + dx * offsetRatio;
          const startY = yCurrent + dy * offsetRatio;

          // Simple straight dotted line starting at circle edge.
          tracksG
            .append("line")
            .attr("x1", startX)
            .attr("y1", startY)
            .attr("x2", xOldest)
            .attr("y2", yOldest)
            .attr("stroke", colors.trackColor)
            .attr("stroke-width", 1.5)
            .attr("stroke-linecap", "round")
            .attr("stroke-dasharray", "2 3")
            .attr("opacity", sat.used ? 1.0 : 0.6);
        });
      });

      // Project current satellite positions.
      const pts = sats.map((s) => {
        const [x, y] = project(s.az, s.el);
        return { ...s, x, y };
      });

      const satG = g.append("g");

      const iconSize = 18; // px

      // Used-for-fix ring.
      satG
        .selectAll(".sat-ring")
        .data(pts.filter((d) => d.used))
        .enter()
        .append("circle")
        .attr("class", "sat-ring")
        .attr("cx", (d) => d.x)
        .attr("cy", (d) => d.y)
        .attr("r", iconSize / 2 + 3)
        .attr("fill", "none")
        .attr("stroke", colors.satRing)
        .attr("stroke-width", 1.2);

      const satFO = satG
        .selectAll(".sat-fo")
        .data(pts)
        .enter()
        .append("foreignObject")
        .attr("class", "sat-fo")
        .attr("x", (d) => d.x - iconSize / 2)
        .attr("y", (d) => d.y - iconSize / 2)
        .attr("width", iconSize)
        .attr("height", iconSize)
        .style("overflow", "visible");

      const bubble = satFO
        .append("xhtml:div")
        .style("width", `${iconSize}px`)
        .style("height", `${iconSize}px`)
        .style("border-radius", "50%")
        .style("overflow", "hidden")
        .style("display", "flex")
        .style("align-items", "center")
        .style("justify-content", "center")
        .style("background", colors.bubbleBackground);

      bubble
        .append("xhtml:span")
        .attr("class", (d) => {
          const flag = flagClassForConstellation(d.constellation);
          return flag ? `fi ${flag} flag-inline` : "";
        })
        .style("width", "100%")
        .style("height", "100%")
        .style("display", "block")
        .style("cursor", "pointer")
        .style("opacity", (d) => (d.used ? 1.0 : 0.5))
        .attr("title", (d) => `${d.constellation} PRN ${d.prn} `)
        .on("click", (event, d) => {
          window.location.href = ApiRoutes.GNSS.PRN(d.constellation, d.prn);
        })
        .on("mousemove", (event, d) => {
          if (!tooltip) return;
          tooltip.style.opacity = "1";
          tooltip.style.left = `${event.clientX}px`;
          tooltip.style.top = `${event.clientY}px`;
          tooltip.innerHTML = `
          <div style="font-weight: bold; text-align: center;">
            <span class="fi ${flagClassForConstellation(d.constellation)} flag-inline"></span>
            ${escapeHtml(d.constellation)} PRN ${escapeHtml(String(d.prn))}
          </div>
          
          <hr style="margin: 4px; width: 100%;" />
          
          C/N&#8320;: <span class="${snoColor(d.sno)}">${d.sno ? (d.sno + " dB-Hz") : "Unknown"}</span><br>
          Multipath: <span class="${multipathColor(d.maximum_multipath_indicator)}">${multipathLabel(d.maximum_multipath_indicator)}</span><br><br>
          
          Azimuth: ${escapeHtml(String(d.az))}°<br>
          Elevation: ${escapeHtml(String(d.el))}°
        `;
        })
        .on("mouseleave", () => {
          if (!tooltip) return;
          tooltip.style.opacity = "0";
        });
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
  }, [satellites, elevationMask, sats, satellitesArray, elevationMaskArray, mask, tracks, height, darkMode]);

  // Render decision after hooks.
  if (satellites == null || elevationMask == null) {
    return <GenericWidgetLoadingSpinner height={height} />;
  }

  const containerBg = Store.get('dark_mode') ? '#262626' : '#f9f9f9';

  return (
    <div
      ref={containerRef}
      style={{
        width: "100%",
        height,
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

function flagClassForConstellation(constellation) {
  switch (constellation) {
    case "GPS":
      return "fi-us";
    case "Galileo":
      return "fi-eu";
    case "GLONASS":
      return "fi-ru";
    case "BeiDou":
      return "fi-cn";
    default:
      return null;
  }
}

function snoColor(sno) {
  if (!sno) {
    return "text-muted";
  }

  if (sno <= 20) {
    return "text-danger-brighter";
  } else if (sno <= 30) {
    return "text-warning";
  } else {
    return "text-success-brighter";
  }
}

function multipathLabel(mp) {
  switch (mp) {
    case 1: return "Low";
    case 2: return "Medium";
    case 3: return "High";
    default: return "n/a";
  }
}

function multipathColor(mp) {
  switch (mp) {
    case 1: return "text-success-brighter";
    case 2: return "text-warning";
    case 3: return "text-danger-brighter";
    default: return "text-muted";
  }
}