import React from 'react';
import Reflux from 'reflux';

class TrackBanditButton extends Reflux.Component {

    render() {
        const bandit = this.props.bandit;
        const tracker = this.props.tracker;

        let disabled;
        if (tracker.state === "DARK" || tracker.has_pending_tracking_requests) {
            disabled = true;
        } else {
            if (tracker.tracking_mode) {
                disabled = true;
            } else {
                disabled = false;
            }
        }

        if (bandit) {
            return (
                <button className="btn btn-sm btn-primary" disabled={disabled} onClick={this.props.onClick}>Track this bandit</button>
            )
        } else {
            return <span />
        }
    }

}

export default TrackBanditButton;