import React, {useState} from "react";
import QRCode from "react-qr-code";
import AuthenticationService from "../../../services/AuthenticationService";

const authenticationService = new AuthenticationService();

function MFASetupStep1(props) {

  const show = props.show;
  const otpAuthUrl = props.otpAuthUrl;
  const onAdvance = props.onAdvance;

  const [otpValidation, setOtpValidation] = useState("")
  const [otpValidationButtonText, setOtpValidationButtonText] = useState("Validate")
  const [otpHelpText, setOtpHelpText] = useState(<span>Enter a generated OTP token to confirm that you have correctly set up MFA.</span>);
  const [otpValidated, setOtpValidated] = useState(false);

  const validate = () => {
    authenticationService.verifyInitialMFA(otpValidation, () => {
      // Token is correct.
      setOtpValidated(true);
      setOtpValidationButtonText("Validated")
      setOtpHelpText(<span className="text-success">MFA has been verified. You can move on to the next step.</span>);
    }, () => {
      // Token is incorrect.
      setOtpValidated(false)
      setOtpHelpText(<span className="text-danger">Invalid code. Please try again.</span>);
    })
  }

  if (!show) {
    return null;
  }

  return (
      <React.Fragment>
        <QRCode
            className="totp-qr"
            style={{height: "auto", width: "150px"}}
            value={otpAuthUrl}
        />

        <p className="mt-4">
          Scan the QR code with your preferred TOTP application like Google Authenticator, Authy
          or the Yubikey Authenticator.{' '}
          <a href="#" data-bs-toggle="modal" data-bs-target="#otp-url-modal">Show TOTP URL</a>
        </p>

        <div className="modal fade"
             id="otp-url-modal"
             tabIndex="-1"
             aria-labelledby="otp-url-modal-label"
             aria-hidden="true">
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-body">
                <h2>TOTP URL</h2>

                <p>
                  Many TOTP applications allow you to enter the TOTP URL directly if you cannot scan the QR code:
                </p>

                <pre>
                  {otpAuthUrl}
                </pre>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Close</button>
              </div>
            </div>
          </div>
        </div>

        <div className="mt-3">
          <form>
            <label htmlFor="otp-validation" className="form-label">Enter OTP Token</label>
            <div className="row">
              <div className="col-md-8 pe-0">
                <input type="text" className="form-control" id="otp-validation"
                       value={otpValidation} onChange={(e) => {
                  e.preventDefault();
                  setOtpValidation(e.target.value)
                }}/>
                </div>
              <div className="col-md-4 ps-0">
                <button className={"btn btn-sm " + (otpValidated ? "btn-success" : "btn-primary")}
                        type="button"
                        disabled={otpValidated}
                        onClick={validate}>
                  {otpValidationButtonText}
                </button>
              </div>
            </div>
            <div className="form-text">{otpHelpText}</div>
          </form>
        </div>

        <button className="btn btn-primary mt-3" onClick={onAdvance} disabled={!otpValidated}>Next Step</button>
      </React.Fragment>
)

}

export default MFASetupStep1;