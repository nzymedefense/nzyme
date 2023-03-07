import React from "react";
import moment from "moment/moment";
import TLSCertificateDetailsPrincipal from "./TLSCertificateDetailsPrincipal";
import TLSCertificateSourceType from "./TLSCertificateSourceType";
import ExpirationDate from "./ExpirationDate";
import ValidityDate from "./ValidityDate";

function TLSCertificateDetails(props) {

  const cert = props.cert;

  const validFrom = moment(cert.valid_from);
  const expiresAt = moment(cert.expires_at);

  return (
      <dl className="tls-cert-details mt-3">
        <dt>Source/Type</dt>
        <dd><TLSCertificateSourceType type={cert.sourcetype} /></dd>
        <dt>Valid From</dt>
        <dd><ValidityDate date={validFrom} /> ({validFrom.fromNow()})</dd>
        <dt>Expires At</dt>
        <dd><ExpirationDate date={expiresAt} /> ({expiresAt.fromNow()})</dd>
        <dt>Fingerprint</dt>
        <dd>{cert.fingerprint.toUpperCase()}</dd>
        <dt>Signature Algorithm</dt>
        <dd>{cert.signature_algorithm}</dd>
        <dt>Subject <i className="fa-solid fa-angle-down"></i></dt>
        <dd><TLSCertificateDetailsPrincipal principal={cert.subject} /></dd>
        <dt>Issuer <i className="fa-solid fa-angle-down"></i></dt>
        <dd><TLSCertificateDetailsPrincipal principal={cert.issuer} /></dd>
      </dl>
  )

}

export default TLSCertificateDetails;