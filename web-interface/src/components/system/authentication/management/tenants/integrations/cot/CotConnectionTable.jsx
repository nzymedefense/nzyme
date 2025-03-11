import React from "react";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";

export default function CotConnectionTable(props) {

  const connections = props.connections;

  if (!connections) {
    return <LoadingSpinner />
  }

  return "connections"

}