import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import SystemService from "../../../../../../../services/SystemService";
import EventTypesTable from "../../../../../events/shared/subscriptions/EventTypesTable";

const systemService = new SystemService();

function OrganizationEventSubscriptions(props) {

  const PER_PAGE = 15;

  const organizationId = props.organizationId;

  const [page, setPage] = useState(1);

  const [eventTypes, setEventTypes] = useState(null);

  useEffect(() => {
    setEventTypes(null);
    systemService.findAllEventTypesOfOrganization(setEventTypes, organizationId, PER_PAGE, (page-1)*PER_PAGE, ["AUTHENTICATION"]);
  }, [page])

  if (!eventTypes) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <EventTypesTable eventTypes={eventTypes}
                         perPage={PER_PAGE}
                         setPage={setPage}
                         page={page}
                         organizationId={organizationId} />
      </React.Fragment>
  )

}

export default OrganizationEventSubscriptions;