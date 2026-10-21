import React from "react";

export default function BeaconExclusionCriteria({exclusion}) {

  const criteria = [];

  if (exclusion.beacon_types && exclusion.beacon_types.length > 0) {
    criteria.push({label: "Type", value: exclusion.beacon_types.join(", ")});
  }

  if (exclusion.destination_host) {
    criteria.push({label: "Destination host", value: exclusion.destination_host});
  }

  if (exclusion.destination_cidr) {
    criteria.push({label: "Destination address", value: exclusion.destination_cidr});
  }

  if (exclusion.destination_ports) {
    criteria.push({label: exclusion.destination_ports.match(/[,-]/) ? "Destination ports" : "Destination port",
      value: exclusion.destination_ports});
  }

  if (exclusion.source_cidr) {
    criteria.push({label: "Device address", value: exclusion.source_cidr});
  }

  if (exclusion.source_mac) {
    criteria.push({label: "Device MAC", value: exclusion.source_mac});
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
