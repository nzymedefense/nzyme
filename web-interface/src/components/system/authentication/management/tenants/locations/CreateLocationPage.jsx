import React, {useEffect, useState} from "react";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import {Navigate, useParams} from "react-router-dom";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import ApiRoutes from "../../../../../../util/ApiRoutes";
import LocationForm from "./LocationForm";
import {notify} from "react-notify-toast";

const authenticationManagementService = new AuthenticationManagementService();

function CreateLocationPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);

  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
  }, [organizationId, tenantId])

  const create = (name, description) => {
    authenticationManagementService.createTenantLocation(organization.id, tenant.id, name, description, () => {
      notify.show('Location created.', 'success');
      setRedirect(true);
    })
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS_PAGE(organization.id, tenant.id)} />
  }

  if (!organization || !tenant) {
    return <LoadingSpinner />
  }

  return (
      <div className="row">
        <div className="col-md-9">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Authentication &amp; Authorization</a>
              </li>
              <li className="breadcrumb-item">Organizations</li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
                  {organization.name}
                </a>
              </li>
              <li className="breadcrumb-item">Tenants</li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
                  {tenant.name}
                </a>
              </li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS_PAGE(organization.id, tenant.id)}>
                  Locations
                </a>
              </li>
              <li className="breadcrumb-item active" aria-current="page">Create</li>
            </ol>
          </nav>
        </div>

        <div className="col-md-3">
          <span className="float-end">
            <a className="btn btn-secondary"
               href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS_PAGE(organization.id, tenant.id)}>
              Back
            </a>
          </span>
        </div>

        <div className="col-md-12">
          <h1>Create Location</h1>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Create Location</h3>

                <LocationForm submitText="Create Location" onSubmit={create} />
              </div>
            </div>
          </div>
        </div>
      </div>
  )

}

export default CreateLocationPage;