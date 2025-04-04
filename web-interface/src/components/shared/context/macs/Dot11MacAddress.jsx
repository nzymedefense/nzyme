import React from "react";
import MacAddress from "./MacAddress";
import Dot11MacAddressContextOverlay from "./details/Dot11MacAddressContextOverlay";

function Dot11MacAddress(props) {

  const address = props.address;
  const addressWithContext = props.addressWithContext;
  const type = props.type;
  const showOui = props.showOui;
  const highlighted = props.highlighted;

  // Optional.
  const href = props.href;
  const onClick = props.onClick;
  const filterElement = props.filterElement;

  return (
      <span className="mac-address">
        <MacAddress address={address}
                    addressWithContext={addressWithContext}
                    overlay={<Dot11MacAddressContextOverlay
                        address={addressWithContext ? addressWithContext.address : address}
                        isRandomized={addressWithContext ? addressWithContext.is_randomized : address.is_randomized}
                        oui={addressWithContext ? addressWithContext.oui : null} />}
                    type={type}
                    showOui={showOui}
                    href={href}
                    onClick={onClick}
                    filterElement={filterElement}
                    highlighted={highlighted} />
      </span>
  )

}

export default Dot11MacAddress;