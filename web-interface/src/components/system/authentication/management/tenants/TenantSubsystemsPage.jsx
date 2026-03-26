import React, {useEffect, useState} from 'react';
import {useParams} from "react-router-dom";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import Routes from "../../../../../util/ApiRoutes";
import ApiRoutes from "../../../../../util/ApiRoutes";
import SectionMenuBar from "../../../../shared/SectionMenuBar";
import {TENANT_MENU_ITEMS} from "./TenantMenuItems";
import CardTitleWithControls from "../../../../shared/CardTitleWithControls";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import usePageTitle from "../../../../../util/UsePageTitle";
import SubsystemsConfiguration from "../../../../shared/SubsystemsConfiguration";

const authenticationManagementService = new AuthenticationManagementService();

export default function TenantSubsystemsPage() {

  usePageTitle("Subsystems of Tenant");

  const { organizationId } = useParams();
  const { tenantId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
  }, [organizationId, tenantId])

  if (!organization || !tenant) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-9">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item">
                <a href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Authentication &amp; Authorization</a>
              </li>
              <li className="breadcrumb-item">Organizations</li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
                  {organization.name}
                </a>
              </li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.TENANTS_PAGE(organization.id)}>
                  Tenants
                </a>
              </li>
              <li className="breadcrumb-item active" aria-current="page">{tenant.name}</li>
            </ol>
          </nav>
        </div>

        <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-primary"
                 href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.EDIT(organization.id, tenant.id)}>
                Edit Tenant
              </a>
            </span>
        </div>
      </div>

      <div className="row">
        <div className="col-md-12">
          <SectionMenuBar items={TENANT_MENU_ITEMS(organization.id, tenant.id)}
                          activeRoute={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.SUBSYSTEMS_PAGE(organization.id, tenant.id)}/>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <h1>Subsystems of Tenant &quot;{tenant.name}&quot;</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-xl-12 col-xxl-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Subsystems" slim={true} />

              <p>
                This setting governs subsystem availability for this tenant.
              </p>

              <SubsystemsConfiguration organizationUUID={organization.id}
                                       tenantUUID={tenant.id}
                                       dbUpdateCallback={authenticationManagementService.updateSubsystemsConfigurationOfTenantOfOrganization}/>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}