import React from "react";

export default function GenericConnectionStatus(props) {

  const status = props.status;

  switch (status) {
    case "Active":
      return <span className="badge bg-success">Active</span>
    case "Inactive":
      return <span className="badge bg-warning">Inactive</span>
    case "InactiveTimeout":
      return <span className="badge bg-warning">TCP Timeout</span>
    default:
      return <span className="badge bg-secondary">Invalid</span>
  }

}