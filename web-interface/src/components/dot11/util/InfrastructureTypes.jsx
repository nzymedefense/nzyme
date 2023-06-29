import React from "react";

function InfrastructureTypes(props) {

  const types = props.types.filter(n => n);

  if (types.length === 0) {
    // Purely wildcard/hidden BSSIDs have none.
    return <span className="hidden-ssid">None</span>
  }

  return (
      <React.Fragment>
        {types.map(function(type, i){
          let x = ""
          switch (type) {
            case "accesspoint":
              x = "Infrastructure";
              break;
            case "adhoc":
              x = "Ad-Hoc";
              break;
            case "invalid":
              x = "Unknown/Invalid";
              break;
          }

          if (i !== types.length-1) {
            x += ", "
          }

          return <span title={type}>{x}</span>
        })}
      </React.Fragment>
  )

}

export default InfrastructureTypes;