import React from "react";

export default function AssetIpAddresses(props) {

  // Deduplicate.
  const addresses = Array.from(
      new Map(props.addresses.map(item => [item.address, item])).values()
  )

  const additional = () => {
    if (addresses.length < 2) {
      return null
    }

    return <span>[+{addresses.length-1}]</span>
  }

  if (addresses === null || addresses.length === 0) {
    return <span className="text-muted">None</span>;
  }

  return (
      <span title={addresses.map(item => item.address).join(", ")}>
        <span className="ip-address">{addresses[0].address}</span>{' '}{additional()}
      </span>
  )

}