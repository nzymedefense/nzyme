import React from 'react';

export default function L4SessionState({state, showFull}) {

  switch (state) {
    case "ACTIVE":
    case "ESTABLISHED":
      // Open.
      return <span className={showFull ? null : "badge bg-success"} title={"Established (" + state + ")"}>{showFull ? "Established (ESTB)" : "ESTB"}</span>
    case "SYNSENT":
    case "SYNRECEIVED":
      // Setup.
      return <span className={showFull ? null : "badge bg-info"} title={"Setting Up (" + state + ")"}>{showFull ? "Setting Up (SETP)" : "SETP"}</span>
    case "FINWAIT1":
    case "FINWAIT2":
      // Teardown.
      return <span className={showFull ? null : "badge bg-warning"} title={"Tearing Down (" + state + ")"}>{showFull ? "Tearing Down (TDWN)" : "TDWN"}</span>
    case "CLOSED":
    case "CLOSEDNODE":
    case "CLOSEDFIN":
    case "CLOSEDRST":
    case "CLOSEDTIMEOUT":
    case "CLOSEDTIMEOUTNODE":
      // Closed.
      return <span className={showFull ? null : "badge bg-warning"} title={"Closed (" + state + ")"}>{showFull ? "Closed (CLSD)" : "CLSD"}</span>
    case "REFUSED":
      // Refused.
      return <span className={showFull ? null : "badge bg-danger"} title={"Refused (" + state + ")"}>{showFull ? "Refused (RFSD)" : "RFSD"}</span>
    default:
      return <span className={showFull ? null : "badge bg-black"} title={"Unknown (" + state + ")"}>{showFull ? "Unknown (UNKN)" : "UNKN"}</span>;
  }

}