import React from "react";

function Flag(props) {

  const code = props.code.toLowerCase();

  return (
      <span className={"fi fi-" + code + " flag-inline"} />
  )

}

export default Flag;