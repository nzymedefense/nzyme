import React from "react";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import {truncate} from "../../../../util/Tools";
import ApiRoutes from "../../../../util/ApiRoutes";

function CustomBanditsTable(props) {

  const bandits = props.bandits;

  if (!bandits) {
    return <LoadingSpinner />
  }

  if (bandits.bandits.length === 0) {
    return <div className="alert alert-info mt-3">No custom bandits defined yet.</div>
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Name</th>
            <th>Description</th>
          </tr>
          </thead>
          <tbody>
          {bandits.bandits.map((bandit, i) => {
            return (
                <tr key={"bandit-" + i}>
                  <td><a href={ApiRoutes.DOT11.MONITORING.BANDITS.CUSTOM_DETAILS(bandit.id)}>{bandit.name}</a></td>
                  <td>{truncate(bandit.description, 200, true)}</td>
                </tr>
            )
          })}
          </tbody>
        </table>
      </React.Fragment>
  )

}

export default CustomBanditsTable;