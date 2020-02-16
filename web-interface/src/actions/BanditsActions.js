import Reflux from 'reflux';

const BanditsActions = Reflux.createActions([
    "findAll",
    "findOne",
    "createBandit",
    "updateBandit",
    "findAllIdentifierTypes",
    "createIdentifier",
    "deleteIdentifier"
]);

export default BanditsActions;