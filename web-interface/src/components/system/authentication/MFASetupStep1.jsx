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
          or the Yubikey Authenticator.
        </p>

        <button className="btn btn-primary mt-3" onClick={onAdvance}>Next Step</button>
      </React.Fragment>
  )

}

export default MFASetupStep1;