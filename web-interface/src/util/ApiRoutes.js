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
        SETTINGS: '/system/authentication/settings',
        ORGANIZATIONS: {
          DETAILS: (organizationId) => `/system/authentication/organizations/show/${organizationId}`,
          TENANTS_PAGE: (organizationId) => `/system/authentication/organizations/show/${organizationId}/tenants`,
          ADMINS_PAGE: (organizationId) => `/system/authentication/organizations/show/${organizationId}/administrators`,
          EVENTS_PAGE: (organizationId) => `/system/authentication/organizations/show/${organizationId}/events`,
          DATABASE_PAGE: (organizationId) => `/system/authentication/organizations/show/${organizationId}/database`,
          QUOTAS_PAGE: (organizationId) => `/system/authentication/organizations/show/${organizationId}/quotas`,
          CREATE: '/system/authentication/organizations/create',
          EDIT: (organizationId) => `/system/authentication/organizations/show/${organizationId}/edit`,
          ADMINS: {
            CREATE: (organizationId) => `/system/authentication/organizations/show/${organizationId}/admins/create`,
            DETAILS: (organizationId, userId) => `/system/authentication/organizations/show/${organizationId}/admins/show/${userId}`,
            EDIT: (organizationId, userId) => `/system/authentication/organizations/show/${organizationId}/admins/show/${userId}/edit`,
          },
          EVENTS: {
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
          TAPS_PAGE: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/taps`,
          USERS_PAGE: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/users`,
          LOCATIONS_PAGE: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/locations`,
          INTEGRATIONS_PAGE: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations`,
          DATABASE_PAGE: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/database`,
          QUOTAS_PAGE: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/quotas`,
          CREATE: (organizationId) => `/system/authentication/organizations/show/${organizationId}/tenants/create`,
          EDIT: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/edit`,
          LOCATIONS: {
            DETAILS: (organizationId, tenantId, locationId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/locations/show/${locationId}`,
            CREATE: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/locations/create`,
            EDIT: (organizationId, tenantId, locationId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/locations/show/${locationId}/edit`,
            FLOORS: {
              DETAILS: (organizationId, tenantId, locationId, floorId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/locations/show/${locationId}/floors/show/${floorId}`,
              CREATE: (organizationId, tenantId, locationId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/locations/show/${locationId}/floors/create`,
              EDIT: (organizationId, tenantId, locationId, floorId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/locations/show/${locationId}/floors/show/${floorId}/edit`,
            }
          },
          INTEGRATIONS: {
            COT: {
              DETAILS:  (organizationId, tenantId, outputId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/show/${outputId}`,
              CREATE: (organizationId, tenantId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/create`,
              EDIT:  (organizationId, tenantId, outputId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/show/${outputId}/edit`,
              EDIT_CLIENT_CERTIFICATE:  (organizationId, tenantId, outputId) => `/system/authentication/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/show/${outputId}/edit-client-cert`,
            }
          }
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
    },
    CONNECT: '/system/connect',
    LOOKANDFEEL: '/system/lookandfeel',
    SUBSYSTEMS: '/system/subsystems'
  },
  SEARCH: {
    RESULTS: '/search/results'
  },
  ETHERNET: {
    OVERVIEW: '/ethernet/overview',
    L4: {
      OVERVIEW: '/ethernet/l4',
      IP: ip => `/ethernet/l4/ip/show/${ip}`
    },
    ASSETS: {
      INDEX: '/ethernet/assets',
      DETAILS: uuid => `/ethernet/assets/${uuid}`,
      DHCP: {
        INDEX: '/ethernet/assets/dhcp',
        TRANSACTION_DETAILS: transactionId => `/ethernet/assets/dhcp/show/${transactionId}`
      },
      ARP: {
        INDEX: '/ethernet/assets/arp'
      }
    },
    HOSTNAMES: {
      HOSTNAME: hostname => `/ethernet/hostnames/show/${hostname}`
    },
    DNS: {
      INDEX: '/ethernet/dns',
      TRANSACTION_LOGS: '/ethernet/dns/logs'
    },
    TUNNELS: {
      INDEX: '/ethernet/tunnels',
      SOCKS: {
        TUNNEL_DETAILS: tunnelId => `/ethernet/tunnels/socks/tunnels/show/${tunnelId}`
      }
    },
    REMOTE: {
      INDEX: '/ethernet/remoteaccess',
      SSH: {
        SESSION_DETAILS: sessionId => `/ethernet/remoteaccess/ssh/sessions/show/${sessionId}`
      }
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
      SIMILAR_SSID_CONFIGURATION: (uuid) => `/dot11/monitoring/ssids/show/${uuid}/configuration/similarssids`,
      RESTRICTED_SUBSTRINGS_CONFIGURATION: (uuid) => `/dot11/monitoring/ssids/show/${uuid}/configuration/restrictedsubstrings`,
      CLIENTS_CONFIGURATION: (uuid) => `/dot11/monitoring/ssids/show/${uuid}/configuration/clients`,
      BANDITS: {
        INDEX: "/dot11/monitoring/bandits",
        BUILTIN_DETAILS: (id) => `/dot11/monitoring/bandits/builtin/show/${id}`,
        CREATE: (organizationId, tenantId) => `/dot11/monitoring/bandits/custom/organizations/${organizationId}/tenants/${tenantId}/create`,
        CUSTOM_DETAILS: (id) => `/dot11/monitoring/bandits/custom/show/${id}`,
        EDIT: (id) => `/dot11/monitoring/bandits/custom/show/${id}/edit`
      },
      DISCO: {
        CONFIGURATION: (uuid) => `/dot11/monitoring/ssids/show/${uuid}/disco/configuration`,
      },
      PROBE_REQUESTS: {
        INDEX: "/dot11/monitoring/proberequests",
        CREATE: (organizationId, tenantId) => `/dot11/monitoring/proberequests/organizations/${organizationId}/tenants/${tenantId}/create`,
        EDIT: (id, organizationId, tenantId) => `/dot11/monitoring/proberequests/organizations/${organizationId}/tenants/${tenantId}/edit/${id}`
      },
      SSIDS: {
        INDEX: "/dot11/monitoring/ssids"
      }
    },
    NETWORKS: {
      BSSIDS: '/dot11/bssids',
      BSSID: (bssid) => `/dot11/bssids/show/${bssid}`,
      SSID: (bssid, ssid, frequency) => `/dot11/bssids/show/${bssid}/ssids/show/${ssid}/frequencies/show/${frequency}`
    },
    CLIENTS: {
      CONNECTED: '/dot11/clients/connected',
      DISCONNECTED: '/dot11/clients/disconnected',
      DETAILS: (mac) => `/dot11/clients/show/${mac}`
    },
    DISCO: {
      INDEX: '/dot11/disco'
    }
  },
  BLUETOOTH: {
    DEVICES: {
      INDEX: '/bluetooth/devices',
      DETAILS: (mac) => `/bluetooth/devices/show/${mac}`
    },
    MONITORING: {
      INDEX: '/bluetooth/monitoring',
      RULES: {
        CREATE: (organizationId, tenantId) => `/bluetooth/monitoring/rules/organizations/show/${organizationId}/tenants/show/${tenantId}/create`
      }
    }
  },
  UAV: {
    INDEX: '/uavs/all',
    DETAILS: (identifier) => `/uavs/all/show/${identifier}`,
    MONITORING: {
      INDEX: '/uavs/monitoring'
    },
    TYPES: {
      INDEX: '/uavs/types',
      CREATE: (organizationId, tenantId) => `/uavs/types/organizations/show/${organizationId}/tenants/show/${tenantId}/create`,
      EDIT: (uuid, organizationId, tenantId) => `/uavs/types/organizations/show/${organizationId}/tenants/show/${tenantId}/show/${uuid}/edit`,
    }
  },
  GNSS: {
    CONSTELLATIONS: '/gnss/constellations',
    PRN: (constellation, prn) => `/gnss/constellations/${constellation}/prns/show/${prn}`,
    MONITORING: {
      INDEX: '/gnss/monitoring',
      RULES: {
        CREATE: (organizationId, tenantId) => `/gnss/monitoring/rules/organizations/show/${organizationId}/tenants/show/${tenantId}/create`
      }
    }
  },
  CONTEXT: {
    MAC_ADDRESSES: {
      INDEX: '/context/macs',
      SHOW: (uuid, organizationId, tenantId) => `/context/macs/organizations/show/${organizationId}/tenants/show/${tenantId}/show/${uuid}`,
      EDIT: (uuid, organizationId, tenantId) => `/context/macs/organizations/show/${organizationId}/tenants/show/${tenantId}/show/${uuid}/edit`,
      CREATE: '/context/macs/create'
    }
  },
  NOT_FOUND: '/notfound',
  ALERTS: {
    INDEX: '/alerts/overview',
    DETAILS: (uuid) => `/alerts/show/${uuid}`,
    SUBSCRIPTIONS: {
      INDEX: '/alerts/subscriptions',
      DETAILS: (organizationId, detectionName) => `/alerts/subscriptions/organizations/show/${organizationId}/types/show/${detectionName}`
    }
  }
}

export default ApiRoutes
