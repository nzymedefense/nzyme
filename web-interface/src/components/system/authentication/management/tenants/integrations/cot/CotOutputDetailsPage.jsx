import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import CotIntegrationService from "../../../../../../../services/integrations/CotIntegrationService";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import Routes from "../../../../../../../util/ApiRoutes";
import ApiRoutes from "../../../../../../../util/ApiRoutes";
import CardTitleWithControls from "../../../../../../shared/CardTitleWithControls";
import CotOutputStatus from "./CotOutputStatus";
import numeral from "numeral";
import moment from "moment";
import {notify} from "react-notify-toast";
import CotConnectionType from "./CotConnectionType";

const authenticationManagementService = new AuthenticationManagementService();
const cotIntegrationService = new CotIntegrationService();

export default function CotOutputDetailsPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();
  const { outputId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [output, setOutput] = useState(null);

  const [revision, setRevision] = useState(new Date());
  const [deleted, setDeleted] = useState(false);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);

    setOutput(null);
    cotIntegrationService.findOutput(setOutput, organizationId, tenantId, outputId);
  }, [organizationId, tenantId, outputId, revision])

  const onDelete = () => {
    if (!confirm("Really delete output?")) {
      return;
    }

    cotIntegrationService.deleteOutput(organizationId, tenantId, outputId, () => {
      notify.show("Cursor on Target output deleted.", "success");
      setDeleted(true);
    });
  }

  const onPause = () => {
    if (!confirm("Really pause output?")) {
      return;
    }

    cotIntegrationService.pauseOutput(organizationId, tenantId, outputId, () => {
      notify.show("Cursor on Target output paused.", "success");
      setRevision(new Date());
    });
  }

  const onStart = () => {
    if (!confirm("Really start output?")) {
      return;
    }

    cotIntegrationService.startOutput(organizationId, tenantId, outputId, () => {
      notify.show("Cursor on Target output started.", "success");
      setRevision(new Date());
    });
  }

  if (deleted) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS_PAGE(organizationId, tenantId)} />
  }

  if (!organization || !tenant || !output) {
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
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
                    {tenant.name}
                  </a>
                </li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS_PAGE(organization.id, tenant.id)}>
                    Integrations
                  </a>
                </li>
                <li className="breadcrumb-item">Cursor on Target</li>
                <li className="breadcrumb-item active">{output.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-secondary"
                 href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS_PAGE(organization.id, tenant.id)}>
                Back
              </a>&nbsp;

              <a className="btn btn-primary" href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.EDIT(organization.id, tenant.id, output.uuid)}>
                Edit Output
              </a>
            </span>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>Cursor on Target Output &quot;{output.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-8">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Details" slim={true} />

                <dl>
                  <dt>Address</dt>
                  <dd>{output.address}:{output.port}</dd>
                  <dt>Connection Type</dt>
                  <dd><CotConnectionType type={output.connection_type} /></dd>
                  <dt>Status</dt>
                  <dd><CotOutputStatus status={output.status} /></dd>
                  <dt>Sent Messages</dt>
                  <dd>{numeral(output.sent_messages).format("0,0")}</dd>
                  <dt>Sent Bytes</dt>
                  <dd>{numeral(output.sent_bytes).format("0 b")}</dd>
                  <dt>Created At</dt>
                  <dd title={moment(output.created_at).format()}>{moment(output.created_at).fromNow()}</dd>
                  <dt>Updated At</dt>
                  <dd title={moment(output.updated_at).format()}>{moment(output.updated_at).fromNow()}</dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="row">
              <div className="col-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Delete Output" slim={true} />

                    <p className="help-text">Deleted outputs will be immediately stopped and cannot be restored.</p>

                    <button className="btn btn-danger" type="button" onClick={onDelete}>Delete Output</button>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title={output.status === "RUNNING" ? "Pause Output" : "Start Output"}
                                           slim={true} />

                    <p className="help-text">You can start or pause outputs at any time.</p>

                    { output.status === "RUNNING"
                        ? <button className="btn btn-warning" type="button" onClick={onPause}>Pause Output</button>
                        : <button className="btn btn-success" type="button" onClick={onStart}>Start Output</button>}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Description" slim={true} />

                { output.description ? output.description : "No description."}
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  );

}