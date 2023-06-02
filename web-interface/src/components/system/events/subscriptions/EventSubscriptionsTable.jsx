import React from "react";
import Paginator from "../../../misc/Paginator";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import ApiRoutes from "../../../../util/ApiRoutes";

function EventSubscriptionsTable(props) {

  const eventTypes = props.eventTypes;
  const perPage = props.perPage;
  const setPage = props.setPage;
  const page = props.page;

  if (!eventTypes) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Category</th>
            <th>Name</th>
            <th>Subscriptions</th>
            <th>&nbsp;</th>
          </tr>
          </thead>
          <tbody>
          {eventTypes.types.map((type, i) => {
            return (
                <tr key={"eventtype-" + i}>
                  <td title={type.category_id}>{type.category_name}</td>
                  <td title={type.id}>{type.name}</td>
                  <td>{type.subscriptions}</td>
                  <td>
                    <a href={ApiRoutes.SYSTEM.EVENTS.SUBSCRIPTIONS.DETAILS(type.id.toLowerCase())}>Manage</a>
                  </td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={eventTypes.count} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default EventSubscriptionsTable;