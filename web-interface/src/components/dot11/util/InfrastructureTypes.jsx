import React from "react";
import {translateInfrastructureType} from "./InfrastructureTypeTranslator";
import FilterValueIcon from "../../shared/filtering/FilterValueIcon";
import {BSSID_FILTER_FIELDS} from "../bssids/BssidFilterFields";

function InfrastructureTypes(props) {

  const types = props.types.filter(n => n);

  // Optional.
  const setFilters = props.setFilters;

  const filterIcon = (value) => {
    if (!setFilters) {
      return null;
    }

    return <FilterValueIcon setFilters={setFilters}
                       fields={BSSID_FILTER_FIELDS}
                       field="mode"
                       value={value}/>
  }

  if (types.length === 0) {
    // Purely wildcard/hidden BSSIDs have none.
    return <span className="text-muted">None</span>
  }

  return (
      <React.Fragment>
        {types.map(function(type, i) {
          let x = translateInfrastructureType(type);

          if (i !== types.length-1) {
            x += ", "
          }

          return (
            <React.Fragment key={i}>
              <span key={"iftype-" + i} title={type}>{x}</span>
              {filterIcon(x)}
            </React.Fragment>
          )
        })}
      </React.Fragment>
  )

}

export default InfrastructureTypes;