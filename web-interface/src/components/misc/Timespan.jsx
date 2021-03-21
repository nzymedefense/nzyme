import React from 'react';
import moment from "moment";

class Timespan extends React.Component {

    constructor(props) {
        super(props);

        this.timespan = moment.duration(moment(this.props.to).diff(moment(this.props.from)));
    }

    render() {
        const minutes = this.timespan.asMinutes();
        if (minutes < 120) {
            return (
                <span>{Math.round(this.timespan.asMinutes())} min</span>
            )
        }

        if (minutes > 120) {
            return (
                <span>{Math.round(this.timespan.asHours())} hours</span>
            )
        }
    }

}

export default Timespan;