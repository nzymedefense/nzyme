import React, {useState} from "react";
import MacAddressContextOverlay from "./details/MacAddressContextOverlay";
import ContextOverlayVisibilityWrapper from "../ContextOverlayVisibilityWrapper";

function MacAddress(props) {

  const address =  props.addressWithContext ? props.addressWithContext.address : props.address;
  const context = props.addressWithContext ?  props.addressWithContext.context : null;

  const href = props.href;
  const onClick = props.onClick;

  const [overlayTimeout, setOverlayTimeout] = React.useState(null);
  const [overlayVisible, setOverlayVisible] = useState(false);

  const addressElement = () => {
    if (href || onClick) {
      return <a href={href ? href : "#"} onClick={onClick ? onClick : () => {}}>{address}</a>
    } else {
      return address;
    }
  }

  const contextElement = () => {
    if (!context) {
      return null;
    }

    return <i className="fa-solid fa-fingerprint additional-context-available"
              title="Additional context available." />
  }

  const mouseOver = () => {
    setOverlayVisible(false);
    setOverlayTimeout(setTimeout(() => setOverlayVisible(true), 1000));
  }

  const mouseOut = () => {
    setOverlayVisible(false);
    if (overlayTimeout) {
      clearTimeout(overlayTimeout);
    }
  }

  return (
      <div onMouseEnter={mouseOver} onMouseLeave={mouseOut}>
        {addressElement()} {contextElement()}

        <ContextOverlayVisibilityWrapper
            visible={overlayVisible}
            overlay={<MacAddressContextOverlay address={address} />} />
      </div>
  )

}

export default MacAddress;