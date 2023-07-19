import React, {useState} from 'react'
import Store from '../../util/Store'
import DarkModeButton from './DarkModeButton'
import AuthenticationService from "../../services/AuthenticationService";
import TapSelector from "../misc/TapSelector";
import TimeRangeSelector from "../misc/TimeRangeSelector";
import {Navigate} from "react-router-dom";
import ApiRoutes from "../../util/ApiRoutes";

const authService = new AuthenticationService();

function NavigationBar(props) {

  const darkModeEnabled = props.darkModeEnabled;
  const setDarkModeEnabled = props.setDarkModeEnabled;

  const handleLogout = function(e) {
    e.preventDefault()

    authService.deleteSession(function() {
      Store.delete('sessionid')
    });
  }

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

          <div className="timerange-selector">
            <TimeRangeSelector />
          </div>
        </div>

        <div className="d-flex flex-row">
          <DarkModeButton darkModeEnabled={darkModeEnabled} setDarkModeEnabled={setDarkModeEnabled} />

          <a href="https://go.nzyme.org/help" className="btn btn-outline-dark main-help" title="Help" target="_blank" rel="noreferrer">
            Help
          </a>
          <button className="btn btn-outline-primary" title="Sign out" onClick={handleLogout} >
            Sign Out &nbsp;<i className="fa-solid fa-arrow-right-from-bracket" />
          </button>
        </div>
      </div>
    </nav>
  )

}

export default NavigationBar
