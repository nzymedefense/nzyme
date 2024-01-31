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
import ApiRoutes from './util/ApiRoutes'
import Footer from './components/layout/Footer'
import LoginPage from './components/system/authentication/LoginPage'
import Store from './util/Store'
import PingService from './services/PingService'
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
import EventSubscriptionDetailsPage from "./components/system/events/subscriptions/EventSubscriptionDetailsPage";
import CreateOrganizationActionPage
  from "./components/system/authentication/management/organizations/events/actions/CreateOrganizationActionPage";
import OrganizationActionDetailsPage
  from "./components/system/authentication/management/organizations/events/actions/OrganizationActionDetailsPage";
import EditOrganizationActionPage
  from "./components/system/authentication/management/organizations/events/actions/EditOrganizationActionPage";
import ActionDetailsPage from "./components/system/events/actions/ActionDetailsPage";
import CreateActionPage from "./components/system/events/actions/CreateActionPage";
import EditActionPage from "./components/system/events/actions/EditActionPage";
import OrganizationEventsPage from "./components/system/authentication/management/organizations/events/OrganizationEventsPage";
import OrganizationEventSubscriptionDetailsPage
  from "./components/system/authentication/management/organizations/events/subscriptions/OrganizationEventSubscriptionDetailsPage";
import BSSIDsPage from "./components/dot11/bssids/BSSIDsPage";
import DatabasePage from "./components/system/database/DatabasePage";
import SSIDDetailsPage from "./components/dot11/bssids/ssids/SSIDDetailsPage";
import ClientsPage from "./components/dot11/clients/ClientsPage";
import BeaconsPage from "./components/ethernet/beacons/BeaconsPage";
import ReportingPage from "./components/reporting/ReportingPage";
import ClientDetailsPage from "./components/dot11/clients/ClientDetailsPage";
import BSSIDDetailsPage from "./components/dot11/bssids/BSSIDDetailsPage";
import Dot11OverviewPage from "./components/dot11/Dot11OverviewPage";
import SearchResultPage from "./components/search/SearchResultPage";
import Dot11MonitoringPage from "./components/dot11/monitoring/Dot11MonitoringPage";
import CreateMonitoredNetworkPage from "./components/dot11/monitoring/CreateMonitoredNetworkPage";
import MonitoredNetworkDetailsPage from "./components/dot11/monitoring/MonitoredNetworkDetailsPage";
import MonitoredNetworkConfigurationImportPage
  from "./components/dot11/monitoring/import/MonitoredNetworkConfigurationImportPage";
import AlertsPage from "./components/alerts/AlertsPage";
import AlertDetailsPage from "./components/alerts/AlertDetailsPage";
import AlertSubscriptionsPage from "./components/alerts/subscriptions/AlertSubscriptionsPage";
import AlertSubscriptionDetailsPage from "./components/alerts/subscriptions/AlertSubscriptionDetailsPage";
import BuiltinBanditDetailsPage from "./components/dot11/monitoring/bandits/BuiltinBanditDetailsPage";
import CreateCustomBanditPage from "./components/dot11/monitoring/bandits/CreateCustomBanditPage";
import CustomBanditDetailsPage from "./components/dot11/monitoring/bandits/CustomBanditDetailsPage";
import EditCustomBanditPage from "./components/dot11/monitoring/bandits/EditCustomBanditPage";
import DiscoPage from "./components/dot11/disco/DiscoPage";
import ConfigureDiscoDetectionMethodPage
  from "./components/dot11/monitoring/disco/configuration/ConfigureDiscoDetectionMethodPage";
import AddTapProxyPage from "./components/system/taps/AddTapProxyPage";
import AuthenticationSettingsPage from "./components/system/authentication/management/AuthenticationSettingsPage";
import MacAddressContextPage from "./components/context/macs/MacAddressContextPage";
import CreateMacAddressContextPage from "./components/context/macs/CreateMacAddressContextPage";
import MacAddressContextDetailsPage from "./components/context/macs/MacAddressContextDetailsPage";
import EditMacAddressContextPage from "./components/context/macs/EditMacAddressContextPage";
import SimilarSSIDConfigurationPage from "./components/dot11/monitoring/SimilarSSIDConfigurationPage";
import RestrictedSubstringsConfigurationPage from "./components/dot11/monitoring/RestrictedSubstringsConfigurationPage";
import FloorDetailsPage from "./components/system/authentication/management/tenants/locations/floors/FloorDetailsPage";
import CreateLocationPage from "./components/system/authentication/management/tenants/locations/CreateLocationPage";
import LocationDetailsPage from "./components/system/authentication/management/tenants/locations/LocationDetailsPage";
import EditLocationPage from "./components/system/authentication/management/tenants/locations/EditLocationPage";

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
  const [selectedTaps, setSelectedTaps] = useState(Store.get("selected_taps"));
  const [tapSelectorEnabled, setTapSelectorEnabled] = useState(false);

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
                <TapContext.Provider value={{set: setSelectedTaps, taps: selectedTaps, selectorEnabled: tapSelectorEnabled, setSelectorEnabled: setTapSelectorEnabled}}>
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

                          { /* Search. */}
                          <Route path={ApiRoutes.SEARCH.RESULTS} element={<SearchResultPage />}/>

                          { /* System/Misc. */}
                          <Route path={ApiRoutes.SYSTEM.VERSION} element={<VersionPage />}/>

                          { /* System/Authentication */ }
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX} element={<AuthenticationPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SETTINGS} element={<AuthenticationSettingsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.CREATE} element={<CreateSuperAdminPage/>}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.DETAILS(':userId')} element={<SuperAdminDetailsPage/>}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.EDIT(':userId')} element={<EditSuperAdminPage/>}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.CREATE} element={<CreateOrganizationPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(':organizationId')} element={<OrganizationDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EDIT(':organizationId')} element={<EditOrganizationPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.CREATE(':organizationId')} element={<CreateOrganizationAdministratorPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.DETAILS(':organizationId', ':userId')} element={<OrganizationAdminDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.EDIT(':organizationId', ':userId')} element={<EditOrganizationAdminPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS.INDEX(':organizationId')} element={<OrganizationEventsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS.ACTIONS.CREATE(':organizationId')} element={<CreateOrganizationActionPage />} />
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS.ACTIONS.DETAILS(':organizationId', ':actionId')} element={<OrganizationActionDetailsPage />} />
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS.ACTIONS.EDIT(':organizationId', ':actionId')} element={<EditOrganizationActionPage />} />
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS.SUBSCRIPTIONS.DETAILS(':organizationId', ':eventTypeName')} element={<OrganizationEventSubscriptionDetailsPage />} />
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(':organizationId', ':tenantId')} element={<TenantDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.CREATE(':organizationId')} element={<CreateTenantPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.EDIT(':organizationId', ':tenantId')} element={<EditTenantPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.CREATE(':organizationId', ':tenantId')} element={<CreateLocationPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.DETAILS(':organizationId', ':tenantId', ':locationId')} element={<LocationDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.EDIT(':organizationId', ':tenantId', ':locationId')} element={<EditLocationPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.FLOORS.DETAILS(':organizationId', ':tenantId')} element={<FloorDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.CREATE(':organizationId', ':tenantId')} element={<CreateTenantUserPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.DETAILS(':organizationId', ':tenantId', ':userId')} element={<TenantUserDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.EDIT(':organizationId', ':tenantId', ':userId')} element={<EditTenantUserPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.CREATE(':organizationId', ':tenantId')} element={<CreateTapPermissionPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.DETAILS(':organizationId', ':tenantId', ':tapUuid')} element={<TapPermissionDetailsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.EDIT(':organizationId', ':tenantId', ':tapUuid')} element={<EditTapPermissionsPage />}/>

                          { /* System/Taps. */}
                          <Route path={ApiRoutes.SYSTEM.TAPS.INDEX} element={<TapsPage />}/>
                          <Route path={ApiRoutes.SYSTEM.TAPS.PROXY_ADD} element={<AddTapProxyPage />} />
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
                          <Route path={ApiRoutes.SYSTEM.EVENTS.SUBSCRIPTIONS.DETAILS(':eventTypeName')} element={<EventSubscriptionDetailsPage />} />
                          <Route path={ApiRoutes.SYSTEM.EVENTS.ACTIONS.DETAILS(':actionId')} element={<ActionDetailsPage />} />
                          <Route path={ApiRoutes.SYSTEM.EVENTS.ACTIONS.EDIT(':actionId')} element={<EditActionPage />} />
                          <Route path={ApiRoutes.SYSTEM.EVENTS.ACTIONS.CREATE} element={<CreateActionPage />} />

                          { /* System/Database */ }
                          <Route path={ApiRoutes.SYSTEM.DATABASE.INDEX} element={<DatabasePage />} />

                          { /* Ethernet/DNS. */}
                          <Route path={ApiRoutes.ETHERNET.DNS.INDEX} element={<DNSOverviewPage />}/>

                          { /* Ethernet/Beacons. */}
                          <Route path={ApiRoutes.ETHERNET.BEACONS.INDEX} element={<BeaconsPage />}/>

                          { /* 802.11/Monitoring. */ }
                          <Route path={ApiRoutes.DOT11.MONITORING.INDEX} element={<Dot11MonitoringPage />} />
                          <Route path={ApiRoutes.DOT11.MONITORING.CREATE} element={<CreateMonitoredNetworkPage />} />
                          <Route path={ApiRoutes.DOT11.MONITORING.SSID_DETAILS(':uuid')} element={<MonitoredNetworkDetailsPage/>} />
                          <Route path={ApiRoutes.DOT11.MONITORING.CONFIGURATION_IMPORT(':uuid')} element={<MonitoredNetworkConfigurationImportPage/>} />
                          <Route path={ApiRoutes.DOT11.MONITORING.BANDITS.BUILTIN_DETAILS(':id')} element={<BuiltinBanditDetailsPage />} />
                          <Route path={ApiRoutes.DOT11.MONITORING.BANDITS.CREATE(':organizationId', ':tenantId')} element={<CreateCustomBanditPage />} />
                          <Route path={ApiRoutes.DOT11.MONITORING.BANDITS.CUSTOM_DETAILS(':id')} element={<CustomBanditDetailsPage />} />
                          <Route path={ApiRoutes.DOT11.MONITORING.BANDITS.EDIT(':id')} element={<EditCustomBanditPage />} />
                          <Route path={ApiRoutes.DOT11.MONITORING.DISCO.CONFIGURATION(':uuid')} element={<ConfigureDiscoDetectionMethodPage />} />
                          <Route path={ApiRoutes.DOT11.MONITORING.SIMILAR_SSID_CONFIGURATION(':uuid')} element={<SimilarSSIDConfigurationPage />} />
                          <Route path={ApiRoutes.DOT11.MONITORING.RESTRICTED_SUBSTRINGS_CONFIGURATION(':uuid')} element={<RestrictedSubstringsConfigurationPage />} />

                          { /* 802.11/Networks. */}
                          <Route path={ApiRoutes.DOT11.OVERVIEW} element={<Dot11OverviewPage />}/>
                          <Route path={ApiRoutes.DOT11.NETWORKS.BSSIDS} element={<BSSIDsPage />}/>
                          <Route path={ApiRoutes.DOT11.NETWORKS.BSSID(':bssidParam')} element={<BSSIDDetailsPage />}/>
                          <Route path={ApiRoutes.DOT11.NETWORKS.SSID(':bssidParam', ':ssidParam', ':frequencyParam')} element={<SSIDDetailsPage />} />

                          { /* 802.11/Clients. */}
                          <Route path={ApiRoutes.DOT11.CLIENTS.INDEX} element={<ClientsPage />}/>
                          <Route path={ApiRoutes.DOT11.CLIENTS.DETAILS(':macParam')} element={<ClientDetailsPage />}/>

                          { /* 802.11/Disco. */}
                          <Route path={ApiRoutes.DOT11.DISCO.INDEX} element={<DiscoPage />}/>

                          { /* Context. */ }
                          <Route path={ApiRoutes.CONTEXT.MAC_ADDRESSES.INDEX} element={<MacAddressContextPage />}/>
                          <Route path={ApiRoutes.CONTEXT.MAC_ADDRESSES.CREATE} element={<CreateMacAddressContextPage />}/>
                          <Route path={ApiRoutes.CONTEXT.MAC_ADDRESSES.SHOW(':uuid', ':organizationId', ':tenantId')} element={<MacAddressContextDetailsPage />}/>
                          <Route path={ApiRoutes.CONTEXT.MAC_ADDRESSES.EDIT(':uuid', ':organizationId', ':tenantId')} element={<EditMacAddressContextPage />}/>

                          { /* Alerts. */}
                          <Route path={ApiRoutes.ALERTS.INDEX} element={<AlertsPage />}/>
                          <Route path={ApiRoutes.ALERTS.DETAILS(':uuid')} element={<AlertDetailsPage />}/>
                          <Route path={ApiRoutes.ALERTS.SUBSCRIPTIONS.INDEX} element={<AlertSubscriptionsPage />}/>
                          <Route path={ApiRoutes.ALERTS.SUBSCRIPTIONS.DETAILS(':organizationId', ':detectionName')} element={<AlertSubscriptionDetailsPage />}/>

                          { /* Retro. */ }
                          <Route path={ApiRoutes.RETRO.SEARCH.INDEX} element={plugins.includes('retroplugin') ? <SearchPage /> : <MissingRetroPluginPage /> }/>
                          <Route path={ApiRoutes.RETRO.SERVICE_SUMMARY} element={plugins.includes('retroplugin') ? <ServiceSummaryPage /> : <MissingRetroPluginPage /> }/>
                          <Route path={ApiRoutes.RETRO.CONFIGURATION} element={plugins.includes('retroplugin') ? <RetroConfigurationPage /> : <MissingRetroPluginPage /> }/>

                          { /* Reporting. */}
                          <Route path={ApiRoutes.REPORTING.INDEX} element={<ReportingPage />}/>

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
