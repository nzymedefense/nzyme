import React from 'react';
import moment from "moment";

class ReportFireTime extends React.Component {

  render() {
    const time = this.props.time;

    if (Date.parse(time) > 0) {
      return <span title={moment(time).fromNow()}>{moment(time).format("lll")}</span>
    } else {
      return <span>n/a</span>;
    }
  }

}

export default ReportFireTime;