import React from "react";

function EventSubscriptionsTable(props) {

  const subscriptions = props.subscriptions;

  if (!subscriptions || subscriptions.length === 0) {
    return (
        <div className="alert alert-info mb-0">
          There are no actions subscribed to this event type.
        </div>
    )
  }

  return (
      subs
  )

}

export default EventSubscriptionsTable;