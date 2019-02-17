const Routes = {
    DASHBOARD: "/",
    SYSTEM_STATUS: "/system",
    NETWORKS: "/networks",
    NOT_FOUND: "/notfound",
    ALERTS: {
        SHOW: id => `/alerts/show/${id}`
    }
};

export default Routes;