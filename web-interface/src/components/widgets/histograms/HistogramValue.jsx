import React from "react";

import numeral from "numeral";
import AutomaticDot11MacAddressLink from "../../shared/context/macs/AutomaticDot11MacAddressLink";
import IPAddressLink from "../../ethernet/shared/IPAddressLink";
import ApiRoutes from "../../../util/ApiRoutes";
import L4Address from "../../ethernet/shared/L4Address";

function HistogramValue(props) {

  const highlightValue = props.highlightValue;
  const value = props.value;

  switch (value.type) {
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
    case "L4_ADDRESS":
      return <L4Address address={value.value} />
    case "DNS_TRANSACTION_LOG_LINK":
      return <a href={ApiRoutes.ETHERNET.DNS.TRANSACTION_LOGS}>{numeral(value.value.title).format("0,0")}</a>
    case "INTEGER":
      return <span className={value.value === highlightValue ? "highlighted" : null}>{numeral(value.value).format("0,0")}</span>
    case "GENERIC":
      return <span className={value.value === highlightValue ? "highlighted" : null}>{value.value}</span>
    default:
      return <span>[unknown value type]</span>
  }

}

export default HistogramValue;