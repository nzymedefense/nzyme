import React from "react";

export const ACCESS_POINT = "ACCESS_POINT";
export const CLIENT = "CLIENT";
export const MULTIPLE = "MULTIPLE";
export const UNKNOWN = "UNKNOWN";

function Dot11MacAddressType(props) {

  const type = props.type;

  if (!type) {
    return null;
  }

  switch (type) {
    case ACCESS_POINT:
      return (
          <span className="mac-address-icon-container">
            <i className="fa-solid fa-tower-broadcast text-muted" title="Access Point"></i>
          </span>
      )
    case CLIENT:
      return (
          <span className="mac-address-icon-container">
            <i className="fa-regular fa-user text-muted" title="Client"></i>
          </span>
      )
    case MULTIPLE:
      return (
          <span className="mac-address-icon-container">
            <i className="fa-solid fa-triangle-exclamation text-danger" title="Multiple Types Detected. Spoofing?"></i>
          </span>
      )
    case UNKNOWN:
      return (
          <span className="mac-address-icon-container">
            <i className="fa-regular fa-circle-question text-muted" title="Unknown Type"></i>
          </span>
      )
  }

}

export default Dot11MacAddressType;