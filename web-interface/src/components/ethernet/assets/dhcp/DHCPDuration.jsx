import React from "react";

export default function DHCPDuration(props) {

  const duration = props.duration;

  if (duration == null) {
    return <span className="text-muted">n/a</span>
  }

  if (duration === 0) {
    return "<0 ms"
  }

  return duration + " ms";

}