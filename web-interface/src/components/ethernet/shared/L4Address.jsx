import React from "react";
import IPAddressLink from "./IPAddressLink";

export default function L4Address(props) {

  const address = props.address;
  const hidePort = props.hidePort;

  if (!address) {
    return (
        <span>
          [missing] <i className="fa-solid fa-circle-question"
                       title="Underlying connection information has been retention cleaned or not recorded."></i>
        </span>
    )
  }

  return (
      <IPAddressLink ip={address.address} port={hidePort ? null : address.port} />
  )

}