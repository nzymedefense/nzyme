import React from "react";

export const GNSSSNR = ({ snr }) => {
  if (snr == null || isNaN(snr)) {
    return <span className="text-muted">n/a</span>;
  }

  const v = Math.max(0, Math.min(99, Number(snr)));

  if (v === 0) {
    return <span className="text-muted">n/a</span>;
  }

  let label = "Excellent";
  let className = "text-success";

  if (v <= 20) {
    label = "Low";
    className = "text-danger";
  } else if (v <= 30) {
    label = "Fair";
    className = "text-warning";
  } else if (v <= 40) {
    label = "Good";
    className = "text-success";
  }

  return (
    <span className={className}>
      {label} ({Math.trunc(v)})
    </span>
  );
};
