import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import CotIntegrationService from "../../../../../../../services/integrations/CotIntegrationService";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import ApiRoutes from "../../../../../../../util/ApiRoutes";
import Routes from "../../../../../../../util/ApiRoutes";
import CardTitleWithControls from "../../../../../../shared/CardTitleWithControls";
import {notify} from "react-notify-toast";

const authenticationManagementService = new AuthenticationManagementService();
const cotIntegrationService = new CotIntegrationService();

export default function EditCotCertificatePage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();
  const { outputId } = useParams();

  const DEFAULT_SUBMIT_TEXT = "Update Client Certificate Bundle";

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [output, setOutput] = useState(null);

  const [certificateFiles, setCertificateFiles] = useState([]);
  const [certificatePassphrase, setCertificatePassphrase] = useState("");

  const [submitText, setSubmitText] = useState(DEFAULT_SUBMIT_TEXT);
  const [isSubmitting, setIsSubmitting] = React.useState(false);

  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);

    setOutput(null);
    cotIntegrationService.findOutput(setOutput, organizationId, tenantId, outputId);
  }, [organizationId, tenantId, outputId])

  const submit = (e) => {
    e.preventDefault();

    setIsSubmitting(true);
    setSubmitText(<span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Creating ...</span>)

    const formData = new FormData();
    formData.append("certificate", certificateFiles[0]);
    formData.append("certificate_passphrase", certificatePassphrase);

    cotIntegrationService.editOutputCertificate(organizationId, tenantId, outputId, formData, () => {
      notify.show("Cursor on Target output client certificate bundle updated.", "success");
      setRedirect(true);
    }, () => {
      notify.show("Could not update Cursor on Target output client certificate bundle.", "error");
      setSubmitText(DEFAULT_SUBMIT_TEXT)
      setIsSubmitting(false);
    })
  }

  const updateValue = (e, setter) => {
    setter(e.target.value);
  }

  const formIsReady = () => {
    return certificateFiles && certificateFiles.length === 1;
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.DETAILS(organizationId, tenantId, outputId)} />
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
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.DETAILS(organizationId, tenantId, outputId)}>
                    {output.name}
                  </a>
                </li>
                <li className="breadcrumb-item active">Edit Client Certificate Bundle</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-secondary"
                 href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.DETAILS(organizationId, tenantId, outputId)}>
                Back
              </a>&nbsp;
            </span>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>Edit Client Certificate Bundle of Cursor on Target Output &quot;{output.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Edit Client Certificate Bundle" slim={true} />

                <form>
                  <div className="mb-3">
                    <label htmlFor="certificate" className="form-label">Client Certificate Bundle (<code>PKCS#12 / .p12</code>)</label>
                    <input className="form-control" id="certificate" type="file" accept=".p12"
                           onChange={(e) => setCertificateFiles(e.target.files)}/>
                    <div className="form-text">
                      Your client certificate in <code>PKCS#12 / .p12</code> format. We will automatically trust the certificate
                      authority included in the uploaded file. The data is encrypted in the database.
                    </div>
                  </div>

                  <div className="mb-3">
                    <label htmlFor="certificate_passphrase" className="form-label">
                      Client Certificate Bundle Passphrase <small>Optional</small>
                    </label>
                    <input type="password" className="form-control" id="certificate_passphrase"
                           value={certificatePassphrase} onChange={(e) => { updateValue(e, setCertificatePassphrase) }} />
                    <div className="form-text">
                      Passphrase to access the uploaded <code>PKCS#12 / .p12</code> file. The password is encrypted in the
                      database. Leave empty if bundle has no passphrase.
                    </div>
                  </div>

                  <button className="btn btn-primary" onClick={submit} disabled={!formIsReady() || isSubmitting}>
                    {submitText}
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  );

}