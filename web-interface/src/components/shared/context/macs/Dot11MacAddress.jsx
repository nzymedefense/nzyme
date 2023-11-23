import React from "react";
import Dot11MacAddressType from "./Dot11MacAddressType";
import MacAddress from "./MacAddress";

function Dot11MacAddress(props) {

  const address = props.address;
  const type = props.type;

  // Optional.
  const href = props.href;
  const onClick = props.onClick;

  return (
      <span className="dot11-mac">
        <Dot11MacAddressType type={type} /> <MacAddress address={address} href={href} onClick={onClick} />
      </span>
  )

}

export default Dot11MacAddress;