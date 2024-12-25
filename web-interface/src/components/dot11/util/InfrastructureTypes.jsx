import React from "react";
import {translateInfrastructureType} from "./InfrastructureTypeTranslator";

function InfrastructureTypes(props) {

  const types = props.types.filter(n => n);

  if (types.length === 0) {
    // Purely wildcard/hidden BSSIDs have none.
    return <span className="text-muted">None</span>
  }

  return (
      <React.Fragment>
        {types.map(function(type, i){
          let x = translateInfrastructureType(type);

          if (i !== types.length-1) {
            x += ", "
          }

          return <span key={"iftype-" + i} title={type}>{x}</span>
        })}
      </React.Fragment>
  )

}

export default InfrastructureTypes;