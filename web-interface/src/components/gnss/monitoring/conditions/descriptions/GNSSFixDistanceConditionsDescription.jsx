import React from "react";
import numeral from "numeral";

export default function GNSSFixDistanceConditionsDescription({type, conditionSet, onConditionRemoved}) {

  const keys = Object.keys(conditionSet);

  if (onConditionRemoved) {
    return (
        <>
          {keys.map((key, i) => (
              <span key={i}>
              <a href="#" onClick={(e) => {
                e.preventDefault();
                onConditionRemoved(conditionSet[key], type)
              }}>
                 Greater than {numeral(conditionSet[key].distanceMeters).format("0,0")} meters
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
                Greater than {numeral(conditionSet[key].distanceMeters).format("0,0")} meters{' '}
                {i < keys.length - 1 && (<span className="operator">{" OR "}</span>)}
              </span>
          ))}
        </>
    );
  }

}