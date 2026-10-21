import React from "react";
import numeral from "numeral";

export const formatInterval = (seconds) => {
  if (seconds === null || seconds === undefined || seconds <= 0) {
    return "n/a";
  }

  if (seconds < 60) {
    return numeral(seconds).format("0,0.[0]") + "s";
  }

  if (seconds < 3600) {
    return numeral(seconds / 60).format("0,0.[0]") + "m";
  }

  if (seconds < 86400) {
    return numeral(seconds / 3600).format("0,0.[0]") + "h";
  }

  return numeral(seconds / 86400).format("0,0.[0]") + "d";
}

export default function BeaconInterval({seconds, jitter = undefined}) {

  if (seconds === null || seconds === undefined || seconds <= 0) {
    return <span className="text-muted">n/a</span>
  }

  return (
    <span title={numeral(seconds).format("0,0.0") + " seconds"}>
      {formatInterval(seconds)}
      {jitter !== undefined && jitter !== null ?
        <span className="text-muted"> &plusmn;{numeral(jitter * 100).format("0")}%</span> : null}
    </span>
  )

}
