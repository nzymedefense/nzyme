import React from "react";

export default function GNSSCn0({ cn0 }) {
  if (cn0 == null || isNaN(cn0)) {
    return <span className="text-danger">n/a</span>;
  }

  const v = Math.max(0, Math.min(99, Number(cn0)));

  if (v === 0) {
    return <span className="text-danger">n/a</span>;
  }

  let className = "text-success";

  if (v <= 20) {
    className = "text-danger";
  } else if (v <= 30) {
    className = "text-warning";
  } else if (v <= 40) {
    className = "text-success";
  }

  return (
    <span className={className}>
      {Math.trunc(v)} dB-Hz
    </span>
  );
};
