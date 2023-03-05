import React, {useState} from "react";

function InstallTLSCertificateButton(props) {

  const [submitting, setSubmitting] = useState(false);

  const submit = function() {
    setSubmitting(true);
    props.onClick();
  }

  if (!submitting) {
    return (
        <button className={"btn btn-primary tls-cert-install"} onClick={submit}>
          Install Certificate
        </button>
    )
  } else {
    return (
        <button className={"btn btn-primary tls-cert-install"} disabled={true}>
          <span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Installing ...</span>
        </button>
    )
  }

}

export default InstallTLSCertificateButton;