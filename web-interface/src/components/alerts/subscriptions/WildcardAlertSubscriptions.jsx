import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import EventSubscriptionsTable from "../../system/events/shared/subscriptions/EventSubscriptionsTable";
import {notify} from "react-notify-toast";
import EventSubscriptionActionSelector from "../../system/events/shared/subscriptions/EventSubscriptionActionSelector";
import EventActionsService from "../../../services/EventActionsService";
import WithExactRole from "../../misc/WithExactRole";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";

const eventActionsService = new EventActionsService();

function WildcardAlertSubscriptions() {

  // eslint-disable-next-line no-unused-vars
  const [organizationId, tenantId] = useSelectedTenant();

  const [subscriptions, setSubscriptions] = useState(null);
  const [actions, setActions] = useState(null);

  const [revision, setRevision] = useState(0);
  const [subscriptionError, setSubscriptionError] = useState(null);

  const onUnsubscribeClick = function(subscriptionId) {
    if (!confirm("Really unsubscribe wildcard action?")) {
      return;
    }

    eventActionsService.unsubscribeWildcardAction(subscriptionId, function() {
      notify.show("Unsubscribed wildcard action.", "success");
      setRevision(revision+1);
    })
  }

  const onActionSelect = function(actionId) {
    eventActionsService.subscribeWildcardAction(actionId, organizationId,function() {
      notify.show("Subscribed wildcard action.", "success");
      setRevision(revision+1);
    }, function(error) {
      setSubscriptionError(error.response.data.message);
    });
  }

  useEffect(() => {
    setSubscriptions(null);
    setActions(null);
    eventActionsService.findAllDetectionAlertWildcardSubscriptions(organizationId, setSubscriptions);
    eventActionsService.findAllActionsOfOrganization(organizationId, setActions, 9999999, 0);
  }, [organizationId, revision]);

  if (!subscriptions || !actions) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <EventSubscriptionsTable organizationId={organizationId}
                                 subscriptions={subscriptions}
                                 onUnsubscribeClick={onUnsubscribeClick} />

        <h4 className="mt-4 mb-0">Subscribe Action</h4>

        <WithExactRole role="SUPERADMIN">
          <div className="alert alert-warning mt-2 mb-2">
            You are logged in as super administrator. Remember that only organization actions can be subscribed to
            alerts. You will not find super administrator actions here. Create organization actions in the organization
            settings.
          </div>
        </WithExactRole>

        <EventSubscriptionActionSelector onSubmit={onActionSelect}
                                         actions={actions.actions}
                                         subscriptionError={subscriptionError} />
      </React.Fragment>
  )

}

export default WildcardAlertSubscriptions;