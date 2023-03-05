import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import ClusterService from "../../../../services/ClusterService";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import Routes from "../../../../util/ApiRoutes";
import CryptoService from "../../../../services/CryptoService";
import {notify} from "react-notify-toast";
import TLSCertificateDetails from "./TLSCertificateDetails";
import TLSCertificateHelp from "./TLSCertificateHelp";
import TLSCertificateUploadForm from "./form/TLSCertificateUploadForm";

const clusterService = new ClusterService()
const cryptoService = new CryptoService();

function TLSCertificateDetailsPage(props) {

  const { nodeUUID } = useParams()

  const [node, setNode] = useState(null)
  const [certificate, setCertificate] = useState(null)

  const [certInstallationSuccess, setCertInstallationSuccess] = useState(false)

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

  const installCertificate = function(formData) {
    if (!confirm("Really install individual TLS certificate for this node? This will replace the existing " +
        "certificate. Note that the nzyme HTTP server on this node will restart and you will likely experience a brief " +
        "loss of connection.")) {
      return;
    }

    cryptoService.installIndividualTLSCertificate(nodeUUID, formData, function() {
      notify.show('TLS certificate installed.', 'success');
      setCertInstallationSuccess(true);
    })
  }

  if (!node || !certificate) {
    return <LoadingSpinner />
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

                  <TLSCertificateDetails cert={certificate} />

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
                  <h3>Install Individual Certificate</h3>

                  <p>Use this form to upload an individual TLS certificate for this nzyme node.</p>

                  <TLSCertificateHelp />

                  <TLSCertificateUploadForm onInstall={installCertificate} />

                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
  )
}

export default TLSCertificateDetailsPage;