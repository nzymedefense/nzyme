import React from "react";
import MacAddress from "./MacAddress";
import EthernetMacAddressContextOverlay from "./details/EthernetMacAddressContextOverlay";

export default function EthernetMacAddress(props) {

  const address = props.address;
  const addressWithContext = props.addressWithContext;
  const showOui = props.showOui;
  const highlighted = props.highlighted;

  // Optional.
  const href = props.href;
  const assetId = props.assetId;
  const onClick = props.onClick;
  const filterElement = props.filterElement;

  return (
      <span className="mac-address">
        <MacAddress address={address}
                    addressWithContext={addressWithContext}
                    overlay={<EthernetMacAddressContextOverlay
                        assetId={assetId}
                        address={addressWithContext ? addressWithContext.address : address}
                        oui={addressWithContext ? addressWithContext.oui : null} />}
                    showOui={showOui}
                    href={href}
                    onClick={onClick}
                    filterElement={filterElement}
                    highlighted={highlighted} />
      </span>
  )

}