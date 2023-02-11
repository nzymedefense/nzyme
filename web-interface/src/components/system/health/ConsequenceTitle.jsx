import React from "react";

function ConsequenceTitle(props) {

  return (
      <h4 className={"health-consequence-" + props.color}>
        Indicator: <span className="health-consequence-indicator">{props.title}</span>
      </h4>
  )

}

export default ConsequenceTitle;