import React from 'react';
import ReportName from "./ReportName";
import ReportFireTime from "./ReportFireTime";

class ReportsTableRow extends React.Component {
  render() {
    const report = this.props.report;

    return (
        <tr>
          <td><ReportName name={report.name} /></td>
          <td><ReportFireTime time={report.next_fire_time} /></td>
          <td><ReportFireTime time={report.previous_fire_time} /></td>
          <td>{report.trigger_state}</td>
          <td title={report.cron_expression}>{report.schedule_string}</td>
        </tr>
    )
  }

}

export default ReportsTableRow;