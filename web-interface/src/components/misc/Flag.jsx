import React from "react";

function Flag(props) {

  const code = props.code;

  if (!code) {
    return null;
  }

  return (
      <span className={"fi fi-" + code.toLowerCase() + " flag-inline"} />
  )

}

export default Flag;