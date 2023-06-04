import React, {useEffect, useState} from "react";
import SystemService from "../../../../services/SystemService";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import EventTypesTable from "./EventTypesTable";
import EventTypesTableFilter from "./EventTypesTableFilter";

const systemService = new SystemService();

function EventSubscriptions(props) {

  const PER_PAGE = 15;

  const [page, setPage] = useState(1);

  const [eventTypes, setEventTypes] = useState(null);
  const [categories, setCategories] = useState(["AUTHENTICATION", "HEALTH_INDICATOR"])

  useEffect(() => {
    setEventTypes(null);
    systemService.findAllEventTypes(setEventTypes, PER_PAGE, (page-1)*PER_PAGE, categories);
  }, [page, categories])

  if (!eventTypes) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <EventTypesTableFilter categories={categories} setCategories={setCategories} />

        <EventTypesTable eventTypes={eventTypes} perPage={PER_PAGE} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default EventSubscriptions;