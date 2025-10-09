import React from "react";

import numeral from "numeral";
import AutomaticDot11MacAddressLink from "../../shared/context/macs/AutomaticDot11MacAddressLink";
import ApiRoutes from "../../../util/ApiRoutes";
import L4Address from "../../ethernet/shared/L4Address";
import EthernetMacAddress from "../../shared/context/macs/EthernetMacAddress";
import FilterValueIcon from "../../shared/filtering/FilterValueIcon";
import {ARP_FILTER_FIELDS} from "../../ethernet/assets/arp/ARPFilterFields";
import moment from "moment";
import AssetHostnames from "../../ethernet/assets/AssetHostnames";

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
    case "ETHERNET_MAC":
      if (value.metadata) {
        return <EthernetMacAddress addressWithContext={value.metadata} withAssetLink withAssetName />
      } else {
        return "XX"
      }
    case "L4_ADDRESS":
      return <L4Address address={value.value} />
    case "DNS_TRANSACTION_LOG_LINK":
      return <a href={ApiRoutes.ETHERNET.DNS.TRANSACTION_LOGS + "?filters=" + value.metadata.filter_parameters}>{numeral(value.value.title).format("0,0")}</a>
    case "INTEGER":
      return <span className={value.value === highlightValue ? "highlighted" : null}>{numeral(value.value).format("0,0")}</span>
    case "GENERIC":
      return <span className={value.value === highlightValue ? "highlighted" : null}>{value.value}</span>
    case "DATETIME":
      return <span className={value.value === highlightValue ? "highlighted" : null} title={moment(value.value).format()}>
        {moment(value.value).fromNow()}
      </span>
    case "ASSET_HOSTNAMES":
      return <AssetHostnames hostnames={value.value} />
    default:
      return <span>[unknown value type]</span>
  }

}

export default HistogramValue;