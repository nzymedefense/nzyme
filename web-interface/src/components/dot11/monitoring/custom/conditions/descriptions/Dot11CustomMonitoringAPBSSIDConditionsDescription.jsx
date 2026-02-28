import React from "react";

export default function Dot11CustomMonitoringAPBSSIDConditionsDescription({type, conditionSet, onConditionRemoved}) {

  const keys = Object.keys(conditionSet);

  if (onConditionRemoved) {
    return (
      <>
        {keys.map((key, i) => (
          <span key={i}>
              <a href="#" onClick={(e) => {e.preventDefault(); onConditionRemoved(conditionSet[key], type)}}>
                {conditionSet[key].bssid}
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
                {conditionSet[key].bssid} {i < keys.length - 1 && (<span className="operator">{" OR "}</span>)}
              </span>
        ))}
      </>
    );
  }

}