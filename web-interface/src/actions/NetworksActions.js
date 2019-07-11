import Reflux from 'reflux';

const NetworksActions = Reflux.createActions([
    "findAll",
    "findSSIDOnBSSID",
    "resetFingerprints"
]);

export default NetworksActions;