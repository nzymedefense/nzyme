const Routes = {
    DASHBOARD: "/",
    SYSTEM_STATUS: "/system",
    NETWORKS: {
        INDEX: "/networks",
        SHOW: (bssid, ssid, channel) => `/networks/show/${bssid}/${ssid}/${channel}`,
        DASHBOARD: ssid => `/networks/dashboard/${ssid}`
    },
    NOT_FOUND: "/notfound",
    ALERTS: {
        INDEX: "/alerts",
        SHOW: id => `/alerts/show/${id}`
    },
    BANDITS: {
        INDEX: "/bandits",
        SHOW: (uuid) => `/bandits/show/${uuid}`,
        NEW: "/bandits/new",
        EDIT: (uuid) => `/bandits/edit/${uuid}`,
        NEW_IDENTIFIER: (banditUUID) => `/bandits/show/${banditUUID}/identifiers/new`,
    },
    TRACKERS: {
        SHOW: (name) => `/trackers/show/${name}`,
    }
};

export default Routes;