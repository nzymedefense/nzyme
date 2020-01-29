import Reflux from 'reflux';

const BanditsActions = Reflux.createActions([
    "findAll",
    "findOne",
    "createBandit",
    "updateBandit"
]);

export default BanditsActions;