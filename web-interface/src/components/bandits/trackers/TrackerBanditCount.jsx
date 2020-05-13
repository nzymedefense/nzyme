import React from 'react';

class TrackerBanditCount extends React.Component {

    render() {
        if (this.props.status === "DARK") {
            return <span className="text-danger">OFFLINE</span>;
        }

        return(
            <span className={this.props.trackerBanditCount === this.props.totalBanditCount ? "text-success" : "text-warning"}>
                {this.props.trackerBanditCount} / {this.props.totalBanditCount}
            </span>
        )
    }

}

export default TrackerBanditCount;