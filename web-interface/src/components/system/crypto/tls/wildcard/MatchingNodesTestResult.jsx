import React from "react";
import MatchingNodes from "./MatchingNodes";

function MatchingNodesTestResult(props) {

  const matchingNodes = props.matchingNodes;

  if (matchingNodes === undefined || matchingNodes === null) {
    return null;
  }

  return (
      <MatchingNodes nodes={matchingNodes} />
  )

}

export default MatchingNodesTestResult;