import React from 'react';

export default function L4SessionState(props) {

  const state = props.state;

  switch (state) {
    case "ACTIVE":
    case "ESTABLISHED":
      // Open.
      return <span className="badge bg-success" title={"Established (" + props.state + ")"}>ESTB</span>
    case "SYNSENT":
    case "SYNRECEIVED":
      // Setup.
      return <span className="badge bg-info" title={"Setting Up (" + props.state + ")"}>SETP</span>
    case "FINWAIT1":
    case "FINWAIT2":
      // Teardown.
      return <span className="badge bg-warning" title={"Tearing Down (" + props.state + ")"}>TDWN</span>
    case "CLOSED":
    case "CLOSEDNODE":
    case "CLOSEDFIN":
    case "CLOSEDRST":
    case "CLOSEDTIMEOUT":
    case "CLOSEDTIMEOUTNODE":
      // Closed.
      return <span className="badge bg-warning" title={"Closed (" + props.state + ")"}>CLSD</span>
    case "REFUSED":
      // Refused.
      return <span className="badge bg-danger" title={"Refused (" + props.state + ")"}>RFSD</span>
    default:
      return <span className="badge bg-black" title={"Unknown (" + props.state + ")"}>UNKN</span>;
  }

}