import React from 'react';

class TrackBanditButton extends React.Component {

    render() {
        const bandit = this.props.bandit;
        const tracker = this.props.tracker;

        if (tracker.state === "DARK") {
            return <span />;
        }

        let tracking;
        if (tracker.has_pending_tracking_requests) {
            tracking = true;
        } else {
            if (tracker.tracking_mode) {
                tracking = true;
            } else {
                tracking = false;
            }
        }

        if (bandit) {
            if (!tracking) {
                return <button className="btn btn-sm btn-primary" disabled={tracker.has_pending_tracking_requests} onClick={this.props.onStartTrackingClick}>Track This Bandit</button>
            } else {
                return <button className="btn btn-sm btn-warning" disabled={tracker.has_pending_tracking_requests} onClick={this.props.onCancelTrackingClick}>Cancel Tracking</button>
            }
        } else {
            return <span />
        }
    }

}

export default TrackBanditButton;