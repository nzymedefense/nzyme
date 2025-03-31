import React from "react";

export default function CotOutputStatus(props) {

  const status = props.status;

  switch (status) {
    case "RUNNING":
      return <span className="text-success">Running</span>;
    case "PAUSED":
      return <span className="text-warning">Paused</span>;
    default:
      return <span className="text-error">Unknown/Invalid</span>;
  }

}