import React from 'react'
import Store from '../../util/Store'
import DarkModeButton from './DarkModeButton'
import AuthenticationService from "../../services/AuthenticationService";
import TapSelector from "../misc/TapSelector";

const authService = new AuthenticationService();

class NavigationBar extends React.Component {

  static _handleLogout (e) {
    e.preventDefault()

    authService.deleteSession(function() {
      Store.delete('sessionid')
    });
  }

  render () {
    return (
        <nav className="navbar">
            <div className="container-fluid">
              <div className="d-flex flex-row">
                <form>
                    <div className="input-group">
                        <input className="form-control" type="search" placeholder="Search" aria-label="Search" />
                        <button className="btn btn-outline-primary" type="submit">
                            <i className="fa-solid fa-search" />
                        </button>
                    </div>
                </form>

                <div className="tap-selector">
                  <TapSelector />
                </div>
              </div>

                <div className="d-flex flex-row">
                    <DarkModeButton darkModeEnabled={this.props.darkModeEnabled} setDarkModeEnabled={this.props.setDarkModeEnabled} />

                    <a href="https://go.nzyme.org/help" className="btn btn-outline-dark main-help" title="Help" target="_blank" rel="noreferrer">
                        Help
                    </a>
                    <button className="btn btn-outline-primary" title="Sign out" onClick={NavigationBar._handleLogout} >
                        Sign Out &nbsp;<i className="fa-solid fa-arrow-right-from-bracket" />
                    </button>
                </div>
            </div>
        </nav>
    )
  }
}

export default NavigationBar
