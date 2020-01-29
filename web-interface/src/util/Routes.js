const Routes = {
    DASHBOARD: "/",
    SYSTEM_STATUS: "/system",
    NETWORKS: {
        INDEX: "/networks",
        SHOW: (bssid, ssid, channel) => `/networks/show/${bssid}/${ssid}/${channel}`
    },
    NOT_FOUND: "/notfound",
    ALERTS: {
        INDEX: "/",
        SHOW: id => `/alerts/show/${id}`
    },
    BANDITS: {
        INDEX: "/bandits",
        SHOW: (uuid) => `/bandits/show/${uuid}`,
        NEW: "/bandits/new",
        EDIT: (uuid) => `/bandits/edit/${uuid}`
    }
};

export default Routes;