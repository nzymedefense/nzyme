import React from 'react'
import AuthenticationService from '../../services/AuthenticationService'
import AssetImage from '../misc/AssetImage'

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
        <section className="vh-100 start">
            <div className="container py-5 h-100">
                <div className="row d-flex justify-content-center align-items-center h-100">
                    <div className="col col-xl-10">
                        <div className="card main-card">
                            <div className="row g-0">

                                <div className="col-md-6 col-lg-5 d-none d-md-block left-half justify-content-center">
                                    <AssetImage filename="logo_small.png"
                                                className="d-block mx-auto"
                                                alt="nzyme logo"
                                                id="logo" />
                                </div>

                                <div className="col-md-6 col-lg-7 d-flex align-items-center">
                                    <div className="card-body p-4 p-lg-5 text-black">
                                        <form onSubmit={this._submitLoginForm}>
                                            <h5 className="fw-normal mb-3 pb-3">Sign into your account</h5>

                                            <div className="form-outline mb-4">
                                                <input type="text" id="username" className="form-control form-control-lg"
                                                        ref={this.usernameInput} required />
                                                <label className="form-label" htmlFor="username">
                                                    Username
                                                </label>
                                            </div>

                                            <div className="form-outline mb-4">
                                                <input type="password" id="password" className="form-control form-control-lg"
                                                       ref={this.passwordInput} required />
                                                <label className="form-label" htmlFor="password">
                                                    Password
                                                </label>
                                            </div>

                                            <div className="pt-1 mb-3">
                                                <button className="btn btn-dark btn-lg btn-block" type="submit">
                                                    {this.state.loggingIn ? 'Signing in ...' : 'Sign in'}
                                                </button>
                                            </div>
                                        </form>

                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    )
  }
}

export default LoginPage
