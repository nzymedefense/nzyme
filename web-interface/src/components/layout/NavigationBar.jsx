import React from 'react'
import DarkModeButton from './DarkModeButton'
import TapSelector from "../misc/TapSelector";
import ApiRoutes from "../../util/ApiRoutes";
import GlobalTenantSelectorButton from "../system/tenantselector/GlobalTenantSelectorButton";
import WithMinimumRole from "../misc/WithMinimumRole";

function NavigationBar(props) {

  const onLogout = props.onLogout;
  const darkModeEnabled = props.darkModeEnabled;
  const setDarkModeEnabled = props.setDarkModeEnabled;

  return (
    <nav className="navbar">
      <div className="container-fluid">
        <div className="d-flex flex-row">
          <form method="GET" action={ApiRoutes.SEARCH.RESULTS}>
            <div className="input-group">
              <input className="form-control" type="search" placeholder="Search" aria-label="Search" />
              <button className="btn btn-outline-secondary" type="submit">
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

          <a href="https://go.nzyme.org/help" className="btn btn-outline-secondary main-help"
             title="Help"
             target="_blank">
            Help
          </a>
          <WithMinimumRole role="ORGADMIN">
            <GlobalTenantSelectorButton />
          </WithMinimumRole>
          <button className="btn btn-outline-secondary" title="Sign out" onClick={onLogout} >
            Sign Out &nbsp;<i className="fa-solid fa-arrow-right-from-bracket" />
          </button>
        </div>
      </div>
    </nav>
  )

}

export default NavigationBar
