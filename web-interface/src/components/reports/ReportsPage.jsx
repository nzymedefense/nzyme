import React from 'react';
import Routes from "../../util/Routes";
import ReportsTable from "./ReportsTable";

class ReportsPage extends React.Component {

  render() {
    return (
        <div>
          <div className="row">
            <div className="col-md-12">
              <h1>Reports</h1>
            </div>
          </div>

          <div className="row">
            <div className="col-md-9">
              <p>
                Reports can be generated on demand, scheduled to be generated periodically and stored in nzyme as well
                as sent out to multiple email addresses.
              </p>
            </div>

            <div className="col-md-3">
              <div className="float-right">
                <a href="https://go.nzyme.org/reporting" className="btn btn-primary">Help</a>
                &nbsp;
                <a href={Routes.SYSTEM.REPORTS.SCHEDULE} className="btn btn-success">Schedule Report</a>
              </div>
            </div>
          </div>

          <div className="row">
            <div className="col-md-12">
              <ReportsTable />
            </div>
          </div>
        </div>
    )
  }

}

export default ReportsPage;