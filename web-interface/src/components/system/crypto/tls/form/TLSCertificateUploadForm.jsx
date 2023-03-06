import React, {useEffect, useState} from "react";
import CryptoService from "../../../../../services/CryptoService";
import TLSCertificateTestInProgress from "./TLSCertificateTestInProgress";
import TLSCertificateTestCatastrophicFailure from "./TLSCertificateTestCatastrophicFailure";
import TLSCertificateTestFailure from "./TLSCertificateTestFailure";
import TLSCertificateTestSuccess from "./TLSCertificateTestSuccess";

const cryptoService = new CryptoService();

function TLSCertificateUploadForm(props) {

  const externalFinishSignal = props.externalFinishSignal;
  const externalTestButtonReady = props.externalTestButtonReady;

  const [certFiles, setCertFiles] = useState(null);
  const [keyFiles, setKeyFiles] = useState(null);

  const [certTestInProgress, setCertTestInProgress] = useState(false)
  const [certTestFailureResult, setCertTestFailureResult] = useState(null)
  const [certTestCatastrophicFailure, setCertTestCatastrophicFailure] = useState(null)
  const [certTestSuccessResult, setCertTestSuccessResult] = useState(null)

  useEffect(() => {
    setCertFiles(null);
    setKeyFiles(null);
    setCertTestSuccessResult(null);
  }, [externalFinishSignal])

  const onCertChange = function(e) {
    setCertFiles(e.target.files);
  }

  const onKeyChange = function(e) {
    setKeyFiles(e.target.files);
  }

  const certButtonActive = function() {
    return certFiles && certFiles[0] && keyFiles && keyFiles[0]
        && (externalTestButtonReady !== undefined && externalTestButtonReady());
  }

  const getFormData = function() {
    const formData = new FormData();
    formData.append("certificate", certFiles[0]);
    formData.append("private_key", keyFiles[0]);

    return formData;
  }

  const testCertificate = function() {
    setCertTestInProgress(true);
    const formData = getFormData();

    setCertTestSuccessResult(null);
    setCertTestFailureResult(null);
    setCertTestCatastrophicFailure(null);

    cryptoService.testTLSCertificate(formData, function(response) {
      // Test succeeded
      setCertTestInProgress(false);
      setCertTestSuccessResult(response.data);
    }, function (response) {
      setCertTestInProgress(false);
      if (response.status === 401) {
        setCertTestFailureResult(response.data);
      } else {
        setCertTestCatastrophicFailure(true);
      }
    })
  }

  return (
      <React.Fragment>
        <div className="mb-3">
          <label htmlFor="fu-certificate" className="form-label">
            Certificate PEM File
          </label>
          <input className="form-control form-control-sm" name="certificate" id="fu-certificate"
                 onChange={onCertChange} type="file" disabled={certTestSuccessResult !== null} />
        </div>

        <div className="mb-3">
          <label htmlFor="fu-key" className="form-label">Private Key File</label>
          <input className="form-control form-control-sm" name="private_key" id="fu-key"
                 onChange={onKeyChange} type="file" disabled={certTestSuccessResult !== null} />
        </div>

        <button className="btn btn-sm btn-primary" disabled={!certButtonActive()}
                onClick={testCertificate}>
          Test Certificate
        </button>

        <TLSCertificateTestInProgress show={certTestInProgress} />
        <TLSCertificateTestCatastrophicFailure show={certTestCatastrophicFailure} />
        <TLSCertificateTestFailure result={certTestFailureResult} />
        <TLSCertificateTestSuccess result={certTestSuccessResult} onClick={() => props.onInstall(getFormData())} />
      </React.Fragment>
  )

}

export default TLSCertificateUploadForm;