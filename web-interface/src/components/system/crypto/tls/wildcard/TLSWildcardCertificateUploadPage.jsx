import React, {useState} from "react";
import Routes from "../../../../../util/ApiRoutes";
import TLSCertificateHelp from "../TLSCertificateHelp";
import TLSCertificateUploadForm from "../form/TLSCertificateUploadForm";
import CryptoService from "../../../../../services/CryptoService";
import {notify} from "react-notify-toast";
import {Navigate} from "react-router-dom";
import MatchingNodesTestResult from "./MatchingNodesTestResult";
import LoadingSpinner from "../../../../misc/LoadingSpinner";

const cryptoService = new CryptoService();

function TLSWildcardCertificateUploadPage() {

  const [certInstallationSuccess, setCertInstallationSuccess] = useState(false)

  const [regexValue, setRegexValue] = useState(undefined);
  const [matchingNodesTestRunning, setMatchingNodesTestRunning] = useState(false);
  const [matchingNodes, setMatchingNodes] = useState(null)

  const testNodeMatcherButtonActive = function() {
    return regexValue && regexValue.length > 0;
  }

  const updateRegex = function(e) {
    setMatchingNodes(null);
    setRegexValue(e.target.value);
  }

  const testNodeMatcher = function() {
    setMatchingNodesTestRunning(true);

    cryptoService.testTLSWildcardCertificateNodeMatcher(regexValue, function(response) {
      setMatchingNodesTestRunning(false);
      setMatchingNodes(response.data);
    });
  }

  const installCertificate = function(formData) {
    if (!confirm("Really install wildcard TLS certificate? This will replace the existing " +
        "certificate on all nodes that match the regular expression. Note that the nzyme HTTP server on those node will " +
        "restart and you will likely experience a brief loss of connection.")) {
      return;
    }

    formData.append("node_matcher", regexValue);

    cryptoService.installWildcardTLSCertificate(formData, function() {
      notify.show('TLS certificate installed.', 'success');
      setCertInstallationSuccess(true);
    })
  }

  if (certInstallationSuccess) {
    return <Navigate to={Routes.SYSTEM.CRYPTO.INDEX} />
  }

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

                <div className="mb-3">
                  <label htmlFor="fu-certificate" className="form-label">
                    Node Matcher (Regular Expression)
                  </label>
                  <div className="input-group mb-3">
                    <input className="form-control form-control-sm" name="node_matcher"
                           onChange={updateRegex} type="text" />
                    <button className="btn btn-sm btn-secondary"
                            onClick={testNodeMatcher}
                            disabled={!testNodeMatcherButtonActive()}>
                      Test Regex
                    </button>
                  </div>

                  <LoadingSpinner show={matchingNodesTestRunning} />
                  <MatchingNodesTestResult matchingNodes={matchingNodes} />
                </div>

                <TLSCertificateUploadForm onInstall={installCertificate} />
              </div>
            </div>
          </div>
        </div>
      </div>
  )

}

export default TLSWildcardCertificateUploadPage;