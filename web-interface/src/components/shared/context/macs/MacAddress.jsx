import React, {useState} from "react";
import MacAddressContextOverlay from "./details/MacAddressContextOverlay";
import ContextOverlayVisibilityWrapper from "../ContextOverlayVisibilityWrapper";

function MacAddress(props) {

  const address = props.address;

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
        {addressElement()}

        <ContextOverlayVisibilityWrapper
            visible={overlayVisible}
            overlay={<MacAddressContextOverlay address={address} />} />
      </div>
  )

}

export default MacAddress;