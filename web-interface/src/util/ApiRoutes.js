const ApiRoutes = {
  DASHBOARD: '/',
  SYSTEM: {
    VERSION: '/system/version',
    AUTHENTICATION: '/system/authentication',
    TAPS: {
      INDEX: '/system/taps',
      DETAILS: tapName => `/system/taps/show/${tapName}`,
      METRICDETAILS: (tapName, metricType, metricName) => `/system/taps/show/${tapName}/metrics/${metricType}/${metricName}`
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
