import React from "react";
import IPAddressLink from "./IPAddressLink";
import Flag from "../../misc/Flag";

export default function L4Address(props) {

  const address = props.address;
  const hidePort = props.hidePort;

  const geoCountryCode = () => {
    if (!address.geo || !address.geo.country_code) {
      return "NONE"
    } else {
      return address.geo.country_code;
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
      <span>
        <Flag code={geoCountryCode() }/>{' '}
        <IPAddressLink ip={address.address} port={hidePort ? null : address.port} />
      </span>
  )

}