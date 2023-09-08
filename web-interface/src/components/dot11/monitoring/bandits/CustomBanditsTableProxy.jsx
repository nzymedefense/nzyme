import React, {useContext, useEffect, useState} from "react";
import {UserContext} from "../../../../App";
import Dot11Service from "../../../../services/Dot11Service";
import OrganizationAndTenantSelector from "../../../shared/OrganizationAndTenantSelector";
import CustomBanditsTable from "./CustomBanditsTable";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../../util/ApiRoutes";

const authenticationManagementService = new AuthenticationManagementService();
const dot11Service = new Dot11Service();

function CustomBanditsTableProxy() {

  const user = useContext(UserContext);

  const [bandits, setBandits] = useState(null);

  const [organizationUUID, setOrganizationUUID] = useState(null);
  const [tenantUUID, setTenantUUID] = useState(null);

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);

  const perPage = 25;
  const [page, setPage] = useState(1);

  const onOrganizationChange = (organizationUUID) => {
    if (organizationUUID) {
      setOrganizationUUID(organizationUUID)
    }
  }

  const onTenantChange = (tenantUUID) => {
    if (tenantUUID) {
      setTenantUUID(tenantUUID);
    }
  }

  const resetOrganizationAndTenant = (e) => {
    e.preventDefault();

    setOrganizationUUID(null);
    setTenantUUID(null);
  }

  useEffect(() => {
    if (organizationUUID && tenantUUID) {
      authenticationManagementService.findOrganization(organizationUUID, setOrganization);
      authenticationManagementService.findTenantOfOrganization(organizationUUID, tenantUUID, setTenant);

      setBandits(null);
      dot11Service.findCustomBandits(organizationUUID, tenantUUID, perPage, (page-1)*perPage, setBandits)
    }
  }, [organizationUUID, tenantUUID, page]);

  if (!organizationUUID || !tenantUUID) {
    return <OrganizationAndTenantSelector
        onOrganizationChange={onOrganizationChange}
        onTenantChange={onTenantChange} />
  }

  if (!organization || !tenant) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        {user.is_superadmin || user.is_orgadmin ? <div className="mb-2"><strong>Organization:</strong> {organization.name}, <strong>Tenant:</strong> {tenant.name} <a href="#" onClick={resetOrganizationAndTenant}>Change</a></div> : null}

        <CustomBanditsTable bandits={bandits} />

        <a href={ApiRoutes.DOT11.MONITORING.BANDITS.CREATE(organization.id, tenant.id)} className="btn btn-sm btn-secondary">Create Custom Bandit</a>
      </React.Fragment>
  )

}

export default CustomBanditsTableProxy;