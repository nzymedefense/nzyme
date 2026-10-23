import React from "react";
import {reconScanTypeToName} from "../ReconScanType";

export default function ReconExclusionCriteria({exclusion}) {

  const criteria = [];

  if (exclusion.scan_types && exclusion.scan_types.length > 0) {
    criteria.push({label: "Type", value: exclusion.scan_types.map(reconScanTypeToName).join(", ")});
  }

  if (exclusion.source_cidr) {
    criteria.push({label: "Scanner address", value: exclusion.source_cidr});
  }

  if (exclusion.source_mac) {
    criteria.push({label: "Scanner MAC", value: exclusion.source_mac});
  }

  if (exclusion.target_cidr) {
    criteria.push({label: "Target network", value: exclusion.target_cidr});
  }

  if (criteria.length === 0) {
    return <span className="text-muted">No criteria</span>
  }

  return (
    <React.Fragment>
      {criteria.map((c, i) => {
        return (
          <React.Fragment key={i}>
            {i > 0 ? <span className="text-muted"> and </span> : null}
            <span className="text-nowrap">{c.label} <span className="machine-data">{c.value}</span></span>
          </React.Fragment>
        )
      })}
    </React.Fragment>
  )

}
