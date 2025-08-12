import React from "react";

export default function GNSSDegrees({degrees}) {

  if (degrees === null) {
    return <span className="text-muted">n/a</span>;
  }

  return (
    <span>{degrees}&deg;</span>
  )

}