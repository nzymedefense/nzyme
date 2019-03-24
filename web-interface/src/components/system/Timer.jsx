import React from 'react';
import Reflux from 'reflux';
import numeral from "numeral";

class Timer extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div>
                <h5>{this.props.title}</h5>
                <dl>
                    <dt>Maximum</dt>
                    <dd>{numeral(this.props.timer.max).format("0,0")} &#181;s</dd>
                    <dt>Minimum</dt>
                    <dd>{numeral(this.props.timer.min).format("0,0")} &#181;s</dd>
                    <dt>Mean</dt>
                    <dd>{numeral(this.props.timer.mean).format("0,0")} &#181;s</dd>
                    <dt>99th Percentile</dt>
                    <dd>{numeral(this.props.timer.percentile_99).format("0,0")} &#181;s</dd>
                    <dt>Standard Deviation</dt>
                    <dd>{numeral(this.props.timer.stddev).format("0,0")} &#181;s</dd>
                </dl>
            </div>
        )
    }

}

export default Timer;