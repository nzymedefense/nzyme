import RESTClient from '../util/RESTClient'

class EventActionsService {

  findSystemEventType(eventTypeName, setEventType, organizationId = undefined) {
    const params = organizationId ? {organization_id: organizationId} : {};
    RESTClient.get("/system/events/types/system/show/" + eventTypeName, params, function(response) {
      setEventType(response.data);
    })
  }

  findAllActions(setActions, limit, offset) {
    RESTClient.get("/system/events/actions",
        {limit: limit, offset: offset}, function(response) {
          setActions(response.data);
        })
  }

  findAllActionsOfOrganization(organizationId, setActions, limit, offset) {
    RESTClient.get("/system/authentication/mgmt/organizations/show/" + organizationId + "/events/actions",
        {limit: limit, offset: offset}, function(response) {
      setActions(response.data);
    })
  }

  findAction(actionId, setAction) {
    RESTClient.get("/system/events/actions/show/" + actionId, {}, function(response) {
          setAction(response.data);
    })
  }

  findActionOfOrganization(organizationId, actionId, setAction) {
    RESTClient.get("/system/authentication/mgmt/organizations/show/" + organizationId + "/events/actions/show/" + actionId,
        {}, function(response) {
          setAction(response.data);
        })
  }

  deleteAction(actionId, successCallback) {
    RESTClient.delete("/system/events/actions/show/" + actionId,
        {}, successCallback);
  }

  subscribeActionToEvent(eventTypeName, actionId, organizationId, successCallback, errorCallback) {
    const data = organizationId ? {organization_id: organizationId, action_id: actionId} : {action_id: actionId};

    RESTClient.post("/system/events/types/system/show/" + eventTypeName + "/subscriptions",
        data, successCallback, errorCallback);
  }

  unsubscribeActionFromEvent(eventTypeName, subscriptionId, successCallback) {
    RESTClient.delete("/system/events/types/system/show/" + eventTypeName + "/subscriptions/show/" + subscriptionId,
        successCallback);
  }

  createEmailAction(organizationId, name, description, subjectPrefix, receivers, successCallback) {
    RESTClient.post("/system/events/actions/email", {
      "organization_id": organizationId,
      "name": name,
      "description": description,
      "subject_prefix": subjectPrefix,
      "receivers": receivers
    }, successCallback);
  }

  updateEmailAction(actionId, name, description, subjectPrefix, receivers, successCallback) {
    RESTClient.put("/system/events/actions/email/" + actionId, {
      "name": name,
      "description": description,
      "subject_prefix": subjectPrefix,
      "receivers": receivers
    }, successCallback);
  }

}

export default EventActionsService;