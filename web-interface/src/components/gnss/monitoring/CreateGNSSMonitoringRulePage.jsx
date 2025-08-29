import AuthenticationManagementService from "../../../services/AuthenticationManagementService";
import {Navigate, useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import ApiRoutes from "../../../util/ApiRoutes";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import GNSSMonitoringRuleForm from "./GNSSMonitoringRuleForm";
import GnssService from "../../../services/GnssService";
import {notify} from "react-notify-toast";

const authenticationManagementService = new AuthenticationManagementService();
const gnssService = new GnssService();

export default function CreateGNSSMonitoringRulePage() {

  const {organizationId} = useParams();
  const {tenantId} = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);

  const [redirect, setRedirect] = useState(null);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
  }, [organizationId, tenantId])

  const onFormSubmitted = (name, description, conditions, taps, onFailure) => {
    gnssService.createMonitoringRule(name, description, conditions, taps, organizationId, tenantId, () => {
      // Success.
      notify.show("Monitoring rule created.", "success");
      setRedirect(true);
    }, onFailure)
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.GNSS.MONITORING.INDEX} />
  }

  if (!organization || !tenant) {
    return <LoadingSpinner/>
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-9">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">GNSS</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.GNSS.MONITORING.INDEX}>Monitoring</a>
                </li>
                <li className="breadcrumb-item">Rules</li>
                <li className="breadcrumb-item active" aria-current="page">Create Rule</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-primary" href={ApiRoutes.GNSS.MONITORING.INDEX}>
                Back
              </a>
            </span>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>Create GNSS Monitoring Rule</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Create Rule" slim={true}/>

                <GNSSMonitoringRuleForm onSubmit={onFormSubmitted} submitText="Create Rule" />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )
}

