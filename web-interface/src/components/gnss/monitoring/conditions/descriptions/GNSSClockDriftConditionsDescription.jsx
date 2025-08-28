import React from "react";
import numeral from "numeral";

export default function GNSSClockDriftConditionsDescription({type, conditionSet, onConditionRemoved}) {

  const keys = Object.keys(conditionSet);

  return (
      <>
        {keys.map((key, i) => (
            <span key={i}>
              <a href="#" onClick={(e) => {e.preventDefault(); onConditionRemoved(conditionSet[key], type)}}>
                 Greater than {numeral(conditionSet[key].drift).format("0,0")} ms
              </a>
              {i < keys.length - 1 && (<span className="operator">{" OR "}</span>)}
            </span>
        ))}
      </>
  );

}