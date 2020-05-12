import React from 'react';

class TrackerBanditCount extends React.Component {

    render() {
        return(
            <span className={this.props.trackerBanditCount === this.props.totalBanditCount ? "text-success" : "text-warning"}>
                {this.props.trackerBanditCount} / {this.props.totalBanditCount}
            </span>
        )
    }

}

export default TrackerBanditCount;