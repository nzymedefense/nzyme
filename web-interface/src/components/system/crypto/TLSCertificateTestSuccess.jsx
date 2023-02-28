import React from "react";
import TLSCertificateDetails from "./TLSCertificateDetails";
import InstallTLSCertificateButton from "./InstallTLSCertificateButton";

function TLSCertificateTestSuccess(props) {

  if (props.result) {
    return (
      <div className="alert alert-success mt-4 tls-cert-success">
        <strong>The uploaded certificate and key file were successfully parsed and can be installed.</strong>

        <TLSCertificateDetails cert={props.result.certificate} />

        <InstallTLSCertificateButton onClick={props.onClick} />
      </div>
    )
  }

  return null;

}

export default TLSCertificateTestSuccess;