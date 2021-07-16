import React from 'react';
import Routes from "../../util/Routes";
import NavigationLink from "./NavigationLink";
import AlertsButton from "./AlertsButton";

class NavigationBar extends React.Component {

  render() {

    return (
        <header>
        <div className="container">
          <nav className="navbar navbar-expand-md">

            <a href="/" className="navbar-brand d-flex align-items-center">
              <img src={window.appConfig.assetsUri + "static/nzyme.png"} alt="nzyme" className="logo" />
              nzyme - WiFi Defense System
            </a>

            <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#mobileNav"
                    aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
              <i className="fas fa-bars" />
            </button>

            <div className="collapse navbar-collapse">
              <ul className="navbar-nav">
                <li className="nav-item"><NavigationLink href={Routes.DASHBOARD} title="Dashboard" /></li>
                <li className="nav-item"><NavigationLink href={Routes.NETWORKS.INDEX} title="Networks" /></li>
                <li className="nav-item"><NavigationLink href={Routes.ALERTS.INDEX} title="Alerts" /></li>
                <li className="nav-item"><NavigationLink href={Routes.BANDITS.INDEX} title="Bandits" /></li>

                <li className="nav-item dropdown">
                  <button className={"nav-link btn btn-dark dropdown-toggle " + (window.location.pathname.startsWith("/system") ? "active" : "")}
                     id="navbarDropdown"
                     data-toggle="dropdown"
                     aria-haspopup="true"
                     aria-expanded="false">
                    System
                  </button>
                  <div className="dropdown-menu" aria-labelledby="navbarDropdown">
                    <a className="dropdown-item" href={Routes.SYSTEM.STATUS}>System Status</a>
                    <a className="dropdown-item" href={Routes.SYSTEM.ASSETS.INDEX}>Assets</a>
                    <a className="dropdown-item" href={Routes.REPORTS.INDEX}>Reports</a>
                  </div>
                </li>

                <li className="nav-item">
                  <AlertsButton hasAlerts={this.props.hasAlerts} />
                </li>
                <li className="nav-item">
                  <a href="https://go.nzyme.org/help" target="_blank" rel="noopener noreferrer" className="btn btn-primary">Help</a>
                </li>
                <li className="nav-item">
                  <a href="#logout" onClick={this.props.handleLogout} className="btn btn-dark" title="Sign Out">
                    <i className="fas fa-sign-out-alt" />
                  </a>
                </li>
              </ul>
            </div>

            <div className="collapse" id="mobileNav">
              <ul className="navbar-nav">
                <li className="nav-item"><a className="btn btn-dark" href={Routes.DASHBOARD}>Dashboard</a></li>
                <li className="nav-item"><a className="btn btn-dark" href={Routes.NETWORKS.INDEX}>Networks</a></li>
                <li className="nav-item"><a className="btn btn-dark" href={Routes.ALERTS.INDEX}>Alerts</a></li>
                <li className="nav-item"><a className="btn btn-dark" href={Routes.BANDITS.INDEX}>Bandits</a></li>
                <li className="nav-item"><a className="btn btn-dark" href={Routes.SYSTEM.STATUS}>System Status</a></li>
                <li className="nav-item"><a className="btn btn-dark" href={Routes.SYSTEM.ASSETS.INDEX}>Assets</a></li>
                <li className="nav-item">
                  <a href="#logout" onClick={this.props.handleLogout} className="btn btn-dark" title="Sign Out">
                    Log Out
                  </a>
                </li>
              </ul>
            </div>
          </nav>
        </div>
        </header>
    )
  }

}

export default NavigationBar;