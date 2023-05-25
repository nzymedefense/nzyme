import React, {useEffect, useState} from "react";
import SystemService from "../../../services/SystemService";
import Paginator from "../../misc/Paginator";
import LoadingSpinner from "../../misc/LoadingSpinner";
import moment from "moment";
import EventsTableFilters from "./EventsTableFilters";

const systemService = new SystemService();

function EventsTable() {

  const PER_PAGE = 25;

  const [events, setEvents] = useState();
  const [page, setPage] = useState(1);

  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState(["SYSTEM", "DETECTION"]);

  useEffect(() => {
    setEvents(null);
    systemService.findAllEvents(setEvents, PER_PAGE, (page-1)*PER_PAGE, filters);
  }, [page, filters]);

  if (!events) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <EventsTableFilters show={showFilters} filters={filters} setFilters={setFilters} />

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

        <Paginator itemCount={events.count} perPage={PER_PAGE} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default EventsTable;