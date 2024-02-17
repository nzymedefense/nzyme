import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../../../../../util/ApiRoutes";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import FloorPlan from "../../../../../../shared/floorplan/FloorPlan";
import FloorForm from "./FloorForm";
import {notify} from "react-notify-toast";

const authenticationManagementService = new AuthenticationManagementService();

function FloorDetailsPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();
  const { locationId } = useParams();
  const { floorId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [location, setLocation] = useState(null);

  const [floor, setFloor] = useState(null);

  const [errorMessage, setErrorMessage] = useState(null);
  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
    authenticationManagementService.findTenantLocation(locationId, organizationId, tenantId, setLocation);
    authenticationManagementService.findFloorOfTenantLocation(organizationId, tenantId, locationId, floorId, setFloor);
  }, [organizationId, tenantId])

  const update = (name, description) => {
    authenticationManagementService.editFloorOfTenantLocation(organization.id, tenant.id, location.id, floor.id, name, description, () => {
      notify.show('Floor updated.', 'success');
      setRedirect(true);
    }, (error) => {
      setErrorMessage(error.response.data.message)
    })
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.FLOORS.DETAILS(organization.id, tenant.id, location.id, floor.id)} />
  }

  if (!organization || !tenant || !location || !floor) {
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
              <li className="breadcrumb-item">Locations</li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.DETAILS(organization.id, tenant.id, location.id)}>
                  {location.name}
                </a>
              </li>
              <li className="breadcrumb-item">Floors</li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.FLOORS.DETAILS(organization.id, tenant.id, location.id, floor.id)}>
                  {floor.name} (#{floor.number})
                </a>
              </li>
              <li className="breadcrumb-item active" aria-current="page">Edit</li>
            </ol>
          </nav>
        </div>

        <div className="col-md-3">
          <span className="float-end">
            <a className="btn btn-secondary" href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.FLOORS.DETAILS(organization.id, tenant.id, location.id, floor.id)}>
              Back
            </a>{' '}
          </span>
        </div>

        <div className="col-md-12">
          <h1>Edit Floor &quot;{floor.name}&quot; (#{floor.number}) of Location &quot;{location.name}&quot;</h1>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Edit Floor</h3>

                <FloorForm submitText="Update Floor" number={floor.number} name={floor.name}
                           onSubmit={update} errorMessage={errorMessage} />
              </div>
            </div>
          </div>
        </div>
      </div>
  )

}

export default FloorDetailsPage;