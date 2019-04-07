import React from 'react';
import Reflux from 'reflux';
import Routes from "../../util/Routes";
import NavigationLink from "./NavigationLink";

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
              <NavigationLink href={Routes.DASHBOARD} title="Dashboard" />
              &nbsp;
              <NavigationLink href={Routes.NETWORKS} title="Networks" />
              &nbsp;
              <NavigationLink href={Routes.SYSTEM_STATUS} title="System Status" />
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