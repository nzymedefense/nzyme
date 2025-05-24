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
import OrganizationEventSubscriptionDetailsPage
  from "./components/system/authentication/management/organizations/events/subscriptions/OrganizationEventSubscriptionDetailsPage";
import BSSIDsPage from "./components/dot11/bssids/BSSIDsPage";
import DatabasePage from "./components/system/database/DatabasePage";
import SSIDDetailsPage from "./components/dot11/bssids/ssids/SSIDDetailsPage";
import ClientsPage from "./components/dot11/clients/ClientsPage";
import BeaconsPage from "./components/ethernet/beacons/BeaconsPage";
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
import CreateFloorPage from "./components/system/authentication/management/tenants/locations/floors/CreateFloorPage";
import EditFloorPage from "./components/system/authentication/management/tenants/locations/floors/EditFloorPage";
import LookAndFeelPage from "./components/system/lookandfeel/LookAndFeelPage";
import IPDetailsPage from "./components/ethernet/l4/ip/IPDetailsPage";
import TunnelsPage from "./components/ethernet/tunnels/TunnelsPage";
import RemoteAccessPage from "./components/ethernet/remote/RemoteAccessPage";
import DNSTransactionLogsPage from "./components/ethernet/dns/logs/DNSTransactionLogsPage";
import ConnectPage from "./components/system/connect/ConnectPage";
import BluetoothDevicesPage from "./components/bluetooth/devices/BluetoothDevicesPage";
import HostnameDetailsPage from "./components/ethernet/hostnames/HostnameDetailsPage";
import SocksTunnelDetailsPage from "./components/ethernet/tunnels/socks/SocksTunnelDetailsPage";
import SSHSessionDetailsPage from "./components/ethernet/remote/ssh/SSHSessionDetailsPage";
import L4OverviewPage from "./components/ethernet/l4/L4OverviewPage";
import BluetoothDeviceDetailsPage from "./components/bluetooth/devices/BluetoothDeviceDetailsPage";
import BanditsPage from "./components/dot11/monitoring/bandits/BanditsPage";
import ProbeRequestsPage from "./components/dot11/monitoring/probereq/ProbeRequestsPage";
import CreateProbeRequestPage from "./components/dot11/monitoring/probereq/CreateProbeRequestPage";
import EditProbeRequestPage from "./components/dot11/monitoring/probereq/EditProbeRequestPage";
import SSIDMonitoringPage from "./components/dot11/monitoring/ssid/SSIDMonitoringPage";
import MonitoredClientsConfigurationPage from "./components/dot11/monitoring/clients/MonitoredClientsConfigurationPage";
import SubsystemsPage from "./components/system/subsystems/SubsystemsPage";
import ProtectedRoute from "./components/misc/ProtectedRoute";
import {userHasSubsystem} from "./util/Tools";
import OrganizationTenantsPage
  from "./components/system/authentication/management/organizations/OrganizationTenantsPage";
import OrganizationAdministratorsPage
  from "./components/system/authentication/management/organizations/OrganizationAdministratorsPage";
import OrganizationEventsAndActionsPage
  from "./components/system/authentication/management/organizations/OrganizationEventsAndActionsPage";
import OrganizationDatabasePage
  from "./components/system/authentication/management/organizations/OrganizationDatabasePage";
import TenantUsersPage from "./components/system/authentication/management/tenants/TenantUsersPage";
import TenantTapsPage from "./components/system/authentication/management/tenants/TenantTapsPage";
import TenantLocationsPage from "./components/system/authentication/management/tenants/locations/TenantLocationsPage";
import TenantDatabasePage from "./components/system/authentication/management/tenants/TenantDatabasePage";
import UavsPage from "./components/uav/UavsPage";
import UavDetailsPage from "./components/uav/UavDetailsPage";
import UavMonitoringPage from "./components/uav/monitoring/UavMonitoringPage";
import UavTypesPage from "./components/uav/types/UavTypesPage";
import CreateCustomTypePage from "./components/uav/types/CreateCustomTypePage";
import TenantIntegrationsPage from "./components/system/authentication/management/tenants/integrations/TenantIntegrationsPage";
import OrganizationQuotasPage from "./components/system/authentication/management/organizations/OrganizationQuotasPage";
import TenantQuotasPage from "./components/system/authentication/management/tenants/TenantQuotasPage";
import CreateCotOutputPage
  from "./components/system/authentication/management/tenants/integrations/cot/CreateCotOutputPage";
import CotOutputDetailsPage
  from "./components/system/authentication/management/tenants/integrations/cot/CotOutputDetailsPage";
import EditCotOutputPage
  from "./components/system/authentication/management/tenants/integrations/cot/EditCotOutputPage";
import EditCotCertificatePage
  from "./components/system/authentication/management/tenants/integrations/cot/EditCotCertificatePage";
import DisconnectedClientsPage from "./components/dot11/clients/DisconnectedClientsPage";
import EditCustomTypePage from "./components/uav/types/EditCustomTypePage";
import EthernetAssetsPage from "./components/ethernet/assets/EthernetAssetsPage";
import BluetoothMonitoringPage from "./components/bluetooth/monitoring/BluetoothMonitoringPage";
import CreateBluetoothMonitoringRulePage from "./components/bluetooth/monitoring/CreateBluetoothMonitoringRulePage";
import DHCPTransactions from "./components/ethernet/assets/dhcp/DHCPTransactionsPage";
import DHCPTransactionsPage from "./components/ethernet/assets/dhcp/DHCPTransactionsPage";
import DHCPTransactionDetailsPage from "./components/ethernet/assets/dhcp/DHCPTransactionDetailsPage";

const pingService = new PingService();
const authenticationService = new AuthenticationService();

const isAuthenticated = function() {
  return Store.get("sessionid") !== undefined;
}

const isDarkMode = function() {
  return Store.get("dark_mode") === undefined ? false : Store.get("dark_mode");
}

export const AppContext = createContext(null);
export const UserContext = createContext(null);
export const TapContext = createContext(null);
export const AlertContext = createContext(null);

function App() {

  const [revision, setRevision] = useState(new Date());
  const [apiConnected, setApiConnected] = useState(true);
  const [authenticated, setAuthenticated] = useState(isAuthenticated());
  const [darkModeEnabled, setDarkModeEnabled] = useState(isDarkMode());
  const [mfaRequired, setMfaRequired] = useState(true);
  const [mfaSetup, setMfaSetup] = useState(false);
  const [mfaEntryExpiresAt, setMfaEntryExpiresAt] = useState(null);
  const [nzymeInformation, setNzymeInformation] = useState(null);
  const [userInformation, setUserInformation] = useState(null);
  const [alertInformation, setAlertInformation] = useState(null);
  const [branding, setBranding] = useState(null);
  const [plugins, setPlugins] = useState([]); // TODO
  const [selectedTaps, setSelectedTaps] = useState(Store.get("selected_taps"));
  const [tapSelectorEnabled, setTapSelectorEnabled] = useState(false);

  const [fullyLoaded, setFullyLoaded] = useState(false);

  const fetchSessionInfo = function(callback) {
    if (isAuthenticated()) {
      authenticationService.fetchSessionInfo(function (sessionInfo) {
        if (sessionInfo.user.has_mfa_disabled) {
          setMfaRequired(false);
        } else {
          setMfaRequired(sessionInfo.mfa_valid === false);
        }
        setMfaSetup(sessionInfo.mfa_setup);
        setMfaEntryExpiresAt(sessionInfo.mfa_entry_expires_at);
        setUserInformation(sessionInfo.user);
        setBranding(sessionInfo.branding);
        setAlertInformation({
          has_active_alerts: sessionInfo.has_active_alerts,
          health_indicator_level: sessionInfo.health_indicator_level
        });

        callback();
      }, function() {
        Store.delete("sessionid");
        callback();
      });
    } else {
      callback();
    }
  }

  // Background ping to keep session alive.
  useEffect(() => {
    const x = setInterval(() => {
      authenticationService.touchSession();
    }, 15000);

    return () => {
      clearInterval(x);
    }
  }, []);

  useEffect(() => {
    pingService.ping(setApiConnected, setNzymeInformation, function() {
      fetchSessionInfo(function() {
        setAuthenticated(isAuthenticated());
        setDarkModeEnabled(isDarkMode());
        setFullyLoaded(true);
      });
    }, function () {
      setFullyLoaded(true);
    });
  }, [revision]);

  useEffect(() => {
    Store.set("dark_mode", darkModeEnabled);
    setRevision(new Date());
  }, [darkModeEnabled]);

  const onLogout = (e) => {
    e.preventDefault()
    logout();
  }

  const logout = () => {
    authenticationService.deleteSession(function() {
      Store.delete('sessionid');
      setRevision(new Date());
    });
  }

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

          <SetupWizardPage onActionCompleted={() => setRevision(new Date())} />
        </div>
    )
  }

  if (!authenticated) {
    // API connected but not authenticated. Show login page.
    return (
        <div className="nzyme">
          <DarkMode enabled={false} />

          <Notifications/>
          <LoginPage customImage={nzymeInformation.login_image} onActionCompleted={() => setRevision(new Date())} />
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

              <MFAEntryPage mfaEntryExpiresAt={mfaEntryExpiresAt}
                            customImage={nzymeInformation.login_image}
                            onActionCompleted={() => setRevision(new Date())}/>
            </div>
        )
      } else {
        // MFA is not set up for this user yet. Show setup page.
        return (
          <div className="nzyme">
            <Notifications/>

            <MFASetupPage customImage={nzymeInformation.login_image}
                          onActionCompleted={() => setRevision(new Date())} />
          </div>
        )
      }
    } else {
      // Connected, authenticated and MFA'd. Show full interface.
      return (
          <Router>
            <DarkMode enabled={darkModeEnabled} />

            <div className="nzyme d-flex">
              <AppContext.Provider value={{logout: logout}}>
                <UserContext.Provider value={userInformation}>
                  <TapContext.Provider value={{set: setSelectedTaps, taps: selectedTaps, selectorEnabled: tapSelectorEnabled, setSelectorEnabled: setTapSelectorEnabled}}>
                    <AlertContext.Provider value={alertInformation}>
                      <Sidebar branding={branding} />

                      <div id="main" className="flex-fill">
                        <Notifications/>
                        <NavigationBar darkModeEnabled={darkModeEnabled}
                                       setDarkModeEnabled={setDarkModeEnabled}
                                       onLogout={onLogout} />

                        <div className="container-fluid">
                          <div className="content">
                            <Routes>
                              <Route path={ApiRoutes.DASHBOARD} element={<OverviewPage />}/>

                              { /* User Profile / Own User */}
                              <Route path={ApiRoutes.USERPROFILE.PROFILE} element={<UserProfilePage onMfaReset={() => setRevision(new Date())} />}/>
                              <Route path={ApiRoutes.USERPROFILE.PASSWORD} element={<ChangeOwnPasswordPage />}/>

                              { /* Search. */}
                              <Route path={ApiRoutes.SEARCH.RESULTS} element={<SearchResultPage />}/>

                              { /* System/Authentication */ }
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX} element={<AuthenticationPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SETTINGS} element={<AuthenticationSettingsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.CREATE} element={<CreateSuperAdminPage/>}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.DETAILS(':userId')} element={<SuperAdminDetailsPage/>}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.EDIT(':userId')} element={<EditSuperAdminPage/>}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.CREATE} element={<CreateOrganizationPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(':organizationId')} element={<OrganizationDetailsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.TENANTS_PAGE(':organizationId')} element={<OrganizationTenantsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS_PAGE(':organizationId')} element={<OrganizationAdministratorsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS_PAGE(':organizationId')} element={<OrganizationEventsAndActionsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DATABASE_PAGE(':organizationId')} element={<OrganizationDatabasePage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.QUOTAS_PAGE(':organizationId')} element={<OrganizationQuotasPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EDIT(':organizationId')} element={<EditOrganizationPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.CREATE(':organizationId')} element={<CreateOrganizationAdministratorPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.DETAILS(':organizationId', ':userId')} element={<OrganizationAdminDetailsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.EDIT(':organizationId', ':userId')} element={<EditOrganizationAdminPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS.ACTIONS.CREATE(':organizationId')} element={<CreateOrganizationActionPage />} />
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS.ACTIONS.DETAILS(':organizationId', ':actionId')} element={<OrganizationActionDetailsPage />} />
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS.ACTIONS.EDIT(':organizationId', ':actionId')} element={<EditOrganizationActionPage />} />
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS.SUBSCRIPTIONS.DETAILS(':organizationId', ':eventTypeName')} element={<OrganizationEventSubscriptionDetailsPage />} />
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(':organizationId', ':tenantId')} element={<TenantDetailsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.USERS_PAGE(':organizationId', ':tenantId')} element={<TenantUsersPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.TAPS_PAGE(':organizationId', ':tenantId')} element={<TenantTapsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS_PAGE(':organizationId', ':tenantId')} element={<TenantLocationsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS_PAGE(':organizationId', ':tenantId')} element={<TenantIntegrationsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DATABASE_PAGE(':organizationId', ':tenantId')} element={<TenantDatabasePage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.QUOTAS_PAGE(':organizationId', ':tenantId')} element={<TenantQuotasPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.CREATE(':organizationId')} element={<CreateTenantPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.EDIT(':organizationId', ':tenantId')} element={<EditTenantPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.CREATE(':organizationId', ':tenantId')} element={<CreateLocationPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.DETAILS(':organizationId', ':tenantId', ':locationId')} element={<LocationDetailsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.EDIT(':organizationId', ':tenantId', ':locationId')} element={<EditLocationPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.FLOORS.CREATE(':organizationId', ':tenantId', ':locationId')} element={<CreateFloorPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.FLOORS.DETAILS(':organizationId', ':tenantId', ':locationId', ':floorId')} element={<FloorDetailsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.FLOORS.EDIT(':organizationId', ':tenantId', ':locationId', ':floorId')} element={<EditFloorPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.CREATE(':organizationId', ':tenantId')} element={<CreateCotOutputPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.DETAILS(':organizationId', ':tenantId', ':outputId')} element={<CotOutputDetailsPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.EDIT(':organizationId', ':tenantId', ':outputId')} element={<EditCotOutputPage />}/>
                              <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.EDIT_CLIENT_CERTIFICATE(':organizationId', ':tenantId', ':outputId')} element={<EditCotCertificatePage />}/>
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

                              { /* System/Misc. */}
                              <Route path={ApiRoutes.SYSTEM.VERSION} element={<VersionPage />}/>
                              <Route path={ApiRoutes.SYSTEM.LOOKANDFEEL} element={<LookAndFeelPage onSettingsUpdated={() => setRevision(new Date())} />} />
                              <Route path={ApiRoutes.SYSTEM.CONNECT} element={<ConnectPage />}/>
                              <Route path={ApiRoutes.SYSTEM.SUBSYSTEMS} element={<SubsystemsPage />}/>

                              { /* Ethernet. */ }
                              <Route element={<ProtectedRoute execute={userHasSubsystem(userInformation, "ethernet")} />}>
                                { /* Ethernet/L4. */}
                                <Route path={ApiRoutes.ETHERNET.L4.OVERVIEW} element={<L4OverviewPage />}/>
                                <Route path={ApiRoutes.ETHERNET.L4.IP(':ipParam')} element={<IPDetailsPage />}/>

                                { /* Ethernet/Assets. */}
                                <Route path={ApiRoutes.ETHERNET.ASSETS.INDEX} element={<EthernetAssetsPage />}/>
                                <Route path={ApiRoutes.ETHERNET.ASSETS.DHCP.INDEX} element={<DHCPTransactionsPage />}/>
                                <Route path={ApiRoutes.ETHERNET.ASSETS.DHCP.TRANSACTION_DETAILS(':transactionId')} element={<DHCPTransactionDetailsPage />}/>

                                { /* Ethernet/Hostnames. */}
                                <Route path={ApiRoutes.ETHERNET.HOSTNAMES.HOSTNAME(':hostnameParam')} element={<HostnameDetailsPage />}/>

                                { /* Ethernet/DNS. */}
                                <Route path={ApiRoutes.ETHERNET.DNS.INDEX} element={<DNSOverviewPage />}/>
                                <Route path={ApiRoutes.ETHERNET.DNS.TRANSACTION_LOGS} element={<DNSTransactionLogsPage />}/>

                                { /* Ethernet/Remote. */}
                                <Route path={ApiRoutes.ETHERNET.REMOTE.INDEX} element={<RemoteAccessPage />}/>
                                <Route path={ApiRoutes.ETHERNET.REMOTE.SSH.SESSION_DETAILS(':sessionId')} element={<SSHSessionDetailsPage />}/>

                                { /* Ethernet/Tunnels. */}
                                <Route path={ApiRoutes.ETHERNET.TUNNELS.INDEX} element={<TunnelsPage />}/>
                                <Route path={ApiRoutes.ETHERNET.TUNNELS.SOCKS.TUNNEL_DETAILS(':tunnelId')} element={<SocksTunnelDetailsPage />}/>

                                { /* Ethernet/Beacons. */}
                                <Route path={ApiRoutes.ETHERNET.BEACONS.INDEX} element={<BeaconsPage />}/>
                              </Route>

                              { /* 802.11. */ }
                              <Route element={<ProtectedRoute execute={userHasSubsystem(userInformation, "dot11")} />}>
                                { /* 802.11/Monitoring. */ }
                                <Route path={ApiRoutes.DOT11.MONITORING.INDEX} element={<Dot11MonitoringPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.CREATE} element={<CreateMonitoredNetworkPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.SSID_DETAILS(':uuid')} element={<MonitoredNetworkDetailsPage/>} />
                                <Route path={ApiRoutes.DOT11.MONITORING.CONFIGURATION_IMPORT(':uuid')} element={<MonitoredNetworkConfigurationImportPage/>} />
                                <Route path={ApiRoutes.DOT11.MONITORING.BANDITS.INDEX} element={<BanditsPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.BANDITS.BUILTIN_DETAILS(':id')} element={<BuiltinBanditDetailsPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.BANDITS.CREATE(':organizationId', ':tenantId')} element={<CreateCustomBanditPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.BANDITS.CUSTOM_DETAILS(':id')} element={<CustomBanditDetailsPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.BANDITS.EDIT(':id')} element={<EditCustomBanditPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.DISCO.CONFIGURATION(':uuid')} element={<ConfigureDiscoDetectionMethodPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.SIMILAR_SSID_CONFIGURATION(':uuid')} element={<SimilarSSIDConfigurationPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.RESTRICTED_SUBSTRINGS_CONFIGURATION(':uuid')} element={<RestrictedSubstringsConfigurationPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.CLIENTS_CONFIGURATION(':uuid')} element={<MonitoredClientsConfigurationPage />} />
                                <Route path={ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.INDEX} element={<ProbeRequestsPage />}/>
                                <Route path={ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.CREATE(':organizationId', ':tenantId')} element={<CreateProbeRequestPage />}/>
                                <Route path={ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.EDIT(':id', ':organizationId', ':tenantId')} element={<EditProbeRequestPage />}/>
                                <Route path={ApiRoutes.DOT11.MONITORING.SSIDS.INDEX} element={<SSIDMonitoringPage />}/>

                                { /* 802.11/Networks. */}
                                <Route path={ApiRoutes.DOT11.OVERVIEW} element={<Dot11OverviewPage />}/>
                                <Route path={ApiRoutes.DOT11.NETWORKS.BSSIDS} element={<BSSIDsPage />}/>
                                <Route path={ApiRoutes.DOT11.NETWORKS.BSSID(':bssidParam')} element={<BSSIDDetailsPage />}/>
                                <Route path={ApiRoutes.DOT11.NETWORKS.SSID(':bssidParam', ':ssidParam', ':frequencyParam')} element={<SSIDDetailsPage />} />

                                { /* 802.11/Clients. */}
                                <Route path={ApiRoutes.DOT11.CLIENTS.CONNECTED} element={<ClientsPage />}/>
                                <Route path={ApiRoutes.DOT11.CLIENTS.DISCONNECTED} element={<DisconnectedClientsPage />}/>
                                <Route path={ApiRoutes.DOT11.CLIENTS.DETAILS(':macParam')} element={<ClientDetailsPage />}/>

                                { /* 802.11/Disco. */}
                                <Route path={ApiRoutes.DOT11.DISCO.INDEX} element={<DiscoPage />}/>
                              </Route>

                              { /* Bluetooth. */ }
                              <Route element={<ProtectedRoute execute={userHasSubsystem(userInformation, "bluetooth")} />}>
                                { /* Bluetooth Clients/Devices. */}
                                <Route path={ApiRoutes.BLUETOOTH.DEVICES.INDEX} element={<BluetoothDevicesPage />}/>
                                <Route path={ApiRoutes.BLUETOOTH.DEVICES.DETAILS(':macParam')} element={<BluetoothDeviceDetailsPage />}/>

                                { /* Bluetooth Monitoring. */}
                                <Route path={ApiRoutes.BLUETOOTH.MONITORING.INDEX} element={<BluetoothMonitoringPage />}/>
                                <Route path={ApiRoutes.BLUETOOTH.MONITORING.RULES.CREATE(':organizationId', ':tenantId')} element={<CreateBluetoothMonitoringRulePage />}/>
                              </Route>

                              { /* UAV. */ }
                              <Route element={<ProtectedRoute execute={userHasSubsystem(userInformation, "uav")} />}>
                                { /* UAVs */}
                                <Route path={ApiRoutes.UAV.INDEX} element={<UavsPage />}/>
                                <Route path={ApiRoutes.UAV.DETAILS(':identifierParam')} element={<UavDetailsPage />}/>
                                <Route path={ApiRoutes.UAV.TYPES.INDEX} element={<UavTypesPage />}/>
                                <Route path={ApiRoutes.UAV.TYPES.CREATE(':organizationId', ':tenantId')} element={<CreateCustomTypePage />}/>
                                <Route path={ApiRoutes.UAV.TYPES.EDIT(':uuid', ':organizationId', ':tenantId')} element={<EditCustomTypePage />}/>

                                <Route path={ApiRoutes.UAV.MONITORING.INDEX} element={<UavMonitoringPage />}/>
                              </Route>

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

                              { /* 404. */}
                              <Route path={ApiRoutes.NOT_FOUND} element={<NotFoundPage />}/>

                              { /* Catch-all. */}
                              <Route path="*" element={<NotFoundPage />}/>
                            </Routes>

                            <Footer />
                          </div>
                        </div>
                      </div>
                    </AlertContext.Provider>
                  </TapContext.Provider>
                </UserContext.Provider>
              </AppContext.Provider>
            </div>
          </Router>
      )
    }
  }

}

export default App
