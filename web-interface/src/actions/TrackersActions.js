import Reflux from 'reflux';

const TrackersActions = Reflux.createActions([
    "findAll",
    "findOne",
    "issueStartTrackingRequest",
    "issueCancelTrackingRequest"
]);

export default TrackersActions;