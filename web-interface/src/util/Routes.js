const Routes = {
    DASHBOARD: "/",
    NOT_FOUND: "/notfound",
    ALERTS: {
        SHOW: id => `/alerts/show/${id}`
    }
};

export default Routes;