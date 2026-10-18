import React from "react";
import moment from "moment/moment";
import {truncate} from "../../../util/Tools";
import TLSCertificateFingerprint from "./TLSCertificateFingerprint";

export default function TLSChainTable({chain}) {

  if (!chain || chain.length === 0) {
    return <div className="mb-0 alert alert-info">No certificates observed.</div>
  }

  const type = (c) => {
    if (!c.parsed) {
      return <span className="text-muted">Unparsed</span>
    }

    const parts = [c.is_ca ? "CA" : "Leaf"];
    if (c.self_signed) {
      parts.push("Self-signed");
    }

    return parts.join(", ");
  }

  const name = (value) => {
    if (!value) {
      return <span className="text-muted">n/a</span>
    }

    return <span title={value}>{truncate(value, 40, false)}</span>
  }

  const date = (value, highlightPast) => {
    if (!value) {
      return <span className="text-muted">n/a</span>
    }

    const past = moment(value).isBefore(moment());
    return <span className={highlightPast && past ? "text-danger" : ""} title={moment(value).format()}>{moment(value).format("YYYY-MM-DD")}</span>
  }

  return (
      <table className="table table-sm table-hover table-striped mb-0">
        <thead>
        <tr>
          <th>#</th>
          <th>Fingerprint</th>
          <th>Subject</th>
          <th>Issuer</th>
          <th className="hide-narrow">Type</th>
          <th className="hide-narrow">Key</th>
          <th className="hide-narrow">Signature</th>
          <th className="hide-narrow">Valid From</th>
          <th>Valid Until</th>
        </tr>
        </thead>
        <tbody>
        {chain.map((c, i) => (
          <tr key={c.sha256}>
            <td>{i + 1}</td>
            <td><TLSCertificateFingerprint sha256={c.sha256} /></td>
            <td>{name(c.common_name ? c.common_name : c.subject)}</td>
            <td>{name(issuerCommonName(c.issuer))}</td>
            <td className="hide-narrow">{type(c)}</td>
            <td className="hide-narrow">
              {c.public_key_algorithm ? c.public_key_algorithm + (c.public_key_bits ? " " + c.public_key_bits : "") : <span className="text-muted">n/a</span>}
            </td>
            <td className="hide-narrow">{c.signature_algorithm ? c.signature_algorithm : <span className="text-muted">n/a</span>}</td>
            <td className="hide-narrow">{date(c.not_before, false)}</td>
            <td>{date(c.not_after, true)}</td>
          </tr>
        ))}
        </tbody>
      </table>
  )

}

// RFC 2253 distinguished name, CN first. Falls back to the whole name.
export function issuerCommonName(issuer) {
  if (!issuer) {
    return null;
  }

  const match = issuer.match(/(?:^|,)CN=([^,]+)/);
  return match ? match[1] : issuer;
}
