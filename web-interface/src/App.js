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
import AuthenticationService from './services/AuthenticationService'
import PingService from './services/PingService'
import AssetsPage from './components/system/assets/AssetsPage'
import ReportsPage from './components/reports/ReportsPage'
import ScheduleReportPage from './components/reports/ScheduleReportPage'
import ReportDetailsPage from './components/reports/ReportDetailsPage'
import ReportExecutionLogDetailsPage from './components/reports/ReportExecutionLogDetailsPage'
import NetworkDetailsPageRedirector from './components/networks/details/NetworkDetailsPageRedirector'
import BanditContactDetailsPage from './components/bandits/BanditContactDetailsPage'
import Sidebar from "./components/layout/Sidebar";
import VersionPage from "./components/system/VersionPage";
import LeaderPage from "./components/system/LeaderPage";
import DarkMode from "./components/layout/DarkMode";
import AuthenticationPage from "./components/system/authentication/AuthenticationPage";
import TapsPage from "./components/taps/TapsPage";
import TapDetailsPage from "./components/taps/details/TapDetailsPage";

class App extends React.Component {

    constructor (props) {
        super(props)

        this.state = {
            apiConnected: true,
            authenticated: App._isAuthenticated(),
            darkModeEnabled: Store.get("dark_mode") === undefined ? false : Store.get("dark_mode")
        }

        this.pingService = new PingService()
        this.pingService.ping = this.pingService.ping.bind(this)

        this.authenticationService = new AuthenticationService()
        this.authenticationService.checkSession = this.authenticationService.checkSession.bind(this)

        this._setDarkMode = this._setDarkMode.bind(this);
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
    }

    _setDarkMode(x) {
        this.setState({darkModeEnabled: x});
        Store.set("dark_mode", x);
    }

    static _isAuthenticated () {
        return Store.get("api_token") !== undefined
    }

    render () {
        if (this.state.apiConnected) {
            if (this.state.authenticated) {
                return (
                    <Router>
                        <DarkMode enabled={this.state.darkModeEnabled} />

                        <div className="nzyme d-flex">
                            <Sidebar />

                            <div id="main" className="flex-fill">
                                <Notifications/>
                                <NavigationBar setDarkMode={this._setDarkMode} />

                                <div className="container-fluid">
                                    <div className="content">
                                        <Routes>
                                            <Route path={ApiRoutes.DASHBOARD} element={<OverviewPage />}/>

                                            { /* System. */}
                                            <Route path={ApiRoutes.SYSTEM.LEADER} element={<LeaderPage />}/>
                                            <Route path={ApiRoutes.SYSTEM.VERSION} element={<VersionPage />}/>
                                            <Route path={ApiRoutes.SYSTEM.AUTHENTICATION} element={<AuthenticationPage />}/>

                                            { /* System/Taps. */}
                                            <Route path={ApiRoutes.SYSTEM.TAPS.INDEX} element={<TapsPage />}/>
                                            <Route path={ApiRoutes.SYSTEM.TAPS.DETAILS(':tapName')} element={<TapDetailsPage />}/>

                                            { /* Networks. */}
                                            <Route path={ApiRoutes.DOT11.NETWORKS.INDEX} element={<NetworksPage />}/>
                                            <Route path={ApiRoutes.DOT11.NETWORKS.SHOW(':bssid', ':ssid', ':channel')} element={<NetworkDetailsPage />}/>
                                            <Route path={ApiRoutes.DOT11.NETWORKS.PROXY(':bssid', ':ssid')} element={<NetworkDetailsPageRedirector />} />

                                            { /* Alerts. */}
                                            <Route path={ApiRoutes.ALERTS.INDEX} element={<AlertsPage />}/>
                                            <Route exact path={ApiRoutes.ALERTS.SHOW(':alertId')} element={<AlertDetailsPage />}/>

                                            { /* Bandits. */}
                                            <Route path={ApiRoutes.DOT11.BANDITS.INDEX} element={<BanditsPage />}/>
                                            <Route path={ApiRoutes.DOT11.BANDITS.NEW} element={<CreateBanditPage />}/>
                                            <Route path={ApiRoutes.DOT11.BANDITS.SHOW(':banditId')} element={<BanditDetailPage />} />
                                            <Route path={ApiRoutes.DOT11.BANDITS.CONTACT_DETAILS(':banditUUID', ':contactUUID')} element={<BanditContactDetailsPage />} />
                                            <Route path={ApiRoutes.DOT11.BANDITS.EDIT(':banditId')} element={<EditBanditPage />} />
                                            <Route path={ApiRoutes.DOT11.BANDITS.NEW_IDENTIFIER(':banditId')} element={<CreateIdentifierPage />} />
                                            <Route path={ApiRoutes.DOT11.BANDITS.SHOW_TRACKER(':trackerName')} element={<TrackerDetailPage />} />

                                            { /* Wireless Assets. */}
                                            <Route path={ApiRoutes.DOT11.ASSETS.INDEX} element={<AssetsPage />}/>

                                            { /* Reports. */}
                                            <Route path={ApiRoutes.REPORTING.INDEX} element={<ReportsPage />}/>
                                            <Route path={ApiRoutes.REPORTING.SCHEDULE} element={<ScheduleReportPage />} />
                                            <Route path={ApiRoutes.REPORTING.DETAILS(':reportName')} element={<ReportDetailsPage />} />
                                            <Route path={ApiRoutes.REPORTING.EXECUTION_LOG_DETAILS(':reportName', ':executionId')} element={<ReportExecutionLogDetailsPage />} />

                                            { /* 404. */}
                                            <Route path={ApiRoutes.NOT_FOUND} element={<NotFoundPage />}/>

                                            { /* Catch-all. */}
                                            <Route path="*" element={<NotFoundPage />}/>
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
                        <DarkMode enabled={this.state.darkModeEnabled} />

                        <Notifications/>
                        <LoginPage />
                    </div>
                )
            }
        } else {
            return (
            <div className="nzyme">
                    <DarkMode enabled={this.state.darkModeEnabled} />

                    <Notifications/>
                    <NotConnectedPage />
            </div>
            )
        }
    }
}

export default App
