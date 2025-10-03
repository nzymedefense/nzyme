import React from "react";
import numeral from "numeral";

export default function LongDistance({ feet }) {

  if (feet === null) {
    return <span className="text-muted">n/a</span>
  }

  const FEET_PER_MILE = 5280;

  let displayValue;
  let unit;

  if (feet >= FEET_PER_MILE/4) {
    displayValue = numeral(feet / FEET_PER_MILE).format("0,0.00");
    unit = "mi";
  } else {
    displayValue = numeral(feet).format("0,0");
    unit = "ft";
  }

  return <span>{displayValue} {unit}</span>;
}