import React from "react";

export default function UavSerialNumberMatch(props) {

  const matchType = props.matchType;
  const matchValue = props.matchValue;

  if (matchType === "EXACT") {
    return <span className="serial-number-match">{matchValue}</span>
  } else if (matchType === "PREFIX") {
    return <React.Fragment>
      <span className="serial-number-match">{matchValue}</span> <span className="text-muted">(Prefix)</span>
    </React.Fragment>
  } else {
    return "INVALID";
  }

}