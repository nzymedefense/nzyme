import React, {useState} from "react";
import IPAddressLink from "./IPAddressLink";
import Flag from "../../misc/Flag";
import ContextOverlayVisibilityWrapper from "../../shared/context/ContextOverlayVisibilityWrapper";
import L4AddressContextOverlay from "../l4/ip/L4AddressContextOverlay";

export default function L4Address(props) {

  const address = props.address;

  // Optional.
  const hidePort = props.hidePort;
  const hideFlag = props.hideFlag;
  const filterElement = props.filterElement;

  const [overlayTimeout, setOverlayTimeout] = useState(null);
  const [overlayVisible, setOverlayVisible] = useState(false);

  const geoCountryCode = () => {
    if (!address.geo || !address.geo.country_code) {
      return "NONE"
    } else {
      return address.geo.country_code;
    }
  }

  const flag = () => {
    if (hideFlag) {
      return false;
    }

    return <Flag code={geoCountryCode() }/>
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

  if (!address) {
    return (
        <span>
          [missing] <i className="fa-solid fa-circle-question"
                       title="Underlying connection information has been retention cleaned or not recorded."></i>
        </span>
    )
  }

  return (
      <span onMouseEnter={mouseOver} onMouseLeave={mouseOut}>
        {flag()}{' '}
        <IPAddressLink ip={address.address} port={hidePort ? null : address.port} />{' '}
        {filterElement ? filterElement : null}

        <ContextOverlayVisibilityWrapper visible={overlayVisible}
                                         overlay={<L4AddressContextOverlay address={address} />} />
      </span>
  )

}