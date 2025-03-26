import React from "react";

export default function CotOutputStatus(props) {

  const status = props.status;

  switch (status) {
    case "RUNNING":
      return "Running";
    case "PAUSED":
      return "Paused";
    default:
      return "Unknown/Invalid";
  }

}