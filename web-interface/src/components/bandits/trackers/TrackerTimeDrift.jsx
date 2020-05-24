import React from 'react';

class TrackerTimeDrift extends React.Component {

    render() {
        if (this.props.status === "DARK") {
            return <span className="text-danger">OFFLINE</span>;
        }

        const displayValue = Math.round(this.props.drift/1000);

        if(this.props.drift > 5000 || this.props.drift < -5000) {
            return <span className="text-danger">{displayValue} seconds</span>;
        } else {
            return <span className="text-success">{displayValue} seconds</span>;
        }
    }

}

export default TrackerTimeDrift;