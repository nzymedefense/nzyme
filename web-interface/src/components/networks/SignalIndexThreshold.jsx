import React from 'react';
import numeral from "numeral";

class SignalIndexThreshold extends React.Component {

    render() {
        // Still in training period.
        if (this.props.status === "TRAINING") {
            return (
                <span>
                    THOLD: <span className="text-warning" title={this.props.threshold.index}>{this.props.status}</span>
                </span>
            )
        }

        // Not enough data.
        if (this.props.status === "TEMP_NA") {
            return (
                <span>
                    THOLD: <span className="text-warning">{this.props.status}</span>
                </span>
            )
        }

        const textColor =  this.props.status === "OK" ? "text-success" : "text-danger";
        return (
            <span>
                THOLD: <span className={textColor}>{numeral(this.props.threshold.index).format("0.00")}</span>
            </span>
        );
    }

}

export default SignalIndexThreshold;