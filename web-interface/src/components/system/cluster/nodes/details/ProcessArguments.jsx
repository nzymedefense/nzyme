import React from "react";

function ProcessArguments(props) {

  if (!props.arguments || props.arguments.length === 0) {
    return <i>[none]</i>
  } else {
    if (props.arguments.length > 350) {
      return <code>{props.arguments.slice(0, 350)}</code>
    } else {
      return <code>{props.arguments}</code>
    }
  }

}

export default ProcessArguments