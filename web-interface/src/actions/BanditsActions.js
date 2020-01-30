import Reflux from 'reflux';

const BanditsActions = Reflux.createActions([
    "findAll",
    "findOne",
    "createBandit",
    "updateBandit",
    "findAllIdentifierTypes"
]);

export default BanditsActions;