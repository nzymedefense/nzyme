const ApiRoutes = {
  DASHBOARD: '/',
  SYSTEM: {
    VERSION: '/system/version',
    AUTHENTICATION: {
      MANAGEMENT: {
        INDEX: '/system/authentication',
        ORGANIZATIONS: {
          DETAILS: (organizationId) => `/system/authentication/organizations/show/${organizationId}`,
          CREATE: '/system/authentication/organizations/create',
          EDIT: (organizationId) => `/system/authentication/organizations/show/${organizationId}/edit`,
          ADMINS: {
            DETAILS: (organizationId, userId) => `/system/authentication/organizations/show/${organizationId}/admins/show/${userId}`,
          }
        },
        TENANTS: {
          DETAILS: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}`,
          CREATE: (organizationId) => `/system/authentication/organizations/show/${organizationId}/tenants/create`,
          EDIT: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/edit`,
        },
        USERS: {
          CREATE: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/users/create`,
          DETAILS: (organizationId, tenantId, userId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/users/show/${userId}`,
          EDIT: (organizationId, tenantId, userId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/users/show/${userId}/edit`,
        },
        TAPS: {
          CREATE: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/taps/create`,
          DETAILS: (organizationId, tenantId, tapUuid) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/taps/show/${tapUuid}`,
          EDIT: (organizationId, tenantId, tapUuid) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/taps/show/${tapUuid}/edit`,
        },
        SUPERADMINS: {
          CREATE: '/system/authentication/superadmins/create',
          DETAILS: (userId) => `/system/authentication/superadmins/show/${userId}`,
          EDIT: (userId) => `/system/authentication/superadmins/show/${userId}/edit`,
        },
      }
    },
    TAPS: {
      INDEX: '/system/taps',
      DETAILS: uuid => `/system/taps/show/${uuid}`,
      METRICDETAILS: (uuid, metricType, metricName) => `/system/taps/show/${uuid}/metrics/${metricType}/${metricName}`
    },
    CRYPTO: {
      INDEX: '/system/crypto',
      TLS: {
        CERTIFICATE: nodeUUID => `/system/crypto/tls/certificate/${nodeUUID}`,
        WILDCARD: {
          UPLOAD: '/system/crypto/tls/certificate/wildcard/upload',
          EDIT: (certificateId) => `/system/crypto/tls/certificate/wildcard/${certificateId}`
        }
      }
    },
    MONITORING: {
      INDEX: '/system/monitoring',
      PROMETHEUS: {
        INDEX: '/system/monitoring/prometheus'
      }
    },
    CLUSTER: {
      INDEX: '/system/cluster',
      MESSAGING: {
        INDEX: '/system/cluster/messaging'
      },
      NODES: {
        DETAILS: uuid => `/system/cluster/nodes/show/${uuid}`
      }
    },
    HEALTH: {
      INDEX: "/system/health"
    }
  },
  REPORTING: {
    INDEX: '/reporting',
    SCHEDULE: '/reporting/schedule',
    DETAILS: name => `/reporting/show/${name}`,
    EXECUTION_LOG_DETAILS: (name, executionId) => `/reporting/show/${name}/execution/${executionId}`
  },
  ETHERNET: {
    DNS: {
      INDEX: '/ethernet/dns'
    }
  },
  DOT11: {
    NETWORKS: {
      INDEX: '/dot11/networks',
      SHOW: (bssid, ssid, channel) => `/dot11/networks/show/${bssid}/${ssid}/${channel}`,
      PROXY: (bssid, ssid) => `/dot11/networks/show/${bssid}/${ssid}`
    },
    BANDITS: {
      INDEX: '/dot11/bandits',
      SHOW_TRACKER: (name) => `/dot11/bandits/trackers/show/${name}`,
      SHOW: (uuid) => `/dot11/bandits/show/${uuid}`,
      NEW: '/dot11/bandits/new',
      EDIT: (uuid) => `/dot11/bandits/edit/${uuid}`,
      NEW_IDENTIFIER: (banditUUID) => `/dot11/bandits/show/${banditUUID}/identifiers/new`,
      CONTACT_DETAILS: (banditUUID, contactUUID) => `/dot11/bandits/show/${banditUUID}/contacts/${contactUUID}`
    },
    ASSETS: {
      INDEX: '/system/assets/index'
    }
  },
  RETRO: {
    SEARCH: {
      INDEX: '/retro/search'
    },
    SERVICE_SUMMARY: '/retro/servicesummary',
    CONFIGURATION: '/retro/configuration'
  },
  NOT_FOUND: '/notfound',
  ALERTS: {
    INDEX: '/alerts',
    SHOW: id => `/alerts/show/${id}`
  }
}

export default ApiRoutes
