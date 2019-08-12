import React from 'react';
import Reflux from 'reflux';
import numeral from "numeral";

class TimerRow extends Reflux.Component {

    render() {
        return (
            <tr>
                <td>{this.props.title}</td>
                <td>{numeral(this.props.timer.max).format("0,0")} &#181;s</td>
                <td>{numeral(this.props.timer.min).format("0,0")} &#181;s</td>
                <td>{numeral(this.props.timer.mean).format("0,0")} &#181;s</td>
                <td>{numeral(this.props.timer.percentile_99).format("0,0")} &#181;s</td>
                <td>{numeral(this.props.timer.stddev).format("0,0")} &#181;s</td>
            </tr>
        )
    }

}

export default TimerRow;