import React, {useRef, useState} from 'react'
import AuthenticationService from '../../../services/AuthenticationService'
import AssetStylesheet from "../../misc/AssetStylesheet";
import Store from "../../../util/Store";
import LoginFailedMessage from "./LoginFailedMessage";
import LoginImage from "./LoginImage";

const authenticationService = new AuthenticationService();

function LoginPage(props) {

  const customImage = props.customImage;

  const mask = useRef();
  const [isMasked, setIsMasked] = useState(true);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const DEFAULT_TEXT = "Sign In";

  const [submitText, setSubmitText] = useState(DEFAULT_TEXT)
  const [success, setSuccess] = useState(false);
  const [loginFailed, setLoginFailed] = useState(false);
  const [loginFailedMessage, setLoginFailedMessage] = useState(null);

  const toggleMask = function() {
    if (isMasked) {
      // Unmask password.
      mask.current.classList.remove('text-muted');
      setIsMasked(false);
    } else {
      // Mask password.
      mask.current.classList.add('text-muted');
      setIsMasked(true);
    }
  }

  const onSubmit = function(e) {
    e.preventDefault()
    setLoginFailed(false);

    setSubmitText("Signing in ...")

    authenticationService.createSession(username, password, function(sessionId) {
      setSuccess(true);
      setSubmitText("Success! Please wait...");

      Store.set('sessionid', sessionId)
    }, function(message) {
      setSubmitText(DEFAULT_TEXT)
      setLoginFailed(true);
      setLoginFailedMessage(message);
    })
  }

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  return (
    <React.Fragment>
      <AssetStylesheet filename="onebox.css" />

      <section className="vh-100 start">
        <div className="container py-5 h-100 mb-5">
          <div className="row d-flex justify-content-center align-items-center h-100">
            <div className="col col-xl-10">
              <div className="card main-card">
                <div className="row g-0 vh-100">

                  <div className="col-md-5 d-flex align-items-center">
                    <div className="card-body p-4 p-lg-5 text-black">
                      <h1 className="mb-3 pb-3">Welcome Back.</h1>

                      <p>Please enter your account details.</p>

                      <hr />

                      <LoginFailedMessage show={loginFailed} message={loginFailedMessage} />

                      <form onSubmit={onSubmit}>
                        <div className="form-outline mb-2">
                          <label className="form-label" htmlFor="username">Username</label>
                          <input type="text"
                                 id="username"
                                 className="form-control"
                                 value={username}
                                 onChange={(e) => { updateValue(e, setUsername) }}
                                 required />
                        </div>

                        <div className="form-outline mb-4">
                          <label className="form-label" htmlFor="password">Password</label>
                          <input style={{display: "inline-block"}}
                                 type={isMasked ? "password" : "input"}
                                 id="password"
                                 className="form-control"
                                 value={password}
                                 onChange={(e) => { updateValue(e, setPassword) }}
                                 required />
                          <i className="fa fa-eye text-muted"
                             style={{marginLeft: -30, cursor: "pointer"}}
                             onClick={toggleMask} ref={mask} />
                        </div>

                        <div className="pt-1 mb-3">
                          <button className={"btn btn-block " + (success ? "btn-success" : "btn-dark")} type="submit">
                            {success ? <i className="fa-solid fa-thumbs-up"></i> : null } {submitText}
                          </button>
                        </div>
                      </form>
                    </div>
                  </div>

                  <div className="col-md-7 d-none d-md-block justify-content-center right-half">
                    <LoginImage customImage={customImage} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </React.Fragment>
  )

}

export default LoginPage
