import React from "react";

export default function ASN(props) {

  const geo = props.geo;

  if (!geo || !geo.asn_number) {
    return <span className="text-muted">n/a</span>
  }

  return (
    <span>
      <a className="asn-number" href="#">{geo.asn_number}</a> ({geo.asn_name ? geo.asn_name : "Unknown"})
    </span>
  )

}