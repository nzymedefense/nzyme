import React, {useContext, useEffect, useState} from "react";
import AuthenticationManagementService from "../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import OrganizationSelector from "../../shared/OrganizationSelector";
import {UserContext} from "../../../App";
import EventSubscriptionsTable from "../../system/events/shared/subscriptions/EventSubscriptionsTable";
import {notify} from "react-notify-toast";
import EventSubscriptionActionSelector from "../../system/events/shared/subscriptions/EventSubscriptionActionSelector";
import EventActionsService from "../../../services/EventActionsService";
import WithExactRole from "../../misc/WithExactRole";

const authenticationManagementService = new AuthenticationManagementService();
const eventActionsService = new EventActionsService();

function WildcardAlertSubscriptions() {

  const user = useContext(UserContext);

  const [organizationUUID, setOrganizationUUID] = useState(null);
  const [organization, setOrganization] = useState(null);
  const [organizations, setOrganizations] = useState(null);

  const [subscriptions, setSubscriptions] = useState(null);
  const [actions, setActions] = useState(null);

  const [revision, setRevision] = useState(0);
  const [subscriptionError, setSubscriptionError] = useState(null);

  const resetOrganization = (e) => {
    e.preventDefault();
    setOrganizationUUID(null);
  }

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
    eventActionsService.subscribeWildcardAction(actionId, organizationUUID,function() {
      notify.show("Subscribed wildcard action.", "success");
      setRevision(revision+1);
    }, function(error) {
      setSubscriptionError(error.response.data.message);
    });
  }

  useEffect(() => {
    // If user is not orgadmin, they are super admin and have to select org manually.
    if (user.is_orgadmin) {
      setOrganizationUUID(user.organization_id);
      setOrganizations([]);
    } else {
      // Superadmin.
      authenticationManagementService.findAllOrganizations(setOrganizations, 250, 0);
    }
  }, []);

  useEffect(() => {
    if (organizationUUID) {
      authenticationManagementService.findOrganization(organizationUUID, setOrganization);
      eventActionsService.findAllDetectionAlertWildcardSubscriptions(organizationUUID, setSubscriptions);
      eventActionsService.findAllActionsOfOrganization(organizationUUID, setActions, 9999999, 0);
    }
  }, [organizationUUID, revision]);

  if (!organizations) {
    return <LoadingSpinner />
  }

  if (!organizationUUID) {
    return <OrganizationSelector organizations={organizations}
                                 organization={organizationUUID}
                                 setOrganization={setOrganizationUUID} />
  }

  if (!subscriptions || !actions || !organization) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        {user.is_superadmin ? <div className="mb-2"><strong>Organization:</strong> {organization.name} <a href="#" onClick={resetOrganization}>Change</a></div> : null}

        <EventSubscriptionsTable organizationId={organizationUUID}
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