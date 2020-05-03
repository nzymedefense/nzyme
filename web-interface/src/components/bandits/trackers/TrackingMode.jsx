import React from 'react';

class TrackingMode extends React.Component {

    render() {
        if(this.props.mode) {
            return <span className="badge badge-success blink">TRACKING</span>;
        } else {
            return <span className="badge badge-info">IDLE</span>;

        }
    }

}

export default TrackingMode;