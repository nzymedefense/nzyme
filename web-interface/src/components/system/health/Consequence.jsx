import React from "react";
import AcceptableRangeList from "./AcceptableRangeList";
import ConsequencesList from "./ConsequencesList";
import ConsequenceTitle from "./ConsequenceTitle";

function Consequence(props) {

  return (
      <div className={"health-consequence health-consequence-" + props.color}>
        <div className="health-consequence-left">
          <ConsequenceTitle title="Node Clock" color={props.color} />

          <div className="health-consequence-description">
            <strong>Problem:</strong> {props.problem}
          </div>

          <div className="health-consequence-range">
            <strong>Acceptable Range:</strong>
            <AcceptableRangeList range={props.acceptableRange} />
          </div>

          <div className="health-consequence-consequence">
            <strong>Consequences:</strong>

            <ConsequencesList consequences={props.consequences} />
          </div>
        </div>

        <div className="health-consequence-solutions health-consequence-right">
          <strong>Solution Procedure:</strong>

          {props.procedure}
        </div>

        <div style={{clear: "both"}} />
      </div>
  )

}

export default Consequence;