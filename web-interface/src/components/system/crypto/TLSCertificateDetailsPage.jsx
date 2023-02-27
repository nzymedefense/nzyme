import React, {useEffect, useRef, useState} from "react";
import {useParams} from "react-router-dom";
import ClusterService from "../../../services/ClusterService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Routes from "../../../util/ApiRoutes";
import CryptoService from "../../../services/CryptoService";
import moment from "moment";
import {notify} from "react-notify-toast";
import TLSCertificateTestCatastrophicFailure from "./TLSCertificateTestCatastrophicFailure";
import TLSCertificateTestFailure from "./TLSCertificateTestFailure";
import TLSCertificateTestInProgress from "./TLSCertificateTestInProgress";

const clusterService = new ClusterService()
const cryptoService = new CryptoService();

function TLSCertificateDetailsPage(props) {

  const { nodeUUID } = useParams()

  const [node, setNode] = useState(null)
  const [certificate, setCertificate] = useState(null)

  const [individualCertTestInProgress, setIndividualCertTestInProgress] = useState(false)
  const [individualCertTestSuccessResult, setIndividualCertTestSuccessResult] = useState(null)
  const [individualCertTestFailureResult, setIndividualCertTestFailureResult] = useState(null)
  const [individualCertTestCatastrophicFailure, setIndividualCertTestCatastrophicFailure] = useState(null)

  const fileIndividualCert = useRef(null);
  const fileIndividualKey = useRef(null);

  useEffect(() => {
    clusterService.findNode(nodeUUID, setNode)
    cryptoService.findTLSCertificateOfNode(nodeUUID, setCertificate)
  }, [nodeUUID, setNode])

  const regenerateSelfSignedCertificate = function() {
    if (!confirm("Really re-generate self-signed TLS certificate for this node? This will replace the existing " +
        "certificate. Note that the nzyme HTTP server on this node will restart and you will likely experience a brief " +
        "loss of connection.")) {
      return;
    }

    cryptoService.regenerateSelfSignedTLSCertificate(nodeUUID, function() {
      notify.show('TLS certificate re-generated.', 'success')
    });
  }

  const individualCertificateFormComplete = function() {
    return fileIndividualCert.current && fileIndividualCert.current.files && fileIndividualCert.current.files[0]
      && fileIndividualKey.current && fileIndividualKey.current.files && fileIndividualKey.current.files[0];
  }

  const testIndividualCertificate = function() {
    setIndividualCertTestInProgress(true);
    const formData = new FormData();
    formData.append("certificate", fileIndividualCert.current.files[0]);
    formData.append("private_key", fileIndividualKey.current.files[0]);

    setIndividualCertTestSuccessResult(null);
    setIndividualCertTestFailureResult(null);
    setIndividualCertTestCatastrophicFailure(null);

    cryptoService.testTLSCertificate(nodeUUID, formData, function (response){
      // Test succeeded
      setIndividualCertTestInProgress(false);
      setIndividualCertTestSuccessResult(response.data);
    }, function (response) {
      setIndividualCertTestInProgress(false);
      if (response.status === 401) {
        setIndividualCertTestFailureResult(response.data);
      } else {
        setIndividualCertTestCatastrophicFailure(true);
      }
    })
  }

  if (!node || !certificate) {
    return <LoadingSpinner />
  }

  return (
      <div>
        <div className="row">
          <div className="col-md-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={Routes.SYSTEM.CRYPTO.INDEX}>Crypto</a></li>
                <li className="breadcrumb-item">TLS</li>
                <li className="breadcrumb-item active" aria-current="page">{node.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end" href={Routes.SYSTEM.CRYPTO.INDEX}>Back</a>
          </div>

          <div className="col-md-12">
            <h1>
              TLS Certificate of Node &quot;{node.name}&quot;{' '}
            </h1>
          </div>

          <div className="row mt-3">
            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <h3>Certificate Information</h3>

                  <dl>
                    <dt>Expiration Date</dt>
                    <dd>
                      {moment(certificate.expires_at).format()} ({moment(certificate.expires_at).fromNow()})
                    </dd>
                    <dt>Fingerprint</dt>
                    <dd>
                      {certificate.fingerprint.toUpperCase()}
                    </dd>
                    <dt>Source/Type</dt>
                    <dd>Generated/Self-Signed</dd>
                  </dl>

                  <hr />

                  <p>
                    You can replace the current certificate with a newly generated self-signed certificate that will be
                    valid for 12 months.
                  </p>

                  <button className="btn btn-sm btn-secondary" onClick={regenerateSelfSignedCertificate}>
                    Replace with new self-signed certificate
                  </button>
                </div>
              </div>
            </div>

            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <h3>Load Certificate from Disk</h3>

                  <p>
                    You can place a certificate in the nzyme cryptography folder on the local disk of this node.
                    It will always be loaded before any wildcard or custom certificate is considered. This can be
                    especially useful in automatically deployed environments under configuration management.
                  </p>

                  <p>
                    Learn more about this in the <a href="https://go.nzyme.org/load-tlscert-disk">nzyme documentation</a>.
                  </p>

                  <p>
                    <strong>The nzyme certificate load order is the following:</strong>
                  </p>

                  <ol>
                    <li>Local disk</li>
                    <li>Wildcard certificate that matches the node (loaded from database)</li>
                    <li>Individual certificate assigned to node (loaded from database)</li>
                  </ol>
                </div>
              </div>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <h3>Upload Certificate</h3>

                  <p>Use this form to upload an individual TLS certificate for this nzyme node.</p>

                  <p>
                    The certificate must be in PEM format and will typically include a whole certificate chain.
                    Certificate authorities will usually offer this file for download. If the file includes multiple
                    blocks of Base64 plaintext, surrounded by <code>-----BEGIN CERTIFICATE-----</code>, you likely have
                    the correct file.
                  </p>

                  <p>
                    The private key file will often have a <code>.key</code> name ending and should contain a block of
                    Base64 plaintext, surrounded by <code>-----BEGIN RSA PRIVATE KEY-----</code> or
                    <code>-----BEGIN EC PRIVATE KEY-----</code>.
                  </p>
                  
                  <div className="mb-3">
                    <label htmlFor="fu-certificate" className="form-label">
                      Certificate PEM File
                    </label>
                    <input className="form-control form-control-sm" name="certificate" id="fu-certificate"
                           ref={fileIndividualCert} type="file" />
                  </div>

                  <div className="mb-3">
                    <label htmlFor="fu-key" className="form-label">Private Key File</label>
                    <input className="form-control form-control-sm" name="private_key" id="fu-key"
                           ref={fileIndividualKey} type="file" />
                  </div>

                  <button className="btn btn-sm btn-primary" disabled={!individualCertificateFormComplete()}
                          onClick={testIndividualCertificate}>
                    Test Individual Certificate
                  </button>

                  <TLSCertificateTestInProgress show={individualCertTestInProgress} />
                  <TLSCertificateTestCatastrophicFailure show={individualCertTestCatastrophicFailure} />
                  <TLSCertificateTestFailure result={individualCertTestFailureResult} />

                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
  )
}

export default TLSCertificateDetailsPage;