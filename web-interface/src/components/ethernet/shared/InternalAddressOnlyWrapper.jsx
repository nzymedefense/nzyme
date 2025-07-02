import React from "react";

export default function InternalAddressOnlyWrapper(props) {

  const inner = props.inner;
  const address = props.address;

  if (!address || !address.attributes) {
    return <span className="text-muted">n/a</span>
  }

  if (address.attributes.is_site_local) {
    return inner;
  } else {
    return <span className="text-muted">n/a</span>
  }

}