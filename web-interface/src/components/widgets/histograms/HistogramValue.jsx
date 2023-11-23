import React from "react";

import numeral from "numeral";
import AutomaticDot11MacAddressLink from "../../shared/context/macs/AutomaticDot11MacAddressLink";

function HistogramValue(props) {

  const highlightValue = props.highlightValue;
  const value = props.value;

  let result;

  switch(value.type) {
    case "DOT11_MAC":
      if (value.metadata && value.metadata.type) {
        result = <AutomaticDot11MacAddressLink mac={value.value} type={value.metadata.type} />
      } else {
        result = <span>{value.value}</span>
      }
      break;
    case "INTEGER":
      result = <span>{numeral(value.value).format("0,0")}</span>
      break;
    case "GENERIC":
    default:
      result = <span>{value.value}</span>
  }

  if (value.value === highlightValue) {
    return <span className="highlighted">{result}</span>
  } else {
    return result;
  }

}

export default HistogramValue;