import React from 'react';
import Reflux from 'reflux';

import AuthenticationStore from "../../stores/AuthenticationStore";
import AuthenticationActions from "../../actions/AuthenticationActions";

class LoginPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = AuthenticationStore;

        this.usernameInput = React.createRef();
        this.passwordInput = React.createRef();

        this.state = {
            loggingIn: false
        }

        this._submitLoginForm = this._submitLoginForm.bind(this);
    }

    _submitLoginForm(e) {
        e.preventDefault();
        this.setState({loggingIn:true});

        const username = this.usernameInput.current.value;
        const password = this.passwordInput.current.value;

        AuthenticationActions.createSession(username, password);
    }

    render() {
        document.body.classList.add('login-page');
        document.body.style.backgroundImage = "url(" + window.appConfig.assetsUri + "static/login_background.jpg)";

        return (
            <div className="d-flex justify-content-center">
                <div className="card">
                    <div className="card-header text-center">
                        <h5>Sign in to nzyme</h5>
                    </div>
                    <div className="card-body">
                        <form onSubmit={this._submitLoginForm}>
                            <div className="input-group form-group">
                                <div className="input-group-prepend">
                                    <span className="input-group-text"><i className="fas fa-user"></i></span>
                                </div>
                                <input type="text" required className="form-control" placeholder="username" ref={this.usernameInput} />
                            </div>
                            <div className="input-group form-group">
                                <div className="input-group-prepend">
                                    <span className="input-group-text"><i className="fas fa-key"></i></span>
                                </div>
                                <input type="password" required className="form-control" placeholder="password" ref={this.passwordInput} />
                            </div>
                            <div className="form-group">
                                <input type="submit" value={this.state.loggingIn ? "Logging in ..." : "Login"} className="btn float-right btn-primary" />
                            </div>
                        </form>
                    </div>
                    <div className="card-footer">
                        <div className="d-flex justify-content-center">
                            WiFi Defense System
                        </div>
                    </div>
                </div>
            </div>
        )
    }

}

export default LoginPage;



