import React from 'react';
import LoadingSpinner from "../misc/LoadingSpinner";
import ReportsService from "../../services/ReportsService";
import ReportsTableRow from "./ReportsTableRow";

class ReportsTable extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      reports: undefined
    };

    this.reportsService = new ReportsService();
    this.reportsService.findAll = this.reportsService.findAll.bind(this);
  }

  componentDidMount() {
    this.reportsService.findAll();
  }

  render() {
    if (!this.state.reports) {
      return <LoadingSpinner />;
    }

    const self = this;

    return (
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Report Type</th>
            <th>Next Fire Time</th>
            <th>Previous Fire Time</th>
            <th>Status</th>
            <th>Schedule</th>
          </tr>
          </thead>
          <tbody>
            {Object.keys(this.state.reports).map(function (key,i) {
              return <ReportsTableRow key={"report-"+i} report={self.state.reports[key]} />
            })}
          </tbody>
        </table>
    )
  }

}

export default ReportsTable;