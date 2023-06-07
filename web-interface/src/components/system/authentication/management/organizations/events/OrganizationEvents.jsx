import React, {useEffect, useState} from "react";
import EventsTableFilters from "../../../../events/shared/events/EventsTableFilters";
import EventsTable from "../../../../events/shared/events/EventsTable";
import SystemService from "../../../../../../services/SystemService";

const systemService = new SystemService();

function OrganizationEvents(props) {

  const organizationId = props.organizationId;

  const PER_PAGE = 25;

  const [events, setEvents] = useState();
  const [page, setPage] = useState(1);

  const [filters, setFilters] = useState(["SYSTEM", "DETECTION"]);

  useEffect(() => {
    setEvents(null);
    systemService.findAllEvents(setEvents, PER_PAGE, (page-1)*PER_PAGE, filters, organizationId);
  }, [page, filters]);

  return (
      <React.Fragment>
        <EventsTableFilters filters={filters} setFilters={setFilters} />

        <EventsTable events={events} perPage={PER_PAGE} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default OrganizationEvents;