import React from 'react';
import Reflux from 'reflux';
import numeral from "numeral";
import FormattedTiming from "./FormattedTiming";

class TimerRow extends Reflux.Component {

    render() {
        return (
            <tr>
                <td>{this.props.title}</td>
                <td><FormattedTiming timing={this.props.timer.max} format="0,0" /></td>
                <td><FormattedTiming timing={this.props.timer.min} format="0,0" /></td>
                <td><FormattedTiming timing={this.props.timer.mean} format="0,0" /></td>
                <td><FormattedTiming timing={this.props.timer.percentile_99} format="0,0" /></td>
                <td><FormattedTiming timing={this.props.timer.stddev} format="0,0" /></td>
                <td>{numeral(this.props.timer.count).format(0,0)}</td>
            </tr>
        )
    }

}

export default TimerRow;