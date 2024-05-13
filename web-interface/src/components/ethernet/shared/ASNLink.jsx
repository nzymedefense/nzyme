import React from "react";
import ASN from "./ASN";
import ApiRoutes from "../../../util/ApiRoutes";

export default function ASNLink(props) {

  const geo = props.geo;

  if (!geo || !geo.asn_number) {
    return <span className="text-muted">n/a</span>
  }

  return (
      <a href={ApiRoutes.ETHERNET.L4.ASN(geo.asn_number)}>
        <ASN geo={geo} />
      </a>
  )

}