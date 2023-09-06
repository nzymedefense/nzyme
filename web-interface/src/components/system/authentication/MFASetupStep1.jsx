import React from "react";
import QRCode from "react-qr-code";

function MFASetupStep1(props) {

  const show = props.show;
  const otpAuthUrl = props.otpAuthUrl;
  const onAdvance = props.onAdvance;

  if (!show) {
    return null;
  }

  return (
      <React.Fragment>
        <QRCode
            className="totp-qr"
            style={{height: "auto", width: "225px"}}
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
                  Many TOTP applications allow you to enter the TOTP URL directly f you cannot scan the QR code:
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

        <button className="btn btn-primary mt-3" onClick={onAdvance}>Next Step</button>
      </React.Fragment>
  )

}

export default MFASetupStep1;