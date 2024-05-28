import React from "react";
import IPAddressLink from "./IPAddressLink";

export default function L4Address(props) {

  const address = props.address;
  const hidePort = props.hidePort;

  return (
      <IPAddressLink ip={address.address} port={hidePort ? null : address.port} />
  )

}