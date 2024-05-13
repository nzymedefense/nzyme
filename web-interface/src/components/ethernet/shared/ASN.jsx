import React from "react";

export default function ASN(props) {

  const geo = props.geo;

  if (!geo || !geo.asn_number) {
    return <span className="text-muted">n/a</span>
  }

  return (
      <span>
        <span className="asn-number">{geo.asn_number}</span> ({geo.asn_name ? geo.asn_name : "Unknown"})
      </span>
  )

}