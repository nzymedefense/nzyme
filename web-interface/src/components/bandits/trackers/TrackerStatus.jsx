import React from 'react';

class TrackerStatus extends React.Component {

    render() {
        switch(this.props.status) {
            case "ONLINE":
                return <span className="badge badge-success">ONLINE</span>;
            case "OUT_OF_SYNC":
                return <span className="badge badge-warning">OUT OF SYNC</span>;
            case "DARK":
                return <span className="badge badge-danger">DARK</span>;
        }

        return this.props.status;
    }

}

export default TrackerStatus;