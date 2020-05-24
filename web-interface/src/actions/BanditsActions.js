import Reflux from 'reflux';

const BanditsActions = Reflux.createActions([
    "findAll",
    "findOne",
    "createBandit",
    "updateBandit",
    "deleteBandit",
    "findAllIdentifierTypes",
    "createIdentifier",
    "deleteIdentifier"
]);

export default BanditsActions;