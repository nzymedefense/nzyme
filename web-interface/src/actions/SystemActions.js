import Reflux from 'reflux';

const SystemActions = Reflux.createActions([
    "getStatus",
    "getMetrics",
    "getVersionInfo"
]);

export default SystemActions;