import React from "react";
import IPAddressLink from "./IPAddressLink";

export default function L4Address(props) {

  const address = props.address;

  return (
      <IPAddressLink ip={address.address} port={address.port} />
  )

}