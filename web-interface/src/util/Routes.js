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
        INDEX: "/bandits"
    }
};

export default Routes;