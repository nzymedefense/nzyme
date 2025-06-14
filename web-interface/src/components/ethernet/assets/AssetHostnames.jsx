import React from "react";
import {truncate} from "../../../util/Tools";

export default function AssetHostnames(props) {

  const hostnames = props.hostnames;

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
      <span className="hostname" title={hostnames[0].hostname}>{truncate(hostnames[0].hostname, 30, false)} {additional()}</span>
  )

}