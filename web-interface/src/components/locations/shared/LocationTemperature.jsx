import React from "react";

export default function LocationTemperature({environment}) {

  if (!environment || environment.temperature === 0) {
    return <span className="text-muted">no temperature data</span>
  }

  return <span>{environment.temperature}&deg;C</span>

}