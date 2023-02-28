import React from "react";
import moment from "moment/moment";
import TLSCertificateDetailsPrincipal from "./TLSCertificateDetailsPrincipal";

function TLSCertificateDetails(props) {

  const cert = props.cert;

  return (
      <dl className="tls-cert-details mt-3">
        <dt>Valid From</dt>
        <dd>{moment(cert.valid_from).format()}</dd>
        <dt>Expires At</dt>
        <dd>{moment(cert.expires_at).format()}</dd>
        <dt>Fingerprint</dt>
        <dd>{cert.fingerprint.toUpperCase()}</dd>
        <dt>Signature Algorithm</dt>
        <dd>{cert.signature_algorithm}</dd>
        <dt>Subject</dt>
        <dd><TLSCertificateDetailsPrincipal principal={cert.subject} /></dd>
        <dt>Issuer</dt>
        <dd><TLSCertificateDetailsPrincipal principal={cert.issuer} /></dd>
      </dl>
  )

}

export default TLSCertificateDetails;