import React from "react";

function AcceptableRangeList(props) {

  if (!props.range) {
    return <ul><li>No information available.</li></ul>
  } else {
    return (
        <ul>
          {props.range.map(function (key, i) {
            return <li key={"ar-" + i}>{key}</li>
          })}
        </ul>
    )
  }

}

export default AcceptableRangeList;