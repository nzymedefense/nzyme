import RESTClient from '../util/RESTClient'

class EventActionsService {

  createEmailAction(organizationId, name, description, subjectPrefix, receivers, successCallback) {
    RESTClient.post("/system/events/actions/email", {
          "organization_id": organizationId,
          "name": name,
          "description": description,
          "subject_prefix": subjectPrefix,
          "receivers": receivers
        }, successCallback);
  }

  findAllActionsOfOrganization(organizationId, setActions, limit, offset) {
    RESTClient.get("/system/authentication/mgmt/organizations/show/" + organizationId + "/events/actions",
        {limit: limit, offset: offset}, function(response) {
      setActions(response.data);
    })
  }

}

export default EventActionsService;