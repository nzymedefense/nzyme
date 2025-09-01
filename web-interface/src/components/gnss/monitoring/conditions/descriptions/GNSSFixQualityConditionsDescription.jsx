import React from "react";
import numeral from "numeral";

export default function GNSSFixQualityConditionsDescription({type, conditionSet, onConditionRemoved}) {

  const keys = Object.keys(conditionSet);

  const fixQuality = (q) => {
    switch (q) {
      case "FIX_3D": return "3D";
      case "FIX_2D": return "2D";
      default: return "UNKNOWN";
    }
  }

  if (onConditionRemoved) {
    return (
        <>
          {keys.map((key, i) => (
              <span key={i}>
              <a href="#" onClick={(e) => {
                e.preventDefault();
                onConditionRemoved(conditionSet[key], type)
              }}>
                Fix quality continuously below {fixQuality(conditionSet[key].minimumContinuousFixQuality)} for at
                least {numeral(conditionSet[key].timeframeMinutes).format("0,0")} minutes
              </a>
                {i < keys.length - 1 && (<span className="operator">{" OR "}</span>)}
            </span>
          ))}
        </>
    );
  } else {
    return (
        <>
          {keys.map((key, i) => (
              <span key={i}>
                Fix quality continuously below {fixQuality(conditionSet[key].minimumContinuousFixQuality)} for at
                least {numeral(conditionSet[key].timeframeMinutes).format("0,0")} minutes{' '}
                {i < keys.length - 1 && (<span className="operator">{" OR "}</span>)}
              </span>
          ))}
        </>
    );
  }

}