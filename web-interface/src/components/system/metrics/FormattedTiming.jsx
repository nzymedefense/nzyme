import React from 'react';
import Reflux from 'reflux';
import numeral from "numeral";

class FormattedTiming extends Reflux.Component {

    render() {
        return (
            <span>
                {numeral(this.props.timing > 1000 ? this.props.timing / 1000 : this.props.timing).format(this.props.format)}
                {this.props.timing > 1000 ? " ms" : " µs" }
            </span>
        )
    }

}

export default FormattedTiming;