import React from "react";
import Dot11MacAddressType from "./Dot11MacAddressType";

function Dot11MacAddress(props) {

  const mac = props.mac;
  const type = props.type;

  return <span className="dot11-mac"><Dot11MacAddressType type={type} />{mac}</span>

}

export default Dot11MacAddress;