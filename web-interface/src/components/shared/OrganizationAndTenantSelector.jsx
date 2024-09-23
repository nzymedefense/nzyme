import React, {useContext, useEffect, useState} from "react";
import {UserContext} from "../../App";
import LoadingSpinner from "../misc/LoadingSpinner";
import AuthenticationManagementService from "../../services/AuthenticationManagementService";
import TenantSelector from "./TenantSelector";
import OrganizationSelector from "./OrganizationSelector";
import RefreshGears from "../misc/RefreshGears";

const authenticationManagementService = new AuthenticationManagementService();

function OrganizationAndTenantSelector(props) {

  const onOrganizationChange = props.onOrganizationChange;
  const onTenantChange = props.onTenantChange;

  // Optional.
  const organizationSelectorTitle = props.organizationSelectorTitle;
  const tenantSelectorTitle = props.tenantSelectorTitle;
  const emptyOrganizationTitle = props.emptyOrganizationTitle;
  const emptyTenantTitle = props.emptyTenantTitle;

  const [organization, setOrganization] = useState("");
  const [tenant, setTenant] = useState("");

  const [userOrganization, setUserOrganization] = useState(null)
  const [userTenant, setUserTenant] = useState(null);

  const [organizations, setOrganizations] = useState(null);
  const [tenants, setTenants] = useState(null);

  const [tenantsLoading, setTenantsLoading] = useState(false);

  const [loaded, setLoaded] = useState(false);

  const user = useContext(UserContext);

  useEffect(() => {
    setLoaded(false);

    if (user.is_superadmin) {
      setUserOrganization(null);
      setUserTenant(null);

      authenticationManagementService.findAllOrganizations(setOrganizations,
          250, 0, function() {
            setLoaded(true);
      });
      return
    }

    if (user.is_orgadmin) {
      setUserOrganization(user.organization_id);
      setUserTenant(null);
      onOrganizationChange(user.organization_id);

      authenticationManagementService.findAllTenantsOfOrganization(user.organization_id, setTenants,
          250, 0, function() {
        setLoaded(true);
      });
      return
    }

    // Tenant user.
    setUserOrganization(user.organization_id);
    setUserTenant(user.tenant_id);
    onOrganizationChange(user.organization_id);
    onTenantChange(user.tenant_id);

    setLoaded(true);
  }, []);

  useEffect(() => {
    if (loaded) {
      onTenantChange(null);
      setTenants(null);
    }

    if (organization) {
      setTenantsLoading(true);
      authenticationManagementService.findAllTenantsOfOrganization(organization, setTenants,
          250, 0, function () {
        setTenantsLoading(false);
      });
    }
  }, [organization]);

  useEffect(() => {
    if (tenant) {
      onTenantChange(tenant);
    } else {
      if (loaded) {
        onTenantChange(null);
      }
    }
  }, [tenant]);

  useEffect(() => {
    if (organization) {
      onOrganizationChange(organization);
    } else {
      if (loaded) {
        onOrganizationChange(null);
        onTenantChange(null);
        setTenant(null);
      }
    }
  }, [organization]);

  if (!loaded) {
    return <LoadingSpinner />
  }

  if (userOrganization === null && userTenant == null) {
    // Superadmin. Let user select any organization and any tenant.
    return (
        <React.Fragment>
          <label className="form-label">
            {organizationSelectorTitle ? organizationSelectorTitle : "Organization"}
          </label>

          <OrganizationSelector organization={organization}
                                setOrganization={setOrganization}
                                organizations={organizations}
                                emptyTitle={emptyOrganizationTitle} />

          <label className="form-label">
            {tenantSelectorTitle ? tenantSelectorTitle : "Tenant"} {tenantsLoading ? <RefreshGears /> : null}
          </label>

          <TenantSelector tenant={tenant} setTenant={setTenant} tenants={tenants} emptyTitle={emptyTenantTitle} />
        </React.Fragment>
    )
  }

  if (userOrganization && userTenant == null) {
    // Org admin. Let user select any tenant of their organization.
    return (
        <React.Fragment>
          <label className="form-label">
            {tenantSelectorTitle ? tenantSelectorTitle : "Tenant"}
          </label>

          <TenantSelector tenant={tenant} setTenant={setTenant} tenants={tenants} emptyTitle={emptyTenantTitle} />
        </React.Fragment>
    )
  }

  // Tenant user. Org and tenant ID were returned during load. Nothing to select.
  return null;
}

export default OrganizationAndTenantSelector;