import React from 'react'
import AuthenticationService from '../../services/AuthenticationService'
import AssetStylesheet from "../misc/AssetStylesheet";

class LoginPage extends React.Component {
  constructor (props) {
    super(props)

    this.usernameInput = React.createRef()
    this.passwordInput = React.createRef()

    this.state = {
      loggingIn: false
    }

    this.authenticationService = new AuthenticationService()
    this.authenticationService.createSession = this.authenticationService.createSession.bind(this)

    this._submitLoginForm = this._submitLoginForm.bind(this)
  }

  _submitLoginForm (e) {
    e.preventDefault()
    this.setState({ loggingIn: true })

    const username = this.usernameInput.current.value
    const password = this.passwordInput.current.value

    this.authenticationService.createSession(username, password)
  }

  render () {
    return (
        <React.Fragment>
          <AssetStylesheet filename="login.css" />

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

                                        <form onSubmit={this._submitLoginForm}>
                                            <div className="form-outline mb-2">
                                              <label className="form-label" htmlFor="username">
                                                Username
                                              </label>
                                              <input type="text" id="username" className="form-control"
                                                      ref={this.usernameInput} required />
                                            </div>

                                            <div className="form-outline mb-4">
                                              <label className="form-label" htmlFor="password">
                                                Password
                                              </label>
                                              <input type="password" id="password" className="form-control"
                                                     ref={this.passwordInput} required />
                                            </div>

                                            <div className="pt-1 mb-3">
                                                <button className="btn btn-dark btn-block" type="submit">
                                                    {this.state.loggingIn ? 'Signing in ...' : 'Sign in'}
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
}

export default LoginPage
