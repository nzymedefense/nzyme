import React from "react";

export default function LocationWind({environment}) {

  const describeWind = (speed, gust) => {
    // The descriptor follows whichever is higher. Gusts are usually
    // what determines whether something feels concerning, not the average.
    const effective = Math.max(speed ?? 0, gust ?? 0);

    let label;
    if (effective < 5) return "calm winds";
    else if (effective < 15) label = "light breeze";
    else if (effective < 25) label = "breezy";
    else if (effective < 40) label = "windy";
    else if (effective < 60) label = "strong winds";
    else if (effective < 80) label = "very strong winds";
    else label = "dangerous winds";

    const parts = [];
    if (speed != null) parts.push(`${speed} km/h`);
    if (gust != null) parts.push(`gusts ${gust} km/h`);

    return `${label} (${parts.join(", ")})`;
  }

  if (!environment || (environment.wind_speed === null && environment.wind_gust === null)) {
    return <span className="text-muted">no wind data</span>
  }

  return <span>{describeWind(environment.wind_speed, environment.wind_gust)}</span>

}