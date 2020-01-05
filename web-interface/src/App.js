import React from 'react';
import Reflux from 'reflux';

import {
    BrowserRouter as Router,
    Switch,
    Route
} from 'react-router-dom';

import Notifications from 'react-notify-toast';

import PingStore from "./stores/PingStore";
import PingActions from "./actions/PingActions";

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
import AuthenticationStore from "./stores/AuthenticationStore";
import AuthenticationActions from "./actions/AuthenticationActions";
import AlertsActions from "./actions/AlertsActions";
import AlertsStore from "./stores/AlertsStore";
import BanditsPage from "./components/bandits/BanditsPage";
import CreateBanditPage from "./components/bandits/management/CreateBanditPage";
import BanditDetailPage from "./components/bandits/BanditDetailPage";

class App extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            apiConnected: true,
            authenticated: App._isAuthenticated(),
            active_alerts: []
        };

        this.stores = [PingStore, AuthenticationStore, AlertsStore];

        App._handleLogout = App._handleLogout.bind(this);
    }

    componentDidMount() {
        const self = this;
        PingActions.ping();

        // Check if we are authenticated.
        setInterval(function () {
            self.setState({authenticated: App._isAuthenticated()});
        }, 1000);

        // Check if session is about to expire and log out if so.
        AuthenticationActions.checkSession();
        setInterval(function () {
            AuthenticationActions.checkSession();
        }, 10000);

        if(App._isAuthenticated()) {
            AlertsActions.findActive(1);
            setInterval(AlertsActions.findActive, 5000);
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

        if(this.state.apiConnected) {
            if (this.state.authenticated) {
                return (
                    <Router>
                        <div className="nzyme">
                            <NavigationBar handleLogout={App._handleLogout} hasAlerts={this.state.active_alerts.length > 0} />

                            <div className="container">
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
                                    <Route exact path={Routes.ALERTS.SHOW(":id")} component={AlertDetailsPage}/>

                                    { /* Bandits. */}
                                    <Route exact path={Routes.BANDITS.INDEX} component={BanditsPage}/>
                                    <Route exact path={Routes.BANDITS.NEW} component={CreateBanditPage}/>
                                    <Route exact path={Routes.BANDITS.SHOW(":id")} component={BanditDetailPage} />

                                    { /* 404. */}
                                    <Route path={Routes.NOT_FOUND} component={NotFoundPage}/>
                                    <Route path="*" component={NotFoundPage}/> { /* Catch-all.  */}
                                </Switch>

                                <Footer/>
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