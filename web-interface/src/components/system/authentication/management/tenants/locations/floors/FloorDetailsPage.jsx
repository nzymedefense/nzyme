import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../../../../../util/ApiRoutes";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import Floorplan from "../../../../../../shared/floorplan/Floorplan";
import {notify} from "react-notify-toast";
import moment from "moment/moment";
import UploadFloorPlanForm from "./UploadFloorPlanForm";

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

  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
    authenticationManagementService.findTenantLocation(locationId, organizationId, tenantId, setLocation);
    authenticationManagementService.findFloorOfTenantLocation(organizationId, tenantId, locationId, floorId, setFloor);
  }, [organizationId, tenantId])

  const deleteFloor = () => {
    if (!confirm("Really delete floor? This will delete the associated floor plan as well as all tap placements, but " +
        "not the taps themselves.")) {
      return;
    }

    authenticationManagementService.deleteFloorOfTenantLocation(organizationId, tenantId, locationId, floorId, () => {
      notify.show('Floor deleted.', 'success');
      setRedirect(true);
    })
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.DETAILS(organization.id, tenant.id, location.id)} />
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
              <li className="breadcrumb-item active" aria-current="page">{floor.name} (#{floor.number})</li>
            </ol>
          </nav>
        </div>

        <div className="col-md-3">
          <span className="float-end">
            <a className="btn btn-secondary"
               href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.DETAILS(organization.id, tenant.id, location.id)}>
              Back
            </a>{' '}
            <a className="btn btn-primary"
               href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.FLOORS.EDIT(organization.id, tenant.id, location.id, floor.id)}>
                Edit Floor
            </a>
          </span>
        </div>

        <div className="col-md-12">
          <h1>Floor &quot;{floor.name}&quot; (#{floor.number}) of Location &quot;{location.name}&quot;</h1>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Metadata</h3>

                <dl className="mb-0">
                  <dt>Created At</dt>
                  <dd title={moment(floor.created_at).format()}>{moment(floor.created_at).fromNow()}</dd>
                  <dt>Updated At</dt>
                  <dd title={moment(floor.updated_at).format()}>{moment(floor.updated_at).fromNow()}</dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Delete Floor</h3>

                <button className="btn btn-sm btn-danger" onClick={deleteFloor}>
                  Delete Floor
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Floor Plan</h3>

                <Floorplan containerHeight={500}/>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Upload Floor Plan</h3>

                <UploadFloorPlanForm organizationId={organizationId} tenantId={tenantId} locationId={locationId} floorId={floorId} onSuccess={(e) => {}} submitText="Upload Floor Plan" />
              </div>
            </div>
          </div>
        </div>
      </div>
  )

}

export default FloorDetailsPage;