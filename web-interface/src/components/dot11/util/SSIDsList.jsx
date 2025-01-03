import React from "react";
import FilterValueIcon from "../../shared/filtering/FilterValueIcon";
import {BSSID_FILTER_FIELDS} from "../bssids/BssidFilterFields";

function SSIDsList(props) {

  const ssids = props.ssids;
  const ssidsString = ssids.join(", ");

  // Optional.
  const setFilters = props.setFilters;

  const filterIcon = (value) => {
    if (!setFilters) {
      return null;
    }

    return <FilterValueIcon setFilters={setFilters}
                            fields={BSSID_FILTER_FIELDS}
                            field="advertised_ssid"
                            value={value}/>
  }

  // A single SSID has a filter icon.
  if (ssids.length === 1) {
    return (
      <React.Fragment>
        <span>{ssids[0]}</span>{filterIcon(ssids[0])}
      </React.Fragment>
    )
  }

  if (ssidsString.length > 50) {
    return ssidsString.slice(0, 49) + "\u2026"
  } else {
    return ssidsString
  }

}

export default SSIDsList;