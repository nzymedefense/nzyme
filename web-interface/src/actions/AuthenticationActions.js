import Reflux from 'reflux';

const AuthenticationActions = Reflux.createActions([
    "createSession",
    "checkSession"
]);

export default AuthenticationActions;