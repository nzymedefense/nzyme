const Routes = {
    DASHBOARD: "/",
    SYSTEM_STATUS: "/system",
    NETWORKS: {
        INDEX: "/networks",
        SHOW: (bssid, ssid) => `/networks/show/${bssid}/${ssid}`
    },
    NOT_FOUND: "/notfound",
    ALERTS: {
        SHOW: id => `/alerts/show/${id}`
    }
};

export default Routes;