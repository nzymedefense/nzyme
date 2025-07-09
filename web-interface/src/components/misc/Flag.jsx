import React from "react";

function Flag(props) {

  const code = props.code;

  if (!code || code === "NONE") {
    return <span className="fi fi-none flag-inline" title="No GeoIP Information Resolved."/>
  }

  return (
      <span className={"fi fi-" + code.toLowerCase() + " flag-inline"} title={code} />
  )

}

export default Flag;