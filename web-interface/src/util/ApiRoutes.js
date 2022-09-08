const ApiRoutes = {
  DASHBOARD: '/',
  SYSTEM: {
    LEADER: '/system/leader',
    VERSION: '/system/version',
    AUTHENTICATION: '/system/authentication',
    TAPS: {
      INDEX: '/system/taps',
      DETAILS: tapName => `/system/taps/show/${tapName}`,
      METRICDETAILS: (tapName, metricType, metricName) => `/system/taps/show/${tapName}/metrics/${metricType}/${metricName}`
    },
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
  NOT_FOUND: '/notfound',
  ALERTS: {
    INDEX: '/alerts',
    SHOW: id => `/alerts/show/${id}`
  },
}

export default ApiRoutes
