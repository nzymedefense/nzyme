import React from "react";

export const reconTargetOutcomeToName = (outcome) => {
  switch (outcome) {
    case "OPEN": return "Answered";
    case "CLOSED": return "Refused";
    case "UNANSWERED": return "Unanswered";
    default: return outcome;
  }
}

const outcomeToClassName = (outcome) => {
  switch (outcome) {
    case "OPEN": return "text-success";
    case "CLOSED": return "text-warning";
    default: return "text-muted";
  }
}

export default function ReconScanOutcome({outcome}) {
  return <span className={outcomeToClassName(outcome)} title={outcome}>{reconTargetOutcomeToName(outcome)}</span>
}
