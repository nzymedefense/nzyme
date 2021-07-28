import React from 'react';
import moment from "moment";

class ReportFireTime extends React.Component {

  render() {
    const time = this.props.time;

    console.log(time);

    if (Date.parse(time) > 0) {
      return moment(time).format("lll")
    } else {
      return "n/a";
    }
  }

}

export default ReportFireTime;