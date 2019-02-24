import React from 'react';
import Reflux from 'reflux';
import Routes from "../../util/Routes";

class NavigationBar extends Reflux.Component {

  render() {
    return (
      <header>
        <div className="navbar box-shadow">
          <div className="container d-flex">
            <a href="/" className="navbar-brand d-flex align-items-center">
              <strong>nzyme - WiFi Defense System</strong>
            </a>

            <span className="pull-right">
              <a href={Routes.DASHBOARD} className="btn btn-dark">Dashboard</a>
              &nbsp;
              <a href={Routes.NETWORKS} className="btn btn-dark">Networks</a>
              &nbsp;
              <a href={Routes.SYSTEM_STATUS} className="btn btn-dark">System Status</a>
              &nbsp;
              <a href="https://go.nzyme.org/help" target="_blank" className="btn btn-primary">Help</a>
            </span>
          </div>
        </div>
      </header>
    )
  }

}

export default NavigationBar;