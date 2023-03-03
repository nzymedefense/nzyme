import React from "react";
import Routes from "../../../util/ApiRoutes";
import TLSCertificateHelp from "./TLSCertificateHelp";

function TLSWildcardCertificateUploadPage() {

  return (
      <div>
        <div className="row">
          <div className="col-md-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={Routes.SYSTEM.CRYPTO.INDEX}>Crypto</a></li>
                <li className="breadcrumb-item">TLS</li>
                <li className="breadcrumb-item active" aria-current="page">Upload TLS Wildcard Certificate</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end" href={Routes.SYSTEM.CRYPTO.INDEX}>Back</a>
          </div>

          <div className="col-md-12">
            <h1>
              Upload TLS Wildcard Certificate
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Upload TLS Wildcard Certificate</h3>

                <p>
                  Use this form to upload a TLS wildcard certificate. Every node name that matches the regular
                  expression below will be automatically provisioned with the uploaded wildcard TLS certificate.
                  Wildcard certificates take precedence over individual certificates.
                </p>

                <TLSCertificateHelp />
              </div>
            </div>
          </div>
        </div>
      </div>
  )

}

export default TLSWildcardCertificateUploadPage;