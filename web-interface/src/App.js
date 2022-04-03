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
import Sidebar from "./components/layout/Sidebar";
import VersionPage from "./components/system/VersionPage";
import MetricsPage from "./components/system/MetricsPage";
import DarkMode from "./components/layout/DarkMode";

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
                                <NavigationBar setDarkMode={this._setDarkMode} />

                                <div className="container-fluid">
                                    <div className="content">
                                        <Routes>
                                            <Route path={ApiRoutes.DASHBOARD} element={<OverviewPage />}/>

                                            { /* System. */}
                                            <Route path={ApiRoutes.SYSTEM.METRICS} element={<MetricsPage />}/>
                                            <Route path={ApiRoutes.SYSTEM.VERSION} element={<VersionPage />}/>

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
                <div className="container">
                    <Notifications/>
                    <NotConnectedPage />
                </div>
            </div>
            )
        }
    }
}

export default App
