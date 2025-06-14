import React from "react";

export default function AssetIpAddresses(props) {

  const addresses = props.addresses;

  const additional = () => {
    if (addresses.length < 2) {
      return null
    }

    return <span>(+{addresses.length-1})</span>
  }

  if (addresses === null || addresses.length === 0) {
    return <span className="text-muted">None</span>;
  }

  return (
      <span className="ip-address">{addresses[0].address} {additional()}</span>
  )

}