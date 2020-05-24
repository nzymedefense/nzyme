import React from 'react';

class BanditTrackingStatus extends React.Component {

    render() {
        const bandit = this.props.bandit;

        if (bandit.tracked_by.length > 0) {
            return <span className="badge badge-success">tracked</span>;
        }

        return (<span />);
    }

}

export default BanditTrackingStatus;