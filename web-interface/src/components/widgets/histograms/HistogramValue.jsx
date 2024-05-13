import React from "react";

import numeral from "numeral";
import AutomaticDot11MacAddressLink from "../../shared/context/macs/AutomaticDot11MacAddressLink";
import IPAddressLink from "../../ethernet/shared/IPAddressLink";

function HistogramValue(props) {

  const highlightValue = props.highlightValue;
  const value = props.value;

  switch(value.type) {
    case "DOT11_MAC":
      if (value.metadata && value.metadata.type) {
        return <AutomaticDot11MacAddressLink
            highlighted={value.value === highlightValue}
            mac={value.value}
            type={value.metadata.type}
            addressWithContext={value.metadata.mac} />
      } else {
        return <span className={value.value === highlightValue ? "highlighted" : null}>{value.value}</span>
      }
    case "IP_ADDRESS":
      return <IPAddressLink ip={value.value} />
    case "INTEGER":
      return <span className={value.value === highlightValue ? "highlighted" : null}>{numeral(value.value).format("0,0")}</span>
    case "GENERIC":
    default:
      return <span className={value.value === highlightValue ? "highlighted" : null}>{value.value}</span>
  }

}

export default HistogramValue;