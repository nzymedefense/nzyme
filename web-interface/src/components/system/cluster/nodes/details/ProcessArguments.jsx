import React, {useState} from "react";

function ProcessArguments(props) {

  const [expanded, setExpanded] = useState(false)

  const onExpandClick = function () {
    setExpanded(!expanded);
  }

  if (!props.arguments || props.arguments.length === 0) {
    return <i>[none]</i>
  } else {
    if (props.arguments.length <= 350 || expanded) {
      return <code>{props.arguments}</code>
    } else {
      return (
          <div className="node-process-arguments">
            <code>{props.arguments.slice(0, 350)}</code>{' '}
            <button className="btn btn-link" onClick={onExpandClick}>[expand]</button>
          </div>
      )
    }
  }

}

export default ProcessArguments