import React, {useEffect, useState} from "react";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import {useParams} from "react-router-dom";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import OrganizationHeader from "./OrganizationHeader";
import SectionMenuBar from "../../../../shared/SectionMenuBar";
import {ORGANIZATION_MENU_ITEMS} from "./OrganizationMenuItems";
import ApiRoutes from "../../../../../util/ApiRoutes";
import CardTitleWithControls from "../../../../shared/CardTitleWithControls";
import OrganizationQuotasTable from "./OrganizationQuotasTable";
import usePageTitle from "../../../../../util/UsePageTitle";
import SubsystemsConfiguration from "../../../../shared/SubsystemsConfiguration";

const authenticationManagementService = new AuthenticationManagementService();

export default function OrganizationSubsystemsPage() {

  usePageTitle("Subsystems of Organization");

  const { organizationId } = useParams();

  const [organization, setOrganization] = useState(null);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
  }, [organizationId])

  if (!organization) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>
      <OrganizationHeader organization={organization}/>

      <div className="row">
        <div className="col-md-12">
          <SectionMenuBar items={ORGANIZATION_MENU_ITEMS(organization.id)}
                          activeRoute={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.SUBSYSTEMS_PAGE(organizationId)}/>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <h1>Subsystems of Organization &quot;{organization.name}&quot;</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-xl-12 col-xxl-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Subsystems" slim={true} />

              <p>
                This setting governs subsystem availability for all tenants in this organization. <strong>You can also
                configure subsystems at the tenant level</strong>, with the higher-level configuration taking
                precedence.
              </p>

              <SubsystemsConfiguration organizationUUID={organization.id}
                                       dbUpdateCallback={authenticationManagementService.updateSubsystemsConfigurationOfOrganization}/>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}