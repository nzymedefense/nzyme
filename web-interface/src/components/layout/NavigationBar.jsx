import React from 'react'
import DarkModeButton from './DarkModeButton'
import TapSelector from "../misc/TapSelector";
import ApiRoutes from "../../util/ApiRoutes";

function NavigationBar(props) {

  const onLogout = props.onLogout;
  const darkModeEnabled = props.darkModeEnabled;
  const setDarkModeEnabled = props.setDarkModeEnabled;

  const onSearchSubmit = function() {
    setSearchSubmitted(true);
  }

  return (
    <nav className="navbar">
      <div className="container-fluid">
        <div className="d-flex flex-row">
          <form method="GET" action={ApiRoutes.SEARCH.RESULTS}>
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
          <DarkModeButton darkModeEnabled={darkModeEnabled} setDarkModeEnabled={setDarkModeEnabled} />

          <a href="https://go.nzyme.org/help" className="btn btn-outline-dark main-help" title="Help" target="_blank" rel="noreferrer">
            Help
          </a>
          <button className="btn btn-outline-primary" title="Sign out" onClick={onLogout} >
            Sign Out &nbsp;<i className="fa-solid fa-arrow-right-from-bracket" />
          </button>
        </div>
      </div>
    </nav>
  )

}

export default NavigationBar
