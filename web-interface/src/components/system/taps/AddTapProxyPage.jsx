import React, {useState} from "react";
import Routes from "../../../util/ApiRoutes";
import OrganizationAndTenantSelector from "../../shared/OrganizationAndTenantSelector";
import ApiRoutes from "../../../util/ApiRoutes";

function AddTapProxyPage() {

  const [organizationId, setOrganizationId] = useState(null);
  const [tenantId, setTenantId] = useState(null);

  const onOrganizationChange = (organizationId) => {
    setOrganizationId(organizationId)
  }

  const onTenantChange = (tenantId) => {
    setTenantId(tenantId);
  }

  const button = () => {
    if (organizationId && tenantId) {
      return (
          <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.CREATE(organizationId, tenantId)}
             className="btn btn-primary">
            Add Tap
          </a>
      )
    } else {
      return (
          <button className="btn btn-primary" disabled={true}>
            Add Tap
          </button>
      )
    }
  }

  return (
      <React.Fragment>
        <div className="row">

          <div className="col-md-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={Routes.SYSTEM.TAPS.INDEX}>Taps</a></li>
                <li className="breadcrumb-item active" aria-current="page">Add</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end" href={Routes.SYSTEM.TAPS.INDEX}>Back</a>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>Add Tap</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Please select a tenant first</h3>

                <p>
                  Remember that taps are managed in the{' '}
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>authentication section</a> and this page
                  is only a shortcut.
                </p>

                <OrganizationAndTenantSelector onOrganizationChange={onOrganizationChange}
                                               onTenantChange={onTenantChange} />

                {button()}
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default AddTapProxyPage;