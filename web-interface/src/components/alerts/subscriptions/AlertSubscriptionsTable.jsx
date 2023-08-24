import React, {useContext, useEffect, useState} from "react";
import {UserContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../services/AuthenticationManagementService";
import OrganizationSelector from "../../shared/OrganizationSelector";
import DetectionAlertsService from "../../../services/DetectionAlertsService";
import Paginator from "../../misc/Paginator";
import Subsystem from "../../misc/Subsystem";
import ApiRoutes from "../../../util/ApiRoutes";

const authenticationManagementService = new AuthenticationManagementService();
const detectionAlertService = new DetectionAlertsService();

function AlertSubscriptionsTable() {

  const user = useContext(UserContext);

  const [organizationUUID, setOrganizationUUID] = useState(null);
  const [organization, setOrganization] = useState(null);
  const [organizations, setOrganizations] = useState(null);

  const [types, setTypes] = useState(null);

  const perPage = 25;
  const [page, setPage] = useState(1);

  const resetOrganization = (e) => {
    e.preventDefault();
    setOrganizationUUID(null);
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
      detectionAlertService.findAllAlertTypes(setTypes, organizationUUID, perPage, (page-1)*perPage)
    }
  }, [organizationUUID]);

  if (!organizations) {
    return <LoadingSpinner />
  }

  if (!organizationUUID) {
    return <OrganizationSelector organizations={organizations}
                                organization={organizationUUID}
                                setOrganization={setOrganizationUUID} />
  }

  if (!types || !organization) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        {organization ? <div className="mb-2"><strong>Organization:</strong> {organization.name} <a href="#" onClick={resetOrganization}>Change</a></div> : null}

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Subsystem</th>
            <th>Detection Name</th>
            <th>Subscriptions</th>
            <th>&nbsp;</th>
          </tr>
          </thead>
          <tbody>
          {types.types.map(function(type, i) {
            return (
                <tr key={"at-" + i}>
                  <td><Subsystem subsystem={type.subsystem} /></td>
                  <td>{type.title}</td>
                  <td>{type.subscriptions.length}</td>
                  <td>
                    <a href={ApiRoutes.ALERTS.SUBSCRIPTIONS.DETAILS(organization.id, type.name.toLowerCase())}>Manage</a>
                  </td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={types.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default AlertSubscriptionsTable;