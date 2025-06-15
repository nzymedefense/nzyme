import React from "react";
import {truncate} from "../../../util/Tools";

export default function AssetHostnames(props) {

  // Deduplicate.
  const hostnames = Array.from(
      new Map(props.hostnames.map(item => [item.hostname, item])).values()
  )

  const additional = () => {
    if (hostnames.length < 2) {
      return null
    }

    return <span>(+{hostnames.length-1})</span>
  }

  if (hostnames === null || hostnames.length === 0) {
    return <span className="text-muted">None</span>;
  }

  return (
      <span title={hostnames.map(item => item.hostname).join(", ")}>
        <span className="hostname">{truncate(hostnames[0].hostname, 35, false)}</span>{' '}{additional()}
      </span>
  )

}