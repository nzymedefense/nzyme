import React from 'react'

import {
  BrowserRouter as Router,
  Routes,
  Route
} from 'react-router-dom'

import Notifications from 'react-notify-toast'

import NavigationBar from './components/layout/NavigationBar'
import OverviewPage from './components/overview/OverviewPage'
import NotConnectedPage from './components/misc/NotConnectedPage'
import NotFoundPage from './components/misc/NotFoundPage'
import AlertDetailsPage from './components/alerts/AlertDetailsPage'
import ApiRoutes from './util/ApiRoutes'
import Footer from './components/layout/Footer'
import SystemPage from './components/system/SystemPage'
import NetworksPage from './components/networks/NetworksPage'
import NetworkDetailsPage from './components/networks/details/NetworkDetailsPage'
import LoginPage from './components/authentication/LoginPage'
import Store from './util/Store'
import BanditsPage from './components/bandits/BanditsPage'
import CreateBanditPage from './components/bandits/management/CreateBanditPage'
import BanditDetailPage from './components/bandits/BanditDetailPage'
import EditBanditPage from './components/bandits/management/EditBanditPage'
import CreateIdentifierPage from './components/bandits/management/identifiers/CreateIdentifierPage'
import TrackerDetailPage from './components/bandits/trackers/TrackerDetailPage'
import AlertsPage from './components/alerts/AlertsPage'
import AlertsService from './services/AlertsService'
import AuthenticationService from './services/AuthenticationService'
import PingService from './services/PingService'
import AssetsPage from './components/system/assets/AssetsPage'
import ReportsPage from './components/reports/ReportsPage'
import ScheduleReportPage from './components/reports/ScheduleReportPage'
import ReportDetailsPage from './components/reports/ReportDetailsPage'
import ReportExecutionLogDetailsPage from './components/reports/ReportExecutionLogDetailsPage'
import NetworkDetailsPageRedirector from './components/networks/details/NetworkDetailsPageRedirector'
import BanditContactDetailsPage from './components/bandits/BanditContactDetailsPage'
import NavigationLink from "./components/layout/NavigationLink";
import Sidebar from "./components/layout/Sidebar";

class App extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      apiConnected: true,
      authenticated: App._isAuthenticated(),
      active_alerts: []
    }

    this.pingService = new PingService()
    this.pingService.ping = this.pingService.ping.bind(this)

    this.authenticationService = new AuthenticationService()
    this.authenticationService.checkSession = this.authenticationService.checkSession.bind(this)

    this.alertsService = new AlertsService()
    this.alertsService.findActiveCount = this.alertsService.findActiveCount.bind(this)

    App._handleLogout = App._handleLogout.bind(this)
  }

  componentDidMount () {
    const self = this
    self.pingService.ping()
    self.setState({ authenticated: App._isAuthenticated() })

    // Check if we are authenticated, ping.
    setInterval(function () {
      self.pingService.ping()
      self.setState({ authenticated: App._isAuthenticated() })
    }, 1000)

    // Check if session is about to expire and log out if so.
    this.authenticationService.checkSession()
    setInterval(function () {
      self.authenticationService.checkSession()
    }, 10000)

    if (App._isAuthenticated()) {
      self.alertsService.findActiveCount()
      setInterval(self.alertsService.findActiveCount, 5000)
    }
  }

  static _isAuthenticated () {
    return Store.get('api_token') !== undefined
  }

  static _handleLogout (e) {
    e.preventDefault()
    Store.delete('api_token')
  }

  render () {
    // TODO: This is fucked but it's currently required to hide the login page styling after initial login.
    document.body.classList.remove('login-page')
    document.body.style.backgroundImage = ''

    if (this.state.apiConnected) {
      if (this.state.authenticated) {
        return (
                    <Router>
                        <div className="nzyme d-flex">

                            <Sidebar />

                            <div id="main" className="flex-fill">
                                <NavigationBar handleLogout={App._handleLogout} hasAlerts={this.state.active_alerts_count > 0} />

                                <div className="container-fluid">
                                    <div className="content">
                                        <Routes>
                                            <Route path={ApiRoutes.DASHBOARD} element={<OverviewPage />}/>

                                            { /* System Status. */}
                                            <Route path={ApiRoutes.SYSTEM.STATUS} element={<SystemPage />}/>

                                            { /* Networks. */}
                                            <Route path={ApiRoutes.NETWORKS.INDEX} element={<NetworksPage />}/>
                                            <Route path={ApiRoutes.NETWORKS.SHOW(':bssid', ':ssid', ':channel')} element={<NetworkDetailsPage />}/>
                                            <Route path={ApiRoutes.NETWORKS.PROXY(':bssid', ':ssid')} element={<NetworkDetailsPageRedirector />} />

                                            { /* Alerts. */}
                                            <Route path={ApiRoutes.ALERTS.INDEX} element={<AlertsPage />}/>
                                            <Route exact path={ApiRoutes.ALERTS.SHOW(':alertId')} element={<AlertDetailsPage />}/>

                                            { /* Bandits. */}
                                            <Route path={ApiRoutes.BANDITS.INDEX} element={<BanditsPage />}/>
                                            <Route path={ApiRoutes.BANDITS.NEW} element={<CreateBanditPage />}/>
                                            <Route path={ApiRoutes.BANDITS.SHOW(':banditId')} element={<BanditDetailPage />} />
                                            <Route path={ApiRoutes.BANDITS.CONTACT_DETAILS(':banditUUID', ':contactUUID')} element={<BanditContactDetailsPage />} />
                                            <Route path={ApiRoutes.BANDITS.EDIT(':banditId')} element={<EditBanditPage />} />
                                            <Route path={ApiRoutes.BANDITS.NEW_IDENTIFIER(':banditId')} element={<CreateIdentifierPage />} />
                                            <Route path={ApiRoutes.BANDITS.SHOW_TRACKER(':trackerName')} element={<TrackerDetailPage />} />

                                            { /* Assets. */}
                                            <Route path={ApiRoutes.SYSTEM.ASSETS.INDEX} element={<AssetsPage />}/>

                                            { /* Reports. */}
                                            <Route path={ApiRoutes.SYSTEM.REPORTS.INDEX} element={<ReportsPage />}/>
                                            <Route path={ApiRoutes.SYSTEM.REPORTS.SCHEDULE} element={<ScheduleReportPage />} />
                                            <Route path={ApiRoutes.SYSTEM.REPORTS.DETAILS(':reportName')} element={<ReportDetailsPage />} />
                                            <Route path={ApiRoutes.SYSTEM.REPORTS.EXECUTION_LOG_DETAILS(':reportName', ':executionId')} element={<ReportExecutionLogDetailsPage />} />

                                            { /* 404. */}
                                            <Route path={ApiRoutes.NOT_FOUND} element={<NotFoundPage />}/>
                                            <Route path="*" element={<NotFoundPage />}/> { /* Catch-all.  */}
                                        </Routes>

                                        <Footer />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </Router>
        )
      } else {
        return (
                    <div className="nzyme">
                        <div className="container container-login">
                            <Notifications/>
                            <LoginPage />
                        </div>
                    </div>
        )
      }
    } else {
      return (
                <div className="nzyme">
                    <div className="container">
                        <Notifications/>
                        <NotConnectedPage />
                        <Footer/>
                    </div>
                </div>
      )
    }
  }
}

export default App
