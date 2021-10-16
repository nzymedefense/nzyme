const Routes = {
    DASHBOARD: "/",
    SYSTEM: {
        STATUS: "/system",
        ASSETS: {
            INDEX: "/system/assets/index"
        }
    },
    NETWORKS: {
        INDEX: "/networks",
        SHOW: (bssid, ssid, channel) => `/networks/show/${bssid}/${ssid}/${channel}`
    },
    NOT_FOUND: "/notfound",
    ALERTS: {
        INDEX: "/alerts",
        SHOW: id => `/alerts/show/${id}`
    },
    BANDITS: {
        INDEX: "/bandits",
        SHOW_TRACKER: (name) => `/bandits/trackers/show/${name}`,
        SHOW: (uuid) => `/bandits/show/${uuid}`,
        NEW: "/bandits/new",
        EDIT: (uuid) => `/bandits/edit/${uuid}`,
        NEW_IDENTIFIER: (banditUUID) => `/bandits/show/${banditUUID}/identifiers/new`,
    },
    REPORTS: {
        INDEX: "/reports",
        SCHEDULE: "/reports/schedule",
        DETAILS: name => `/reports/show/${name}`,
        EXECUTION_LOG_DETAILS: (name, executionId) => `/reports/show/${name}/execution/${executionId}`
    }
};

export default Routes;