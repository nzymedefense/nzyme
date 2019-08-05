import React from 'react';
import numeral from "numeral";

class FrameCount extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        if (this.props.alert.frame_count) {
            return (
                <span>{numeral(this.props.alert.frame_count).format('0,0')}</span>
            )
        } else {
            return (
                <span>n/a</span>
            )
        }
    }

}

export default FrameCount;