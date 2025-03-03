import React, {useContext, useEffect, useState} from "react";
import {UserContext} from "../../App";
import AuthenticationManagementService from "../../services/AuthenticationManagementService";
import LoadingSpinner from "../misc/LoadingSpinner";

const authenticationManagementService = new AuthenticationManagementService();

function SelectedOrganizationAndTenant(props) {

  const user = useContext(UserContext);

  const organizationId = props.organizationId;
  const tenantId = props.tenantId;
  const onReset = props.onReset;

  const noEdit = props.noEdit;

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);

  const editButton = () => {
    if (noEdit) {
      return null;
    }

    return (
        <li>
          <a href="#" onClick={(e) => {e.preventDefault(); onReset()}}>
            <i className="fa-solid fa-pen-to-square"></i>
          </a>
        </li>
    )
  }

  useEffect(() => {
    if (organizationId && (user.is_superadmin || user.is_orgadmin)) {
      authenticationManagementService.findOrganization(organizationId, setOrganization);
    }
  }, [organizationId]);

  useEffect(() => {
    if (tenantId && (user.is_superadmin || user.is_orgadmin)) {
      authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
    }
  }, [tenantId]);

  if ((user.is_superadmin || user.is_orgadmin) && (!organization || !tenant)) {
    return <LoadingSpinner />
  }

  if (user.is_superadmin) {
    // Super admins must know which org and tenant was selected.
    return (
        <React.Fragment>
          <ul className="selected-org-tenant">
            <li><span>Organization:</span> {organization.name}</li>
            <li><span>Tenant:</span> {tenant.name}</li>
            {editButton()}
          </ul>
        </React.Fragment>
    )
  }

  if (user.is_orgadmin) {
    // Org admins should know about which tenant was selected.
    return (
        <React.Fragment>
          <ul className="selected-org-tenant">
            <li><span>Tenant:</span> {tenant.name}</li>
            {editButton()}
          </ul>
        </React.Fragment>
    )
  }

  // Tenant user. Only knows about their own tenant. Don't show any info.
  return null;

}

export default SelectedOrganizationAndTenant;