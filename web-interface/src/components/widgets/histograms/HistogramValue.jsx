import React from "react";

import numeral from "numeral";
import AutomaticDot11MacAddressLink from "../../shared/context/macs/AutomaticDot11MacAddressLink";
import IPAddressLink from "../../ethernet/shared/IPAddressLink";
import ApiRoutes from "../../../util/ApiRoutes";

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
    case "IP_ADDRESS":
      return <IPAddressLink ip={value.value} />
    case "IP_ADDRESS_WITH_PORT":
      return <IPAddressLink ip={value.value.ip_address} port={value.value.port} />
    case "DNS_TRANSACTION_LOG_LINK":
      return <a href={ApiRoutes.ETHERNET.DNS.TRANSACTION_LOGS}>{value.value.title}</a>
    case "INTEGER":
      return <span className={value.value === highlightValue ? "highlighted" : null}>{numeral(value.value).format("0,0")}</span>
    case "GENERIC":
      return <span className={value.value === highlightValue ? "highlighted" : null}>{value.value}</span>
    default:
      return <span>[unknown value type]</span>
  }

}

export default HistogramValue;