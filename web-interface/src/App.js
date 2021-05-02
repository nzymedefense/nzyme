import React from 'react';

import {
    BrowserRouter as Router,
    Switch,
    Route
} from 'react-router-dom';

import Notifications from 'react-notify-toast';

import NavigationBar from './components/layout/NavigationBar';
import OverviewPage from "./components/overview/OverviewPage";
import NotConnectedPage from "./components/misc/NotConnectedPage";
import NotFoundPage from "./components/misc/NotFoundPage";
import AlertDetailsPage from "./components/alerts/AlertDetailsPage";
import Routes from "./util/Routes";
import Footer from "./components/layout/Footer";
import SystemPage from "./components/system/SystemPage";
import NetworksPage from "./components/networks/NetworksPage";
import NetworkDetailsPage from "./components/networks/details/NetworkDetailsPage";
import LoginPage from "./components/authentication/LoginPage";
import Store from "./util/Store";
import BanditsPage from "./components/bandits/BanditsPage";
import CreateBanditPage from "./components/bandits/management/CreateBanditPage";
import BanditDetailPage from "./components/bandits/BanditDetailPage";
import EditBanditPage from "./components/bandits/management/EditBanditPage";
import CreateIdentifierPage from "./components/bandits/management/identifiers/CreateIdentifierPage";
import TrackerDetailPage from "./components/bandits/trackers/TrackerDetailPage";
import AlertsPage from "./components/alerts/AlertsPage";
import AlertsService from "./services/AlertsService";
import AuthenticationService from "./services/AuthenticationService";
import PingService from "./services/PingService";
import AssetsPage from "./components/system/assets/AssetsPage";

class App extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            apiConnected: true,
            authenticated: App._isAuthenticated(),
            active_alerts: []
        };

        this.pingService = new PingService();
        this.pingService.ping = this.pingService.ping.bind(this);

        this.authenticationService = new AuthenticationService();
        this.authenticationService.checkSession = this.authenticationService.checkSession.bind(this);

        this.alertsService = new AlertsService();
        this.alertsService.findActiveCount = this.alertsService.findActiveCount.bind(this);

        App._handleLogout = App._handleLogout.bind(this);
    }

    componentDidMount() {
        const self = this;
        self.pingService.ping();
        self.setState({authenticated: App._isAuthenticated()});

        // Check if we are authenticated, ping.
        setInterval(function () {
            self.pingService.ping();
            self.setState({authenticated: App._isAuthenticated()});
        }, 1000);

        // Check if session is about to expire and log out if so.
        this.authenticationService.checkSession();
        setInterval(function () {
            self.authenticationService.checkSession();
        }, 10000);

        if(App._isAuthenticated()) {
            self.alertsService.findActiveCount();
            setInterval(self.alertsService.findActiveCount, 5000);
        }
    }

    static _isAuthenticated() {
        return Store.get("api_token") !== undefined;
    }

    static _handleLogout(e) {
        e.preventDefault();
        Store.delete("api_token");
    }

    render() {
        // TODO: This is fucked but it's currently required to hide the login page styling after initial login.
        document.body.classList.remove('login-page');
        document.body.style.backgroundImage = "";

        const dashboard = document.location.pathname.startsWith("/networks/dashboard/");

        if(this.state.apiConnected) {
            if (this.state.authenticated) {
                return (
                    <Router>
                        <div className="nzyme">
                            <NavigationBar handleLogout={App._handleLogout} hasAlerts={this.state.active_alerts_count > 0} />

                            <div className={dashboard ? "container-fluid" : "container"}>
                                <Notifications/>

                                <Switch>
                                    <Route exact path={Routes.DASHBOARD} component={OverviewPage}/>

                                    { /* System Status. */}
                                    <Route exact path={Routes.SYSTEM_STATUS} component={SystemPage}/>

                                    { /* Networks. */}
                                    <Route exact path={Routes.NETWORKS.INDEX} component={NetworksPage}/>
                                    <Route exact path={Routes.NETWORKS.SHOW(":bssid", ":ssid", ":channel")}
                                           component={NetworkDetailsPage}/>

                                    { /* Alerts. */}
                                    <Route exact path={Routes.ALERTS.INDEX} component={AlertsPage}/>
                                    <Route exact path={Routes.ALERTS.SHOW(":id")} component={AlertDetailsPage}/>

                                    { /* Bandits. */}
                                    <Route exact path={Routes.BANDITS.INDEX} component={BanditsPage}/>
                                    <Route exact path={Routes.BANDITS.NEW} component={CreateBanditPage}/>
                                    <Route exact path={Routes.BANDITS.SHOW(":id")} component={BanditDetailPage} />
                                    <Route exact path={Routes.BANDITS.EDIT(":id")} component={EditBanditPage} />
                                    <Route exact path={Routes.BANDITS.NEW_IDENTIFIER(":banditUUID")} component={CreateIdentifierPage} />
                                    <Route exact path={Routes.BANDITS.SHOW_TRACKER(":name")} component={TrackerDetailPage} />

                                    { /* Assets. */}
                                    <Route exact path={Routes.ASSETS.INDEX} component={AssetsPage}/>

                                    { /* 404. */}
                                    <Route path={Routes.NOT_FOUND} component={NotFoundPage}/>
                                    <Route path="*" component={NotFoundPage}/> { /* Catch-all.  */}
                                </Switch>

                                { dashboard ? undefined : <Footer /> }
                            </div>
                        </div>
                    </Router>
                );
            } else {
                return (
                    <div className="nzyme">
                        <div className="container container-login">
                            <Notifications/>
                            <LoginPage />
                        </div>
                    </div>
                );
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

export default App;