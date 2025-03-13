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

const authenticationManagementService = new AuthenticationManagementService();

export default function OrganizationQuotasPage() {

  const { organizationId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [quotas, setQuotas] = useState(null);

  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
  }, [organizationId])

  useEffect(() => {
    setQuotas(null);
    authenticationManagementService.getQuotasOfOrganization(organizationId, setQuotas)
  }, [organizationId, revision])

  if (!organization) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <OrganizationHeader organization={organization}/>

        <div className="row">
          <div className="col-md-12">
            <SectionMenuBar items={ORGANIZATION_MENU_ITEMS(organization.id)}
                            activeRoute={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.QUOTAS_PAGE(organizationId)}/>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>Quotas of Organization &quot;{organization.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Quotas" slim={true} />

                <OrganizationQuotasTable quotas={quotas}
                                         organization={organization}
                                         onUpdate={() => setRevision(new Date())} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}