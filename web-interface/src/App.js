import React, {useEffect, useState} from 'react'

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

const pingService = new PingService();
const pluginsService = new PluginsService();

const isAuthenticated = function() {
  return Store.get("sessionid") !== undefined;
}

const isDarkMode = function() {
  return Store.get("dark_mode") === undefined ? false : Store.get("dark_mode");
}

function App() {

  const [apiConnected, setApiConnected] = useState(true);
  const [authenticated, setAuthenticated] = useState(isAuthenticated());
  const [darkModeEnabled, setDarkModeEnabled] = useState(isDarkMode());
  const [loaded, setLoaded] = useState(false);
  const [nzymeInformation, setNzymeInformation] = useState(null);
  const [plugins, setPlugins] = useState([]); // TODO

  const preChecks = function() {
    pingService.ping(setApiConnected, setNzymeInformation, setLoaded);
    setAuthenticated(isAuthenticated());
    setDarkModeEnabled(isDarkMode());
  }

  useEffect(() => {
    preChecks();

    setInterval(function () {
      preChecks();
    }, 1000)
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
    // API connected but not authenticated. Show login page. TODO useContext for dark mode enabled? Try to render in header.
    return (
        <div className="nzyme">
          <DarkMode enabled={false} />

          <Notifications/>
          <LoginPage />
        </div>
    )
  } else {
    // Connected and authenticated. Show full interface.
    return (
      <Router>
        <DarkMode enabled={darkModeEnabled} />

        <div className="nzyme d-flex">
          <Sidebar />

          <div id="main" className="flex-fill">
            <Notifications/>
            <NavigationBar darkModeEnabled={darkModeEnabled} setDarkModeEnabled={setDarkModeEnabled} />

            <div className="container-fluid">
              <div className="content">
                <Routes>
                  <Route path={ApiRoutes.DASHBOARD} element={<OverviewPage />}/>

                  { /* System/Misc. */}
                  <Route path={ApiRoutes.SYSTEM.VERSION} element={<VersionPage />}/>

                  { /* System/Authentication*/ }
                  <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX} element={<AuthenticationPage />}/>
                  <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.CREATE} element={<CreateOrganizationPage />}/>
                  <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(':organizationId')} element={<OrganizationDetailsPage />}/>
                  <Route path={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EDIT(':organizationId')} element={<EditOrganizationPage />}/>
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
        </div>
      </Router>
    )

  }

}

export default App
