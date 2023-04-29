import React from "react";

function MFASetupStep2(props) {

  const show = props.show;
  const recoveryCodes = props.recoveryCodes;
  const onAdvance = props.onAdvance;
  const completionInProgress = props.completionInProgress;

  if (!show) {
    return null;
  }

  return (
      <React.Fragment>
        <p>
          Please store the 8 MFA recovery codes below in a safe place that only you can access. You will need them to
          recover your account if you lose access to your TOTP application or device.
        </p>

        <ul className="mfa-recovery-codes">
          {recoveryCodes.map(rc => (<li key={rc}>{rc}</li>))}
        </ul>

        <button className="btn btn-primary mt-4" onClick={onAdvance} disabled={completionInProgress}>
          {completionInProgress ? "Please wait ... " : "Finish MFA Setup"}
        </button>
      </React.Fragment>
  )

}

export default MFASetupStep2;