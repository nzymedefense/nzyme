import React, {useEffect, useState} from "react";
import SystemService from "../../../services/SystemService";
import Paginator from "../../misc/Paginator";
import LoadingSpinner from "../../misc/LoadingSpinner";
import moment from "moment";
import EventsTableFilters from "./EventsTableFilters";
import EventsTable from "./EventsTable";

const systemService = new SystemService();

function Events() {

  const PER_PAGE = 25;

  const [events, setEvents] = useState();
  const [page, setPage] = useState(1);

  const [filters, setFilters] = useState(["SYSTEM", "DETECTION"]);

  useEffect(() => {
    setEvents(null);
    systemService.findAllEvents(setEvents, PER_PAGE, (page-1)*PER_PAGE, filters);
  }, [page, filters]);

  return (
      <React.Fragment>
        <EventsTableFilters filters={filters} setFilters={setFilters} />

        <EventsTable events={events} perPage={PER_PAGE} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default Events;