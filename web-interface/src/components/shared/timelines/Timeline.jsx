import React from "react";

import numeral from "numeral";
import TimelineEvent from "./TimelineEvent";
import Paginator from "../../misc/Paginator";

function buildStatusEvent(events, addressLastSeen) {
  if (!events || events.length === 0) {
    return null;
  }

  const latest = events[0];

  if (latest.event_type === 'GONE' && latest.event_details?.ongoing === true) {
    return null;
  }

  return {
    event_type: 'SYNTHETIC_ACTIVE',
    event_details: {since: addressLastSeen},
    timestamp: new Date().toISOString()
  };
}

export default function Timeline({events, addressLastSeen, page, setPage, perPage}) {

  if (events.total === 0) {
    const statusOnly = {
      event_type: 'SYNTHETIC_ACTIVE',
      event_details: {since: addressLastSeen},
      timestamp: new Date().toISOString()
    };
    return (
      <ul className="timeline-event-list timeline-event-list-single mt-4">
        <TimelineEvent event={statusOnly} />
      </ul>
    );

  }

  const statusEvent = buildStatusEvent(events.events, addressLastSeen);
  const displayEvents = statusEvent ? [statusEvent, ...events.events] : events.events;

  return (
    <>
      <div className="timeline-status-row">
        <span className="timeline-total">
          <strong>Total Events:</strong> {numeral(events.total).format('0,0')}
        </span>

        <span className="pull-right text-muted">
          Events are calculated every 5 minutes.
        </span>
      </div>

      <ul className="timeline-event-list mb-4">
        {displayEvents.map((event, i) => (
          <TimelineEvent key={i} event={event} />
        ))}
      </ul>

      <Paginator page={page} setPage={setPage} perPage={perPage} itemCount={events.total} />
    </>
  )

}