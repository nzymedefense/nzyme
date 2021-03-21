import React from 'react';
import numeral from "numeral";

class FormattedTiming extends React.Component {

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