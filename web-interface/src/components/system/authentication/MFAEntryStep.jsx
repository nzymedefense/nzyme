import React, {useEffect, useRef, useState} from "react";
import moment from "moment/moment";
import Store from "../../../util/Store";
import AuthenticationService from "../../../services/AuthenticationService";

const authenticationService = new AuthenticationService();

function MFAEntryStep(props) {

  const show = props.show;
  const mfaEntryExpiresAt = props.mfaEntryExpiresAt;
  const onEnableRecovery = props.onEnableRecovery;

  const DEFAULT_TEXT = "Enter Code";

  const [code1, setCode1] = useState("");
  const [code2, setCode2] = useState("");
  const [code3, setCode3] = useState("");
  const [code4, setCode4] = useState("");
  const [code5, setCode5] = useState("");
  const [code6, setCode6] = useState("");

  const [submitText, setSubmitText] = useState(DEFAULT_TEXT)
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [success, setSuccess] = useState(false);

  const refs = {
    code1: useRef(null),
    code2: useRef(null),
    code3: useRef(null),
    code4: useRef(null),
    code5: useRef(null),
    code6: useRef(null),
  }

  useEffect(() => {
    refs.code1.current.focus();
  }, [])

  const isValidKeyCode = (code) => {
    return (code >= 48 && code <= 57) || code === 8 || code === 37 || code === 39 || code === 13;
  }

  const isValidCodeValue = (value) => {
    return value >= 0 && value < 10;
  }

  const onKeyUp = function(e, number) {
    controlCursor(e.keyCode, number);
  }

  const controlCursor = (keyCode, number) => {
    if (isValidKeyCode(keyCode)) {
      if (keyCode === 13) {
        // Enter pressed.
        if (formReady()) {
          onSubmit();
        }

        return;
      }

      if (keyCode === 8 || keyCode === 37) {
        // Jump to previous box if there is one.
        if (number > 1) {
          refs["code" + (number - 1)].current.focus();
          refs["code" + (number - 1)].current.select();
        }
      } else {
        // Jump to next box if there is one.
        if (number < 6) {
          refs["code" + (number + 1)].current.focus();
          refs["code" + (number + 1)].current.select();
        }
      }
    }
  }

  const onChange = function(e, valueSetter) {
    const value = e.target.value;

    if (!value) {
      // Value was removed, likely via backspace.
      valueSetter("");
    }
    if (isValidCodeValue(value)) {
      valueSetter(value);
    }
  }

  const onPaste = (e) => {
    e.preventDefault();

    const data = e.clipboardData.getData('text').slice(0, 6);
    for (const pos in data) {
      const value = data[pos];

      if (!isValidCodeValue(value)) {
        continue;
      }

      switch(pos) {
        case "0":
          setCode1(value);
          controlCursor(39, 1);
          break;
        case "1":
          setCode2(value);
          controlCursor(39, 2);
          break;
        case "2":
          setCode3(value);
          controlCursor(39, 3);
          break;
        case "3":
          setCode4(value);
          controlCursor(39, 4);
          break;
        case "4":
          setCode5(value);
          controlCursor(39, 5);
          break;
        case "5":
          setCode6(value);
          controlCursor(39, 6);
          break;
      }
    }
  }

  const onSubmit = function() {
    setErrorMessage(null);
    setFormSubmitting(true);
    setSubmitText("Please wait... ");

    const code = code1 + code2 + code3 + code4 + code5 + code6;

    authenticationService.verifyMFA(code, function() {
      setSuccess(true);
      setSubmitText("Success! Please wait...");
    }, function() {
      // Artificially wait 1 second so the user sees something happening. (The REST call is extremely fast)
      setTimeout(function(){
        setFormSubmitting(false);
        setSubmitText(DEFAULT_TEXT);
        setSuccess(false);
        setErrorMessage("Invalid code. Please try again.");
      }, 1000);
    });
  }

  const cancel = function() {
    Store.delete("sessionid");
  }

  const formReady = function() {
    return !formSubmitting && code1 && code2 && code3 && code4 && code5 && code6
  }

  if (!show) {
    return null;
  }

  return (
    <React.Fragment>
      <h1 className="mb-3 pb-3">Multi-Factor Authentication</h1>

      <p>Enter your multi-factor authentication code to proceed.</p>

      <hr className="mb-4"/>

      <form className="row align-items-center totp-validation mb-4">
        <input type="number" className="form-control" ref={refs.code1} placeholder={"0"}
               value={code1}
               onPaste={onPaste}
               onChange={(e) => onChange(e, setCode1)}
               onKeyUp={(e) => onKeyUp(e, 1)} />
        <input type="number" className="form-control" ref={refs.code2} placeholder={"0"}
               value={code2}
               onPaste={onPaste}
               onChange={(e) => onChange(e, setCode2)}
               onKeyUp={(e) => onKeyUp(e,2)} />
        <input type="number" className="form-control" ref={refs.code3} placeholder={"0"}
               value={code3}
               onPaste={onPaste}
               onChange={(e) => onChange(e, setCode3)}
               onKeyUp={(e) => onKeyUp(e,3)} />
        <input type="number" className="form-control" ref={refs.code4} placeholder={"0"}
               value={code4}
               onPaste={onPaste}
               onChange={(e) => onChange(e, setCode4)}
               onKeyUp={(e) => onKeyUp(e,4)} />
        <input type="number" className="form-control" ref={refs.code5} placeholder={"0"}
               value={code5}
               onPaste={onPaste}
               onChange={(e) => onChange(e, setCode5)}
               onKeyUp={(e) => onKeyUp(e,5)} />
        <input type="number" className="form-control" ref={refs.code6} placeholder={"0"}
               value={code6}
               onPaste={onPaste}
               onChange={(e) => onChange(e, setCode6)}
               onKeyUp={(e) => onKeyUp(e,6)} />
      </form>

      { errorMessage ? <div className="alert alert-warning mt-2 mb-4">{errorMessage}</div> : null }

      <button className={"btn " + (success ? "btn-success" : "btn-primary")}
              onClick={onSubmit}
              disabled={!formReady()}>
        {success ? <i className="fa-solid fa-thumbs-up"></i> : null } {submitText}
      </button>

      <button className="btn btn-sm btn-link mt-5" onClick={onEnableRecovery}>
        Use a recovery code
      </button>

      <div className="mt-5">
        You have <strong title={mfaEntryExpiresAt}>{moment(mfaEntryExpiresAt).fromNow(true)}</strong> remaining
        to pass the multi-factor challenge. After that, you have to log in again.{' '}
        <a href="#" onClick={cancel}>Return to sign in page</a>.
      </div>
    </React.Fragment>
  )

}

export default MFAEntryStep;