import React from "react";
import MacAddress from "./MacAddress";

function Dot11MacAddress(props) {

  const address = props.address;
  const addressWithContext = props.addressWithContext;
  const type = props.type;
  const showOui = props.showOui;
  const highlighted = props.highlighted;

  // Optional.
  const href = props.href;
  const onClick = props.onClick;

  return (
      <span className="mac-address">
        <MacAddress address={address}
                    addressWithContext={addressWithContext}
                    type={type}
                    showOui={showOui}
                    href={href}
                    onClick={onClick}
                    highlighted={highlighted} />
      </span>
  )

}

export default Dot11MacAddress;