import React, {useEffect, useState} from "react";
import Routes from "../../../../../../util/ApiRoutes";
import ApiRoutes from "../../../../../../util/ApiRoutes";
import {Navigate, useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import ActionFormProxy from "./forms/ActionFormProxy";

const authenticationMgmtService = new AuthenticationManagementService();

function CreateActionPage() {

  const { organizationId } = useParams();

  const [complete, setComplete] = useState(false);

  const [organization, setOrganization] = useState(null);
  const [type, setType] = useState("");

  useEffect(() => {
    authenticationMgmtService.findOrganization(organizationId, setOrganization);
  }, [organizationId])

  if (!organization) {
    return <LoadingSpinner />
  }

  if (complete) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)} />
  }

  return (
      <React.Fragment>
        <div className="row">

          <div className="col-md-10">
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
                <li className="breadcrumb-item">Actions</li>
                <li className="breadcrumb-item active" aria-current="page">Create</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end" href={Routes.SYSTEM.EVENTS.INDEX}>Back</a>
          </div>
        </div>

        <div className="row">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Create Action</h3>

                <label htmlFor="actiontype" className="form-label">Action Type</label>
                <select id="actiontype"
                        className="form-select"
                        value={type} onChange={(e) => setType(e.target.value)}>
                  <option value="">Please select an action type</option>
                  <option value="email">Send email</option>
                  <option value="splunk_message">Send Splunk message</option>
                  <option value="opensearch_message">Send OpenSearch message</option>
                  <option value="graylog_message">Send Graylog message</option>
                  <option value="wasm_exec">Execute WASM binary</option>
                </select>

                <ActionFormProxy type={type} setComplete={setComplete} organizationId={organization.id} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateActionPage;