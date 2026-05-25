import React from "react";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import moment from "moment";

export default function Clock({ title, timezone, referenceTimezone, now }) {
  const m = moment(now).tz(timezone);
  const ref = moment(now).tz(referenceTimezone);

  const offsetMin = m.utcOffset() - ref.utcOffset();
  const offsetLabel = offsetMin === 0
    ? null
    : `${Math.abs(offsetMin / 60).toFixed(offsetMin % 60 === 0 ? 0 : 1)}h ${offsetMin > 0 ? "ahead" : "behind"}`;

  const showDate = m.format("YYYY-MM-DD") !== ref.format("YYYY-MM-DD");

  return (
    <div className="col-md-6">
      <div className="card h-100">
        <div className="card-body">
          <CardTitleWithControls title={title} slim={true} />
          <div className="display-5 fw-light" style={{ fontVariantNumeric: "tabular-nums" }}>
            {m.format("LTS")}
          </div>
          <div className="text-muted small mt-1">
            {m.format("z")}
            {offsetLabel && <> · {offsetLabel}</>}
            {showDate && <> · {m.format("ddd, MMM D")}</>}
          </div>
        </div>
      </div>
    </div>
  );
}