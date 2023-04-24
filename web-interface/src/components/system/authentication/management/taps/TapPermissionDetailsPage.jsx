import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import Routes from "../../../../../util/ApiRoutes";
import ApiRoutes from "../../../../../util/ApiRoutes";
import {notify} from "react-notify-toast";
import TapSecret from "./TapSecret";

const authenticationManagementService = new AuthenticationManagementService();

function TapPermissionDetailsPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();
  const { tapUuid } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [tap, setTap] = useState(null);

  const [redirect, setRedirect] = useState(false);
  const [revision, setRevision] = useState(0);

  const deleteTap = function() {
    if (!confirm("Really delete tap?")) {
      return;
    }

    authenticationManagementService.deleteTapPermission(organizationId, tenantId, tapUuid, function() {
      setRedirect(true);
      notify.show('Tap deleted.', 'success');
    });
  }

  const cycleSecret = function() {
    if (!confirm("Really cycle tap secret? You will have to change the secret in the tap configuration file to comtinue using the tap.")) {
      return;
    }

    authenticationManagementService.cycleTapSecret(organizationId, tenantId, tapUuid, function() {
      setTap(null);
      setRevision(revision + 1);
      notify.show('Tap secret cycled.', 'success');
    });
  }

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
    authenticationManagementService.findTapPermission(organizationId, tenantId, tapUuid, setTap);
  }, [organizationId, tenantId, revision])

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)} />
  }

  if (!organization || !tenant || !tap) {
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
                <li className="breadcrumb-item">Tenants</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
                    {tenant.name}
                  </a>
                </li>
                <li className="breadcrumb-item">Taps</li>
                <li className="breadcrumb-item active" aria-current="page">{tap.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-secondary"
                 href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
                Back
              </a>{' '}
              <a className="btn btn-primary" href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.EDIT(organization.id, tenant.id, tap.uuid)}>
                Edit Tap
              </a>
            </span>
          </div>

          <div className="col-md-12">
            <h1>Authentication of Tap &quot;{tap.name}&quot;</h1>
          </div>

          <div className="row mt-3">
            <div className="col-md-8">
              <div className="row">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>Description</h3>

                      <p className="mb-0">
                        {tap.description}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="row mt-3">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>Tap Secret</h3>

                      <p>The tap secret is used to authenticate taps when they connect to nzyme. Every tap has a unique
                      secret that must be configured in the tap configuration file. <strong>You must update the secret
                      in your tap configuration file after cycling it.</strong></p>

                      <TapSecret secret={tap.secret} onCycle={cycleSecret} />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="row">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>Delete Tap</h3>

                      <p>
                        <strong>Warning:</strong> Deleted taps cannot be restored. The deleted tap will not be able to
                        report data until you create a new tap and cycle the tap secret. Existing data will not be
                        removed.
                      </p>

                      <button className="btn btn-sm btn-danger" onClick={deleteTap}>
                        Delete Tap
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default TapPermissionDetailsPage;