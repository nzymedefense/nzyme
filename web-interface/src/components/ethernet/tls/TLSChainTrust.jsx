import React from "react";

/*
 * Chain trust is null when no certificate was visible (TLS 1.3), true when the chain validated
 * against the public trust store, false otherwise with the reason in the tooltip.
 */
export default function TLSChainTrust(props) {

  const trusted = props.trusted;
  const error = props.error;

  if (trusted === null || trusted === undefined) {
    return null;
  }

  if (trusted) {
    return <span className="text-success">Trusted</span>
  }

  return <span className="text-danger" title={error ? error : "Chain did not validate."}>Untrusted</span>

}
