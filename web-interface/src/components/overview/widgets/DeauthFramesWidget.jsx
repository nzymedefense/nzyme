import React from 'react';
import SimpleBarChart from "../../charts/SimpleBarChart";

class DeauthFramesWidget extends React.Component {

    _buildThresholdLine(data, threshold) {
        if (!threshold || !data) {
            return [];
        }

        const dates = Object.keys(data);

        return [
            {
                type: "line",
                visible: true,
                x0: new Date(dates[0]),
                x1: new Date(dates[dates.length-1]),
                y0: threshold,
                y1: threshold,
                line: {
                    color: "#8a0000",
                    dash: "dash",
                    width: 1,
                }
            }
        ];
    }

    render() {
        return (
            <SimpleBarChart
                width={1100}
                height={150}
                customMarginRight={0}
                data={this.props.deauthFrameHistogram}
                shapes={this._buildThresholdLine(this.props.deauthFrameHistogram, this.props.threshold)} />
        );
    }

}

export default DeauthFramesWidget;



