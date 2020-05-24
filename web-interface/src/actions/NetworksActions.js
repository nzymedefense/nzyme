import Reflux from 'reflux';

const NetworksActions = Reflux.createActions([
    "findAll",
    "findSSIDOnBSSID",
    "findSSID",
    "resetFingerprints"
]);

export default NetworksActions;