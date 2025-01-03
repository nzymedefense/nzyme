import React from "react";
import FilterValueIcon from "../../shared/filtering/FilterValueIcon";
import {BSSID_FILTER_FIELDS} from "../bssids/BssidFilterFields";

function Dot11SecurityProtocol(props) {

  const protocol = props.protocol;

  // Optional.
  const setFilters = props.setFilters;

  const filterIcon = (value) => {
    if (!setFilters) {
      return null;
    }

    return <FilterValueIcon setFilters={setFilters}
                            fields={BSSID_FILTER_FIELDS}
                            field="security"
                            value={value}/>
  }

  if (!protocol || protocol === "None") {
    return "None/Unknown"
  }

  let name;
  switch (protocol) {
    case "WEP":
      name = "WEP";
      break;
    case "WPA1":
      name = "WPA1";
      break;
    case "WPA2Personal":
      name = "WPA2-Personal";
      break;
    case "WPA2Enterprise":
      name = "WPA2-Enterprise";
      break;
    case "WPA3Transition":
      name = "WPA3-Transition";
      break;
    case "WPA3Personal":
      name = "WPA3-Personal";
      break;
    case "WPA3Enterprise":
      name = "WPA3-Enterprise";
      break;
    case "WPA3EnterpriseCNSA":
      name = "WPA3-Enterprise (CNSA/192)";
      break;
    default:
      name = protocol
  }

  return (
    <React.Fragment>
      <span>{name}</span>{filterIcon(name)}
    </React.Fragment>
  )

}

export default Dot11SecurityProtocol;