import React from "react";
import moment from "moment/moment";
import Paginator from "../../../../misc/Paginator";
import LoadingSpinner from "../../../../misc/LoadingSpinner";

function EventsTable(props) {

  const events = props.events;
  const perPage = props.perPage;
  const setPage = props.setPage;
  const page = props.page;

  if (events === null || events === undefined) {
    return <LoadingSpinner />
  }

  if (events.events.length === 0) {
    return <div className="alert alert-info mt-1 mb-0">No events found.</div>
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Timestamp</th>
            <th>Type</th>
            <th>Details</th>
          </tr>
          </thead>
          <tbody>
          {events.events.map((event, i) => {
            return (
                <tr key={"event-" + i}>
                  <td title={moment(event.created_at).fromNow()}>
                    {moment(event.created_at).format()}
                  </td>
                  <td>{event.event_type}</td>
                  <td>{event.details}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={events.count} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default EventsTable;