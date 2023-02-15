import React from "react";

function HealthConsoleConfiguration(props) {

  if (!props.indicators) {
    return <div className="alert alert-info">No indicators.</div>
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <p>
              You can enable or disable individual indicators. Disabled indicators will not run, not trigger alerts, and
              be marked as disabled.
            </p>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <table className="table table-sm table-hover table-striped">
              <thead>
              <tr>
                <th>Indicator</th>
                <th>Active</th>
              </tr>
              </thead>
              <tbody>
              <tr>
                <td></td>
                <td>
                </td>
                <td>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </React.Fragment>
  )

}

export default HealthConsoleConfiguration;