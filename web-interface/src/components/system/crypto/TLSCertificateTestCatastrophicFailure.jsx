import React from "react";

function TLSCertificateTestCatastrophicFailure(props) {

  if (props.show) {
    return (
        <div className="alert alert-danger mt-4">
          Parsing failed catastrophically and unexpectedly. Please check your nzyme log files. This means that nzyme
          encountered an internal server error. The expected result is a parsed TLS certificate or a description
          of what could not be parsed.
        </div>
    )
  }

  return null

}

export default TLSCertificateTestCatastrophicFailure;