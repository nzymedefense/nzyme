import React from "react";

function ConsequencesList(props) {

  if (!props.consequences) {
    return <ul><li>No information available.</li></ul>
  } else {
    return (
        <ul>
          {props.consequences.map(function (key, i) {
            return <li key={"conq-" + i}>{key}</li>
          })}
        </ul>
    )
  }

}

export default ConsequencesList;