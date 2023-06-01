import React, {createContext, useEffect, useState} from 'react'

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
import LoginPage from './components/system/authentication/LoginPage'
import Store from './util/Store'
import BanditsPage from './components/bandits/BanditsPage'
import CreateBanditPage from './components/bandits/management/CreateBanditPage'
import BanditDetailPage from './components/bandits/BanditDetailPage'
import EditBanditPage from './components/bandits/management/EditBanditPage'
import CreateIdentifierPage from './components/bandits/management/identifiers/CreateIdentifierPage'
import TrackerDetailPage from './components/bandits/trackers/TrackerDetailPage'
import AlertsPage from './components/alerts/AlertsPage'
import PingService from './services/PingService'
import AssetsPage from './components/system/assets/AssetsPage'
import ReportsPage from './components/reports/ReportsPage'
import ScheduleReportPage from './components/reports/ScheduleReportPage'
import ReportDetailsPage from './components/reports/ReportDetailsPage'
import ReportExecutionLogDetailsPage from './components/reports/ReportExecutionLogDetailsPage'
import NetworkDetailsPageRedirector from './components/networks/details/NetworkDetailsPageRedirector'
import BanditContactDetailsPage from './components/bandits/BanditContactDetailsPage'
import Sidebar from './components/layout/Sidebar'
import VersionPage from './components/system/VersionPage'
import DarkMode from './components/layout/DarkMode'
import TapsPage from './components/system/taps/TapsPage'
import TapDetailsPage from './components/system/taps/details/TapDetailsPage'
import TapMetricsDetailsPage from './components/system/taps/details/metrics/TapMetricsDetailsPage'
import DNSOverviewPage from './components/ethernet/dns/DNSOverviewPage'
import SearchPage from './components/retro/SearchPage'
import ServiceSummaryPage from './components/retro/servicesummary/ServiceSummaryPage'
import RetroConfigurationPage from './components/retro/configuration/RetroConfigurationPage'
import PluginsService from './services/PluginsService'
import MissingRetroPluginPage from './components/retro/MissingRetroPluginPage'
import CryptoSummaryPage from './components/system/crypto/CryptoSummaryPage'
import MonitoringPage from './components/system/monitoring/MonitoringPage'
import PrometheusMetricsPage from './components/system/monitoring/prometheus/PrometheusMetricsPage'
import NodesPage from "./components/system/cluster/nodes/NodesPage";
import NodeDetailsPage from "./components/system/cluster/nodes/details/NodeDetailsPage";
import HealthPage from "./components/system/health/HealthPage";
import TLSCertificateDetailsPage from "./components/system/crypto/tls/TLSCertificateDetailsPage";
import TLSWildcardCertificateUploadPage from "./components/system/crypto/tls/wildcard/TLSWildcardCertificateUploadPage";
import TLSWildcardCertificateEditPage from "./components/system/crypto/tls/wildcard/TLSWildcardCertificateEditPage";
import MessagingPage from "./components/system/cluster/messaging/MessagingPage";
import AuthenticationPage from "./components/system/authentication/management/AuthenticationPage";
import CreateOrganizationPage from "./components/system/authentication/management/organizations/CreateOrganizationPage";
import OrganizationDetailsPage from "./components/system/authentication/management/organizations/OrganizationDetailsPage";
import EditOrganizationPage from "./components/system/authentication/management/organizations/EditOrganizationPage";
import TenantDetailsPage from "./components/system/authentication/management/tenants/TenantDetailsPage";
import CreateTenantPage from "./components/system/authentication/management/tenants/CreateTenantPage";
import EditTenantPage from "./components/system/authentication/management/tenants/EditTenantPage";
import CreateTenantUserPage from "./components/system/authentication/management/users/CreateTenantUserPage";
import CreateTapPermissionPage from "./components/system/authentication/management/taps/CreateTapPermissionPage";
import TenantUserDetailsPage from "./components/system/authentication/management/users/TenantUserDetailsPage";
import TapPermissionDetailsPage from "./components/system/authentication/management/taps/TapPermissionDetailsPage";
import EditTapPermissionsPage from "./components/system/authentication/management/taps/EditTapPermissionsPage";
import EditTenantUserPage from "./components/system/authentication/management/users/EditTenantUserPage";
import SetupWizardPage from "./components/setup/SetupWizardPage";
import AuthenticationService from "./services/AuthenticationService";
import MFASetupPage from "./components/system/authentication/MFASetupPage";
import MFAEntryPage from "./components/system/authentication/MFAEntryPage";
import SuperAdminDetailsPage from "./components/system/authentication/management/users/superadmins/SuperAdminDetailsPage";
import EditSuperAdminPage from "./components/system/authentication/management/users/superadmins/EditSuperAdminPage";
import CreateSuperAdminPage from "./components/system/authentication/management/users/superadmins/CreateSuperAdminPage";
import InitializingPage from "./components/misc/InitializingPage";
import CreateOrganizationAdministratorPage
  from "./components/system/authentication/management/users/orgadmins/CreateOrganizationAdministratorPage";
import OrganizationAdminDetailsPage
  from "./components/system/authentication/management/users/orgadmins/OrganizationAdminDetailsPage";
import EditOrganizationAdminPage
  from "./components/system/authentication/management/users/orgadmins/EditOrganizationAdminPage";
import IntegrationsPage from "./components/system/integrations/IntegrationsPage";
import UserProfilePage from "./components/userprofile/UserProfilePage";
import ChangeOwnPasswordPage from "./components/userprofile/ChangeOwnPasswordPage";
import EventsPage from "./components/system/events/EventsPage";
import CreateActionPage from "./components/system/authentication/management/organizations/actions/CreateActionPage";
import ActionDetailsPage from "./components/system/authentication/management/organizations/actions/ActionDetailsPage";

const pingService = new PingService();
const pluginsService = new PluginsService();
const authenticationService = new AuthenticationService();

const isAuthenticated = function() {
  return Store.get("sessionid") !== undefined;
}

const isDarkMode = function() {
  return Store.get("dark_mode") === undefined ? false : Store.get("dark_mode");
}

export const UserContext = createContext(null);
export const TapContext = createContext(null);

function App() {

  const [apiConnected, setApiConnected] = useState(true);
  const [authenticated, setAuthenticated] = useState(isAuthenticated());
  const [darkModeEnabled, setDarkModeEnabled] = useState(isDarkMode());
  const [mfaRequired, setMfaRequired] = useState(true);
  const [mfaSetup, setMfaSetup] = useState(false);
  const [mfaEntryExpiresAt, setMfaEntryExpiresAt] = useState(null);
  const [nzymeInformation, setNzymeInformation] = useState(null);
  const [userInformation, setUserInformation] = useState(null);
  const [plugins, setPlugins] = useState([]); // TODO
  const [selectedTaps, setSelectedTaps] = useState(null);

  const [fullyLoaded, setFullyLoaded] = useState(false);

  const backgroundChecks = function() {
    pingService.ping(setApiConnected, setNzymeInformation, function() {
      fetchSessionInfo(function() {
        setAuthenticated(isAuthenticated());
        setDarkModeEnabled(isDarkMode());
        setFullyLoaded(true);
      });
    }, function () {
      setFullyLoaded(true);
    });
  }

  const fetchSessionInfo = function(callback) {
    if (isAuthenticated()) {
      authenticationService.fetchSessionInfo(function (sessionInfo) {
        setMfaRequired(sessionInfo.mfa_valid === false);
        setMfaSetup(sessionInfo.mfa_setup);
        setMfaEntryExpiresAt(sessionInfo.mfa_entry_expires_at);
        setUserInformation(sessionInfo.user);

        callback();
      }, function() {
        Store.delete("sessionid");
        callback();
      });
    } else {
      callback();
    }
  }

  useEffect(() => {
    backgroundChecks();

    const x = setInterval(function () {
      backgroundChecks();
    }, 1000);

    return () => {
      clearInterval(x);
    }
  }, []);

  useEffect(() => {
    Store.set("dark_mode", darkModeEnabled);
  }, [darkModeEnabled]);

  if (!apiConnected) {
    // API not connected. Show error page.
    return (
        <div className="nzyme">
          <DarkMode enabled={false} />

          <Notifications/>
          <NotConnectedPage />
        </div>
    )
  }

  if (!fullyLoaded) {
    return <InitializingPage darkModeEnabled={darkModeEnabled} />
  }

  if (nzymeInformation && nzymeInformation.show_setup_wizard) {
    // API connected but initial setup not performed yet. Show initial onboarding.
    return (
        <div className="nzyme">
          <DarkMode enabled={false} />

          <Notifications/>

          <SetupWizardPage />
        </div>
    )
  }

  if (!authenticated) {
    // API connected but not authenticated. Show login page.
    return (
        <div className="nzyme">
          <DarkMode enabled={false} />

          <Notifications/>
          <LoginPage />
        </div>
    )
  } else {
    if (mfaRequired) {
      // Connected, authenticated but MFA not passed yet.
      if (mfaSetup) {
        // MFA is set up for this user. Show MFA challenge.
        return (
            <div className="nzyme">
              <Notifications/>

              <MFAEntryPage mfaEntryExpiresAt={mfaEntryExpiresAt} />
            </div>
        )
      } else {
        // MFA is not set up for this user yet. Show setup page.
        return (
          <div className="nzyme">
            <Notifications/>

            <MFASetupPage />
          </div>
        )
      }
    } else {
      // Connected, authenticated and MFA'd. Show full interface.
      return (
          <Router>
            <DarkMode enabled={darkModeEnabled} />

            <div className="nzyme d-flex">
              <UserContext.Provider value={userInformation}>
                <TapContext.Provider value={{set: setSelectedTaps, taps: selectedTaps}}>
                  <Sidebar />

                  <div id="main" className="flex-fill">
                    <Notifications/>
                    <NavigationBar darkModeEnabled={darkModeEnabled} setDarkModeEnabled={setDarkModeEnabled} />

                    <div className="container-fluid">
                      <div className="content">
                        <Routes>
                          <Route path={ApiRoutes.DASHBOARD} element={<OverviewPage />}/>

                          { /* User Profile / Own User */}
                          <Route path={ApiRoutes.USERPROFILE.PROFILE} element={<UserProfilePage />}/>
                          <Route path={ApiRoutes.USERPROFILE.PASSWORD} element={<ChangeOwnPasswordPage />}/>

                          { /* System/Misc. */}
                          <Route path={ApiRoutes.SYSTEM.VERSION} element={<VersionPage />}/>

                          { /* System/Authentication */ }
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX} element={<AuthenticationPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.CREATE} element={<CreateSuperAdminPage/>}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.DETAILS(':userId')} element={<SuperAdminDetailsPage/>}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.EDIT(':userId')} element={<EditSuperAdminPage/>}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.CREATE} element={<CreateOrganizationPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(':organizationId')} element={<OrganizationDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EDIT(':organizationId')} element={<EditOrganizationPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.CREATE(':organizationId')} element={<CreateOrganizationAdministratorPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.DETAILS(':organizationId', ':userId')} element={<OrganizationAdminDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.EDIT(':organizationId', ':userId')} element={<EditOrganizationAdminPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ACTIONS.CREATE(':organizationId')} element={<CreateActionPage />} />
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ACTIONS.DETAILS(':organizationId', ':actionId')} element={<ActionDetailsPage />} />
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(':organizationId', ':tenantId')} element={<TenantDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.CREATE(':organizationId')} element={<CreateTenantPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.EDIT(':organizationId', ':tenantId')} element={<EditTenantPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.CREATE(':organizationId', ':tenantId')} element={<CreateTenantUserPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.DETAILS(':organizationId', ':tenantId', ':userId')} element={<TenantUserDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.EDIT(':organizationId', ':tenantId', ':userId')} element={<EditTenantUserPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.CREATE(':organizationId', ':tenantId')} element={<CreateTapPermissionPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.DETAILS(':organizationId', ':tenantId', ':tapUuid')} element={<TapPermissionDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.EDIT(':organizationId', ':tenantId', ':tapUuid')} element={<EditTapPermissionsPage />}/>

                          { /* System/Taps. */}
                          <Route path={ApiRoutes.SYSTEM.TAPS.INDEX} element={<TapsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.TAPS.DETAILS(':uuid')} element={<TapDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.TAPS.METRICDETAILS(':uuid', ':metricType', ':metricName')} element={<TapMetricsDetailsPage />}/>

                          { /* System/Crypto. */ }
                          <Route path={ApiRoutes.SYSTEM.CRYPTO.INDEX} element={<CryptoSummaryPage />} />
                          <Route path={ApiRoutes.SYSTEM.CRYPTO.TLS.CERTIFICATE(':nodeUUID')} element={<TLSCertificateDetailsPage />} />
                          <Route path={ApiRoutes.SYSTEM.CRYPTO.TLS.WILDCARD.UPLOAD} element={<TLSWildcardCertificateUploadPage />} />
                          <Route path={ApiRoutes.SYSTEM.CRYPTO.TLS.WILDCARD.EDIT(':certificateId')} element={<TLSWildcardCertificateEditPage />} />

                          { /* System/Monitoring. */ }
                          <Route path={ApiRoutes.SYSTEM.MONITORING.INDEX} element={<MonitoringPage />} />
                          <Route path={ApiRoutes.SYSTEM.MONITORING.PROMETHEUS.INDEX} element={<PrometheusMetricsPage />} />

                          { /* System/Cluster */ }
                          <Route path={ApiRoutes.SYSTEM.CLUSTER.INDEX} element={<NodesPage />} />
                          <Route path={ApiRoutes.SYSTEM.CLUSTER.NODES.DETAILS(':uuid')} element={<NodeDetailsPage />} />
                          <Route path={ApiRoutes.SYSTEM.CLUSTER.MESSAGING.INDEX} element={<MessagingPage />} />

                          { /* System/Health */ }
                          <Route path={ApiRoutes.SYSTEM.HEALTH.INDEX} element={<HealthPage />} />

                          { /* System/Integrations */ }
                          <Route path={ApiRoutes.SYSTEM.INTEGRATIONS.INDEX} element={<IntegrationsPage />} />

                          { /* System/Events */ }
                          <Route path={ApiRoutes.SYSTEM.EVENTS.INDEX} element={<EventsPage />} />

                          { /* Ethernet/DNS. */}
                          <Route path={ApiRoutes.ETHERNET.DNS.INDEX} element={<DNSOverviewPage />}/>

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

                          { /* Retro. */ }
                          <Route path={ApiRoutes.RETRO.SEARCH.INDEX} element={plugins.includes('retroplugin') ? <SearchPage /> : <MissingRetroPluginPage /> }/>
                          <Route path={ApiRoutes.RETRO.SERVICE_SUMMARY} element={plugins.includes('retroplugin') ? <ServiceSummaryPage /> : <MissingRetroPluginPage /> }/>
                          <Route path={ApiRoutes.RETRO.CONFIGURATION} element={plugins.includes('retroplugin') ? <RetroConfigurationPage /> : <MissingRetroPluginPage /> }/>

                          { /* 404. */}
                          <Route path={ApiRoutes.NOT_FOUND} element={<NotFoundPage />}/>

                          { /* Catch-all. */}
                          <Route path="*" element={<NotFoundPage />}/>
                        </Routes>

                        <Footer />
                      </div>
                    </div>
                  </div>
                </TapContext.Provider>
              </UserContext.Provider>
            </div>
          </Router>
      )
    }
  }

}

export default App
