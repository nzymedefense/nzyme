import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../../../util/ApiRoutes";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import Routes from "../../../../../util/ApiRoutes";
import TapPermissionForm from "./TapPermissionForm";
import {notify} from "react-notify-toast";

const authenticationManagementService = new AuthenticationManagementService();

function EditTapPermissionsPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();
  const { tapUuid } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [tap, setTap] = useState(null);

  const [redirect, setRedirect] = useState(false);

  const onFormSubmitted = function (name, description) {
    authenticationManagementService.editTapAuthentication(organization.id, tenant.id, tap.uuid, name, description, function() {
      setRedirect(true);
      notify.show('Tap details updated.', 'success');
    })
  }

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
    authenticationManagementService.findTapPermission(organizationId, tenantId, tapUuid, setTap);
  }, [organizationId, tenantId])

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.DETAILS(organization.id, tenant.id, tap.uuid)} />
  }

  if (!organization || !tenant || !tap) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-9">
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
                <li className="breadcrumb-item">Tenants</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
                    {tenant.name}
                  </a>
                </li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.TAPS_PAGE(organization.id, tenant.id)}>
                    Taps
                  </a>
                </li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.DETAILS(organization.id, tenant.id, tap.uuid)}>
                    {tap.name}
                  </a>
                </li>
                <li className="breadcrumb-item active" aria-current="page">Edit</li>
              </ol>
            </nav>
          </div>

          <div className="col-3">
            <span className="float-end">
              <a className="btn btn-secondary"
                 href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.DETAILS(organization.id, tenant.id, tap.uuid)}>
                Back
              </a>
            </span>
          </div>

          <div className="col-12">
            <h1>Edit Tap &quot;{tap.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Edit Tap Details</h3>

                <TapPermissionForm onClick={onFormSubmitted}
                                   name={tap.name}
                                   description={tap.description}
                                   submitText="Edit Tap"  />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default EditTapPermissionsPage;