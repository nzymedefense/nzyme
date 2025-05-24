import React from "react";

export default function DHCPTransactionSuccess(props) {

  const success = props.success;

  if (success == null) {
    return <span className="text-muted">n/a</span>;
  }

  if (success) {
    return <span className="text-success">Success</span>;
  } else {
    return <span className="text-danger">Failure</span>;
  }

}