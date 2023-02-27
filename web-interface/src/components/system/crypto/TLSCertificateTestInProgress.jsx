import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";

function TLSCertificateTestInProgress(props) {

  if (props.show) {
    return (
        <div className="mt-4">
          <LoadingSpinner />
        </div>
    )
  }

  return null

}

export default TLSCertificateTestInProgress;