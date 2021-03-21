import React from 'react';

class TrackingMode extends React.Component {

    render() {
        if (this.props.pendingRequests) {
            return (
                <span className="badge badge-warning blink">Pending CMD Receipt</span>
            )
        }

        let mode = this.props.mode ? "Tracking" : "Idle";
        let color = this.props.mode ? "success" : "primary";

        if (this.props.status === "DARK") {
            mode = "OFFLINE";
            color = "danger";
        }

        if (this.props.bandit && this.props.mode && this.props.status !== "DARK") {
           if (this.props.bandit.uuid === this.props.mode) {
                mode = "Tracking This Bandit";
                color = "success";
           } else {
               mode = "Tracking Other Bandit";
               color = "warning";
           }
        }

        return(
            <span className={"badge badge-" + color + " " + (mode.startsWith("Tracking") ? "blink" : "")}>
                {mode}
            </span>
        )

    }

}

export default TrackingMode;