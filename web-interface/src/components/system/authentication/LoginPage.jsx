import React, {useState} from 'react'
import AuthenticationService from '../../../services/AuthenticationService'
import AssetStylesheet from "../../misc/AssetStylesheet";
import Store from "../../../util/Store";
import LoginFailedMessage from "./LoginFailedMessage";

const authenticationService = new AuthenticationService();

function LoginPage() {

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const [loginInProgress, setLoginInProgress] = useState(false);
  const [loginFailed, setLoginFailed] = useState(false);
  const [loginFailedMessage, setLoginFailedMessage] = useState(null);

  const onSubmit = function(e) {
    e.preventDefault()
    setLoginInProgress(true);
    setLoginFailed(false);

    authenticationService.createSession(username, password, function(sessionId) {
      Store.set('sessionid', sessionId)
      setLoginInProgress(false);
    }, function(message) {
      setLoginFailed(true);
      setLoginFailedMessage(message);
      setLoginInProgress(false);
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
                          <input type="password"
                                 id="password"
                                 className="form-control"
                                 value={password}
                                 onChange={(e) => { updateValue(e, setPassword) }}
                                 required />
                        </div>

                        <div className="pt-1 mb-3">
                          <button className="btn btn-dark btn-block" type="submit">
                            {loginInProgress ? 'Signing in ...' : 'Sign in'}
                          </button>
                        </div>
                      </form>
                    </div>
                  </div>

                  <div className="col-md-7 d-none d-md-block justify-content-center right-half">
                    <video id="background-video" autoPlay loop muted poster={window.appConfig.assetsUri + "static/loginsplash_preview.jpg"}>
                      <source src={window.appConfig.assetsUri + "static/loginsplash.mp4"} type="video/mp4" />
                    </video>
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