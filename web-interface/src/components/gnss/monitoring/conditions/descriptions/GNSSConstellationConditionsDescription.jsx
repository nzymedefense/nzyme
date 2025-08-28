import React from "react";

export default function GNSSConstellationConditionsDescription({type, conditionSet, onConditionRemoved}) {

  const keys = Object.keys(conditionSet);

  return (
      <>
        {keys.map((key, i) => (
            <span key={i}>
              <a href="#" onClick={(e) => {e.preventDefault(); onConditionRemoved(conditionSet[key], type)}}>
                {conditionSet[key].constellation}
              </a>
              {i < keys.length - 1 && (<span className="operator">{" OR "}</span>)}
            </span>
        ))}
      </>
  );

}