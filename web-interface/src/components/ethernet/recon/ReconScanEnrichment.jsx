import React from "react";
import numeral from "numeral";

export default function ReconScanEnrichment({scan}) {

  const e = scan.enrichment;

  const NO_DATA = <div className="alert alert-info mb-0">No probe details were recorded for this scan.</div>;

  if (!e || Object.keys(e).length === 0) {
    return NO_DATA;
  }

  const row = (label, value, hint = undefined) => {
    if (value === undefined || value === null || value === "") {
      return null;
    }

    return (
      <React.Fragment>
        <dt title={hint}>{label}</dt>
        <dd className="machine-data">{String(value)}</dd>
      </React.Fragment>
    )
  }

  if (scan.type === "ARP_SWEEP") {
    if (e.request_count === undefined || e.request_count === null) {
      return NO_DATA;
    }

    return (
      <dl className="mb-0">
        <dt>ARP Requests</dt>
        <dd className="mb-0">{numeral(e.request_count || 0).format("0,0")}</dd>
      </dl>
    )
  }

  // The type is always present. Without any packet attribute there is nothing to show.
  const hasAttributes = ["ip_ttl", "ip_df", "tcp_window_size", "tcp_mss", "tcp_options", "fingerprint"]
    .some((key) => e[key] !== undefined && e[key] !== null && e[key] !== "");

  if (!hasAttributes) {
    return NO_DATA;
  }

  return (
    <React.Fragment>
      <p className="text-muted">
        Header fields of the probe packets, showing for each field the value the scanner used most often.
      </p>

      <dl className="mb-0">
        {row("IP TTL", e.ip_ttl)}
        {row("IP Don't Fragment", e.ip_df === undefined ? undefined : (e.ip_df ? "Set" : "Not set"))}
        {row("TCP Window Size", e.tcp_window_size)}
        {row("TCP MSS", e.tcp_mss)}
        {row("TCP Options", e.tcp_options)}
        {row("Fingerprint", e.fingerprint)}
        {e.fingerprint_count !== undefined && e.fingerprint_count > 1 ?
          <React.Fragment>
            <dt>Distinct Fingerprints</dt>
            <dd className="mb-0">
              {numeral(e.fingerprint_count).format("0,0")}{' '}
              <span className="text-muted">(the device used more than one packet shape)</span>
            </dd>
          </React.Fragment> : null}
      </dl>
    </React.Fragment>
  )

}
