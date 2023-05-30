import React from "react";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import Paginator from "../../../../../misc/Paginator";

function ActionsTable(props) {

  const actions = props.actions;
  const perPage = props.perPage;
  const page = props.page;
  const setPage = props.setPage;

  if (!actions) {
    return <LoadingSpinner />
  }

  if (actions.actions.length === 0) {
    return (
        <div className="alert alert-info">
          No event actions configured.
        </div>
    )
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Type</th>
            <th>Name</th>
            <th>&nbsp;</th>
          </tr>
          </thead>
          <tbody>
          {actions.actions.map((action, i) => {
            return (
                <tr key={"eventaction-" + i}>
                  <td title={action.action_type}>
                    {action.action_type_human_readable}
                  </td>
                  <td>{action.name}</td>
                  <td>
                    <a href="">Details</a>
                  </td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={actions.count} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default ActionsTable;