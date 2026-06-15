import React, { useState, useMemo, useRef, useEffect } from "react";
import moment from "moment";
import timelineEventEnumToTitle from "./TimelineEventTitle";

const LEVEL_ALPHA = [0, 0.28, 0.5, 0.72, 1.0];
const SAT = 68, LUM = 62;
const MIN_CELL_W = 5;

function hashStr(s) {
  let h = 2166136261;
  for (let i = 0; i < s.length; i++) { h ^= s.charCodeAt(i); h = Math.imul(h, 16777619); }
  return h >>> 0;
}
const defaultHue = (t) => hashStr(t) % 360;

function zoned(value, tz) {
  if (tz === "UTC") return moment.utc(value);
  if (!tz || tz === "local") return moment(value);
  if (typeof moment.tz === "function") return moment.tz(value, tz);
  return moment(value);
}

function levelFor(v, max) {
  if (v <= 0 || max <= 0) return 0;
  const r = v / max;
  return r > 0.75 ? 4 : r > 0.5 ? 3 : r > 0.25 ? 2 : 1;
}

function useWidth() {
  const ref = useRef(null);
  const [w, setW] = useState(0);
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const update = () => setW(el.clientWidth);
    update();
    let ro;
    if (typeof ResizeObserver !== "undefined") { ro = new ResizeObserver(update); ro.observe(el); }
    else { window.addEventListener("resize", update); }
    return () => { if (ro) ro.disconnect(); else window.removeEventListener("resize", update); };
  }, []);
  return [ref, w];
}

const LAYOUTS = {
  DAY: {
    unit: "day", rows: 7, cellH: 13, gap: 3,
    rowIndex: (m) => m.day(),
    columnKey: (m) => m.clone().subtract(m.day(), "days").startOf("day").valueOf(),
    rowLabels: { 1: "Mon", 3: "Wed", 5: "Fri" },
    tip: (m) => m.format("ddd · MMM D, YYYY"),
    colText: (m, prev, i) => (i === 0 || !prev || m.month() !== prev.month() || m.year() !== prev.year() ? m.format("MMM") : ""),
  },
  HOUR: {
    unit: "hour", rows: 24, cellH: 14, gap: 3,
    rowIndex: (m) => m.hour(),
    columnKey: (m) => m.clone().startOf("day").valueOf(),
    rowLabels: { 0: "00", 6: "06", 12: "12", 18: "18" },
    tip: (m) => m.format("MMM D · HH:00"),
    colText: (m, prev, i) => (i > 0 && prev && m.month() !== prev.month() ? m.format("MMM D") : m.format("D")),
  },
  MINUTE: {
    unit: "minute", rows: 60, cellH: 9, gap: 2,
    rowIndex: (m) => m.minute(),
    columnKey: (m) => m.clone().startOf("hour").valueOf(),
    rowLabels: { 0: ":00", 15: ":15", 30: ":30", 45: ":45" },
    tip: (m) => m.format("MMM D · HH:mm"),
    colText: (m, prev, i) => (i === 0 || m.hour() % 3 === 0 ? m.format("HH:00") : ""),
  },
};

function buildModel(data, timeZone) {
  const bucketType = LAYOUTS[data.bucket_type] ? data.bucket_type : "DAY";
  const layout = LAYOUTS[bucketType];
  const mk = (x) => zoned(x, timeZone);

  const from = mk(data.from), to = mk(data.to);
  const byMs = new Map();
  for (const b of data.buckets || []) byMs.set(mk(b.bucket).valueOf(), b);

  const endMs = to.valueOf(), cap = 20000;
  const cols = new Map();
  let order = 0, n = 0;
  const cursor = from.clone().startOf(layout.unit);
  while (cursor.valueOf() <= endMs && n < cap) {
    const m = cursor.clone();
    const ck = layout.columnKey(m);
    let col = cols.get(ck);
    if (!col) { col = { key: ck, order: order++, first: m, slots: new Array(layout.rows).fill(null) }; cols.set(ck, col); }
    col.slots[layout.rowIndex(m)] = { m, ms: m.valueOf(), bucket: byMs.get(m.valueOf()) || null };
    cursor.add(1, layout.unit);
    n++;
  }
  const columns = [...cols.values()].sort((a, b) => a.order - b.order);
  return { layout, columns };
}

function placeLabels(columns, layout, stepPx) {
  const out = new Array(columns.length).fill("");
  let guardRight = -Infinity;
  for (let i = 0; i < columns.length; i++) {
    const text = layout.colText(columns[i].first, i > 0 ? columns[i - 1].first : null, i);
    if (!text) continue;
    const x = i * stepPx;
    if (x < guardRight) continue;
    out[i] = text;
    guardRight = x + (text.length * 7 + 4);
  }
  return out;
}

export function TimelineActivityHistogram({ data, timeZone = "local", hueForType }) {
  const [hover, setHover] = useState(null);
  const [gridRef, gridW] = useWidth(); // width available to the columns

  const { layout, columns } = useMemo(() => buildModel(data, timeZone), [data, timeZone]);
  const cellH = layout.cellH, gap = layout.gap, rows = layout.rows;
  const nCols = columns.length;

  // Stretch cell width to fill the available width.
  const cellW = useMemo(() => {
    if (!gridW || nCols === 0) return cellH;
    const raw = (gridW - (nCols - 1) * gap) / nCols;
    return Math.max(MIN_CELL_W, Math.floor(raw));
  }, [gridW, nCols, gap, cellH]);

  const totals = data.totals_by_event_type || {};
  const buckets = data.buckets || [];
  const sortedTypes = useMemo(() => Object.keys(totals).sort((a, b) => totals[b] - totals[a]), [data]);

  const hueMap = useMemo(() => {
    const names = Object.keys(totals).sort();
    const n = Math.max(1, names.length);
    const m = {};
    names.forEach((name, i) => { m[name] = Math.round((i / n) * 360 + 18) % 360; });
    return m;
  }, [data]);

  const hueOf = (t) => (hueForType ? hueForType(t) : (hueMap[t] != null ? hueMap[t] : defaultHue(t)));
  const swatch = (t) => `hsl(${hueOf(t)} ${SAT}% ${LUM}%)`;
  const fill = (t, a) => `hsl(${hueOf(t)} ${SAT}% ${LUM}% / ${a})`;

  const maxTotal = useMemo(() => Math.max(1, ...buckets.map((b) => b.total)), [data]);
  const colLabels = useMemo(() => placeLabels(columns, layout, cellW + gap), [columns, layout, cellW, gap]);
  const vw = typeof window !== "undefined" ? window.innerWidth : 1200;

  // Computed dimensions handed to CSS as custom properties.
  const rootStyle = { "--th-cw": `${cellW}px`, "--th-ch": `${cellH}px`, "--th-gap": `${gap}px` };

  return (
    <div className="timeline-histogram" style={rootStyle}>

      {/* Color key. */}
      <div className="timeline-histogram-legend">
        {sortedTypes.map((t) => (
          <span key={t} className="timeline-histogram-legend-item">
            <span className="timeline-histogram-legend-swatch" style={{ background: swatch(t) }} />
            {timelineEventEnumToTitle(t)}
            <span className="timeline-histogram-legend-count">{totals[t]}</span>
          </span>
        ))}
      </div>

      {/* Matrix. */}
      <div className="timeline-histogram-matrix">
        <div className="timeline-histogram-axis">
          <div className="timeline-histogram-axis-spacer" />
          {Array.from({ length: rows }).map((_, r) => (
            <div key={r} className="timeline-histogram-axis-label">{layout.rowLabels[r] || ""}</div>
          ))}
        </div>

        <div className="timeline-histogram-grid" ref={gridRef}>
          <div className="timeline-histogram-collabels">
            {columns.map((col, idx) => (
              <div key={col.key} className="timeline-histogram-collabel">
                {colLabels[idx] ? <span>{colLabels[idx]}</span> : null}
              </div>
            ))}
          </div>

          <div className="timeline-histogram-cols">
            {columns.map((col) => (
              <div key={col.key} className="timeline-histogram-col">
                {col.slots.map((slot, r) => {
                  const live = !!slot;
                  const b = live ? slot.bucket : null;
                  const filled = live && b && b.total > 0;
                  let modifier = "";
                  if (!live) modifier = " timeline-histogram-cell-void";
                  else if (!filled) modifier = " timeline-histogram-cell-empty";
                  const hovered = live && hover && hover.ms === slot.ms;

                  let bands = null;
                  if (filled) {
                    const alpha = LEVEL_ALPHA[levelFor(b.total, maxTotal)];
                    bands = Object.keys(b.counts_by_event_type).sort().map((t) => (
                      <span key={t} className="timeline-histogram-band"
                            style={{ flexGrow: b.counts_by_event_type[t], background: fill(t, alpha) }} />
                    ));
                  }

                  return (
                    <div key={r}
                         className={"timeline-histogram-cell" + modifier + (hovered ? " is-hover" : "")}
                         onMouseEnter={live ? (e) => setHover({ ms: slot.ms, m: slot.m, bucket: slot.bucket, x: e.clientX, y: e.clientY }) : undefined}
                         onMouseMove={live ? (e) => setHover((h) => (h && h.ms === slot.ms ? { ...h, x: e.clientX, y: e.clientY } : h)) : undefined}
                         onMouseLeave={live ? () => setHover((h) => (h && h.ms === slot.ms ? null : h)) : undefined}
                    >
                      {bands}
                    </div>
                  );
                })}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Intensity key. */}
      <div className="timeline-histogram-scale">
        Less
        {[0, 1, 2, 3, 4].map((lvl) => (
          <span key={lvl} className="timeline-histogram-scale-box"
                style={{ background: lvl === 0 ? "var(--th-empty)" : `hsl(220 12% 65% / ${LEVEL_ALPHA[lvl]})` }} />
        ))}
        More
      </div>

      {/* Tooltip. */}
      {hover && (() => {
        const b = hover.bucket;
        const left = Math.min(hover.x + 14, vw - 268);
        return (
          <div className="timeline-histogram-tooltip" style={{ left, top: hover.y + 16 }}>
            <div className="timeline-histogram-tooltip-time">{layout.tip(hover.m)}</div>
            {!b || b.total === 0 ? (
              <div className="timeline-histogram-tooltip-empty">No activity</div>
            ) : (
              <>
                <div className="timeline-histogram-tooltip-total">{b.total} event{b.total === 1 ? "" : "s"}</div>
                {Object.entries(b.counts_by_event_type).sort((x, y) => y[1] - x[1]).map(([k, v]) => (
                  <div key={k} className="timeline-histogram-tooltip-row">
                    <span className="timeline-histogram-tooltip-swatch" style={{ background: swatch(k) }} />
                    <span className="timeline-histogram-tooltip-row-name">{timelineEventEnumToTitle(k)}</span>
                    <span className="timeline-histogram-tooltip-row-count">{v}</span>
                  </div>
                ))}
              </>
            )}
          </div>
        );
      })()}
    </div>
  );
}