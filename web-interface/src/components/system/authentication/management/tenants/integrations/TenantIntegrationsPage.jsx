import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import Routes from "../../../../../../util/ApiRoutes";
import ApiRoutes from "../../../../../../util/ApiRoutes";
import SectionMenuBar from "../../../../../shared/SectionMenuBar";
import {TENANT_MENU_ITEMS} from "../TenantMenuItems";
import CardTitleWithControls from "../../../../../shared/CardTitleWithControls";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import CotOutputsTable from "./cot/CotOutputsTable";

const authenticationManagementService = new AuthenticationManagementService();

export default function TenantIntegrationsPage() {

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
                          activeRoute={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS_PAGE(organization.id, tenant.id)}/>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <h1>Integrations of Tenant &quot;{tenant.name}&quot;</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-xl-12 col-xxl-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Cursor on Target (CoT)" slim={true}/>

              <p>
                Cursor on Target (CoT) is an XML-based standard for real-time exchange of geospatial and targeting
                information across different systems. Developed by The MITRE Corporation, it’s often integrated with
                the Tactical Assault Kit (TAK) to share critical data.
              </p>

              <CotOutputsTable organizationId={organizationId} tenantId={tenantId} />

              <div className="mt-2">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.CREATE(organization.id, tenant.id)}
                   className="btn btn-sm btn-secondary">
                  Create Cursor on Target Output
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}