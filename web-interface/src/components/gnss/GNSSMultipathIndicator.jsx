import React from "react";

export default function GNSSMultipathIndicator({indicator}) {

  switch (indicator) {
    case 1: return <span className="text-success">Low</span>;
    case 2: return <span className="text-warning">Medium</span>;
    case 3: return <span className="text-danger">High</span>;
    default: return <span className="text-muted">n/a</span>;
  }

}