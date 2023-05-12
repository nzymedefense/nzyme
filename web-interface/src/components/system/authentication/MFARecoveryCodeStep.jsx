import React, {useState} from "react";
import AuthenticationService from "../../../services/AuthenticationService";
import Store from "../../../util/Store";

const authenticationService = new AuthenticationService();

function MFARecoveryCodeStep(props) {

  const show = props.show;
  const onAbort = props.onAbort;

  const DEFAULT_TEXT = "Enter Code";

  const [recoveryCode, setRecoveryCode] = useState("");

  const [submitText, setSubmitText] = useState(DEFAULT_TEXT);
  const [errorMessage, setErrorMessage] = useState(null)
  const [success, setSuccess] = useState(false);
  const [submitInProgress, setSubmitInProgress] = useState(false);

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formReady = function() {
    return !submitInProgress && recoveryCode
  }

  const onSubmit = function() {
    setErrorMessage(null);
    setSubmitInProgress(true);
    setSubmitText("Please wait... ");

    authenticationService.useMFARecoveryCode(recoveryCode, function() {
      setSuccess(true);
      setSubmitText("Success! Please wait...");
    }, function() {
      // Artificially wait 1 second so the user sees something happening. (The REST call is extremely fast)
      setTimeout(function(){
        setSubmitInProgress(false);
        setSubmitText(DEFAULT_TEXT);
        setSuccess(false);
        setErrorMessage("Invalid code. Please try again.");
      }, 1000);
    })
  }

  const cancel = function() {
    onAbort();
  }

  if (!show) {
    return null;
  }

  return (
      <React.Fragment>
        <h1 className="mb-3 pb-3">MFA Recovery</h1>

        <p>
          Use on of your MFA recovery codes to complete the sign in process. Once you are signed in, you can reset
          your MFA method in your user profile settings if required.
        </p>

        <hr className="mb-4"/>

        <form>
          <div className="form-outline mb-2">
            <label className="form-label" htmlFor="code">Recovery Code</label>
            <input type="text"
                   id="code"
                   className="form-control"
                   value={recoveryCode}
                   onChange={(e) => { updateValue(e, setRecoveryCode) }}
                   required />
          </div>

          { errorMessage ? <div className="alert alert-warning mt-2 mb-4">{errorMessage}</div> : null }

          <div className="pt-1 mb-3">
            <button className={"btn " + (success ? "btn-success" : "btn-primary")}
                    onClick={onSubmit}
                    disabled={!formReady()}>
              {success ? <i className="fa-solid fa-thumbs-up"></i> : null } {submitText}
            </button>
          </div>

          <p className="mt-5">
            <button className="btn btn-link" onClick={cancel}>Back</button>
          </p>
        </form>

      </React.Fragment>
  )

}

export default MFARecoveryCodeStep;