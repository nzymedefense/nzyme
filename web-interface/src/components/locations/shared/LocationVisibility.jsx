import React from "react";

export default function LocationVisibility({ environment }) {

  const describeVisibility = (meters) => {
    if (meters >= 16000) return "full visibility";

    let label;
    if (meters < 200) label = "dense fog";
    else if (meters < 1000) label = "fog";
    else if (meters < 4000) label = "reduced visibility";
    else if (meters < 10000) label = "hazy";
    else label = "mostly clear";

    const value = meters < 1000
      ? `${meters} m`
      : `${(meters / 1000).toFixed(1)} km`;

    return `${label} (${value})`;
  }

  if (!environment || environment.visibility == null) {
    return <span className="text-muted">no visibility data</span>
  }

  return <span>{describeVisibility(environment.visibility)}</span>

}