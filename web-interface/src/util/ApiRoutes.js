const ApiRoutes = {
  DASHBOARD: '/',
  USERPROFILE: {
    PROFILE: '/profile',
    PASSWORD: '/profile/password'
  },
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
            CREATE: (organizationId) => `/system/authentication/organizations/show/${organizationId}/admins/create`,
            DETAILS: (organizationId, userId) => `/system/authentication/organizations/show/${organizationId}/admins/show/${userId}`,
            EDIT: (organizationId, userId) => `/system/authentication/organizations/show/${organizationId}/admins/show/${userId}/edit`,
          },
          EVENTS: {
            INDEX: (organizationId) => `/system/authentication/organizations/show/${organizationId}/events`,
            ACTIONS: {
              DETAILS: (organizationId, actionId) => `/system/authentication/organizations/show/${organizationId}/events/actions/show/${actionId}`,
              CREATE: (organizationId) => `/system/authentication/organizations/show/${organizationId}/events/actions/create`,
              EDIT: (organizationId, actionId) => `/system/authentication/organizations/show/${organizationId}/events/actions/show/${actionId}/edit`
            },
            SUBSCRIPTIONS: {
              DETAILS: (organizationId, eventTypeName) => `/system/authentication/organizations/show/${organizationId}/events/subscriptions/${eventTypeName}`
            }
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
      METRICDETAILS: (uuid, metricType, metricName) => `/system/taps/show/${uuid}/metrics/show/${metricType}/${metricName}`
    },
    CRYPTO: {
      INDEX: '/system/crypto',
      TLS: {
        CERTIFICATE: nodeUUID => `/system/crypto/tls/certificate/show/${nodeUUID}`,
        WILDCARD: {
          UPLOAD: '/system/crypto/tls/certificate/wildcard/upload',
          EDIT: (certificateId) => `/system/crypto/tls/certificate/wildcard/show/${certificateId}`
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
      INDEX: '/system/health'
    },
    DATABASE: {
      INDEX: '/system/database'
    },
    INTEGRATIONS: {
      INDEX: '/system/integrations',
      GEOIP: {
        IPINFO: '/system/integrations/geoip/ipinfo'
      }
    },
    EVENTS: {
      INDEX: '/system/events',
      ACTIONS: {
        DETAILS: actionId => `/system/events/actions/show/${actionId}`,
        CREATE: '/system/events/actions/create',
        EDIT: actionId => `/system/events/actions/show/${actionId}/edit`,
      },
      SUBSCRIPTIONS: {
        DETAILS: eventTypeName => `/system/events/subscriptions/show/${eventTypeName}`
      }
    }
  },
  SEARCH: {
    RESULTS: '/search/results'
  },
  REPORTING: {
    INDEX: '/reporting',
    SCHEDULE: '/reporting/schedule',
    DETAILS: name => `/reporting/show/${name}`,
    EXECUTION_LOG_DETAILS: (name, executionId) => `/reporting/show/${name}/execution/show/${executionId}`
  },
  ETHERNET: {
    DNS: {
      INDEX: '/ethernet/dns'
    },
    BEACONS: {
      INDEX: '/ethernet/beacons'
    }
  },
  DOT11: {
    OVERVIEW: '/dot11/overview',
    MONITORING: {
      INDEX: '/dot11/monitoring',
      CREATE: '/dot11/monitoring/ssids/create',
      SSID_DETAILS: (uuid) => `/dot11/monitoring/ssids/show/${uuid}`,
      CONFIGURATION_IMPORT: (uuid) => `/dot11/monitoring/ssids/show/${uuid}/configuration/import`,
    },
    NETWORKS: {
      BSSIDS: '/dot11/bssids',
      BSSID: (bssid) => `/dot11/bssids/show/${bssid}`,
      SSID: (bssid, ssid, frequency) => `/dot11/bssids/show/${bssid}/ssids/show/${ssid}/frequencies/show/${frequency}`
    },
    CLIENTS: {
      INDEX: '/dot11/clients',
      DETAILS: (mac) => `/dot11/clients/show/${mac}`
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
    DETAILS: (uuid) => `/alerts/show/${uuid}`
  }
}

export default ApiRoutes
