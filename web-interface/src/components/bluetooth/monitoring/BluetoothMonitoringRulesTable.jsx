import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";

export default function BluetoothMonitoringRulesTable(props) {

  const rules = props.rules;
  const page = props.page;
  const setPage = props.setPage;

  if (!rules) {
    return <LoadingSpinner />
  }

  if (rules.count === 0) {
    return <div className="alert alert-info mb-0">
      No Bluetooth monitoring rules configured.
    </div>
  }

  return (
    <React.Fragment>
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Name</th>
        </tr>
        </thead>
        <tbody>

        </tbody>
      </table>
    </React.Fragment>
  )

}