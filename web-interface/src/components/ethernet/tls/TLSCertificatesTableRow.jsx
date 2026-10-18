import React from "react";
import numeral from "numeral";
import moment from "moment/moment";
import {truncate} from "../../../util/Tools";
import FilterValueIcon from "../../shared/filtering/FilterValueIcon";
import {TLS_CERTIFICATE_FILTER_FIELDS} from "./TLSCertificateFilterFields";
import TLSCertificateFingerprint from "./TLSCertificateFingerprint";

export default function TLSCertificatesTableRow(props) {

  const certificate = props.certificate;
  const setFilters = props.setFilters;

  const filterIcon = (field, value) => {
    if (value === null || value === undefined || value === "") {
      return null;
    }

    return <FilterValueIcon setFilters={setFilters}
                            fields={TLS_CERTIFICATE_FILTER_FIELDS}
                            field={field}
                            value={value} />
  }

  const expired = certificate.not_after && moment(certificate.not_after).isBefore(moment());
  const notYetValid = certificate.not_before && moment(certificate.not_before).isAfter(moment());

  const type = () => {
    if (!certificate.parsed) {
      return <span className="text-muted" title="The certificate could not be parsed.">Unparsed</span>
    }

    const parts = [certificate.is_ca ? "CA" : "Leaf"];
    if (certificate.self_signed) {
      parts.push("Self-signed");
    }
    if (certificate.extended_key_usage.includes("clientAuth") && !certificate.extended_key_usage.includes("serverAuth")) {
      parts.push("Client Auth");
    }

    return parts.join(", ");
  }

  const sans = () => {
    if (!certificate.subject_alternative_names || certificate.subject_alternative_names.length === 0) {
      return <span className="text-muted">None</span>
    }

    const first = certificate.subject_alternative_names[0];
    const more = certificate.subject_alternative_names.length - 1;

    return (
      <span title={certificate.subject_alternative_names.join("\n")}>
        {truncate(first, 30, false)}{more > 0 ? <span className="text-muted"> +{more}</span> : null}
      </span>
    )
  }

  return (
      <tr>
        <td>
          <TLSCertificateFingerprint sha256={certificate.sha256} />
          {filterIcon("fingerprint_sha256", certificate.sha256)}
        </td>
        <td>
          {certificate.common_name ? <span title={certificate.common_name}>{truncate(certificate.common_name, 30, false)}</span> : <span className="text-muted" title={certificate.subject}>No CN</span>}
          {filterIcon("common_name", certificate.common_name)}
        </td>
        <td>
          <span title={certificate.issuer}>
            {certificate.issuer ? truncate(issuerCommonName(certificate.issuer), 30, false) : <span className="text-muted">n/a</span>}
          </span>
          {filterIcon("issuer", certificate.issuer)}
        </td>
        <td className="hide-narrow">{sans()}</td>
        <td className="hide-narrow">{type()}</td>
        <td className="hide-narrow">
          {certificate.not_before ? <span className={notYetValid ? "text-danger" : ""}>{moment(certificate.not_before).format("YYYY-MM-DD")}</span> : <span className="text-muted">n/a</span>}
        </td>
        <td>
          {certificate.not_after ? <span className={expired ? "text-danger" : ""} title={expired ? "Expired" : moment(certificate.not_after).fromNow()}>{moment(certificate.not_after).format("YYYY-MM-DD")}</span> : <span className="text-muted">n/a</span>}
        </td>
        <td className="hide-narrow">
          {certificate.public_key_algorithm ? certificate.public_key_algorithm + (certificate.public_key_bits ? " " + certificate.public_key_bits : "") : <span className="text-muted">n/a</span>}
        </td>
        <td className="hide-narrow">
          {certificate.signature_algorithm ? certificate.signature_algorithm : <span className="text-muted">n/a</span>}
          {filterIcon("signature_algorithm", certificate.signature_algorithm)}
        </td>
        <td>
          {numeral(certificate.session_count).format("0,0")}
          {filterIcon("session_count", certificate.session_count)}
        </td>
        <td className="hide-narrow">
          {certificate.sources.map((source, i) => (
            <React.Fragment key={i}>
              {i > 0 ? ", " : null}
              {sourceName(source)}
              {filterIcon("source", source)}
            </React.Fragment>
          ))}
        </td>
        <td className="hide-narrow">{moment(certificate.first_seen).format()}</td>
        <td>{moment(certificate.last_seen).format()}</td>
      </tr>
  )
}

function sourceName(source) {
  switch (source) {
    case "TLS": return "TLS Sessions";
    case "PORTAL_INTEGRITY": return "Portal Integrity";
    default: return source;
  }
}

// RFC 2253 distinguished name, CN first. Falls back to the whole name.
function issuerCommonName(issuer) {
  const match = issuer.match(/(?:^|,)CN=([^,]+)/);
  return match ? match[1] : issuer;
}
