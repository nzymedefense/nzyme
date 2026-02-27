import {Navigate, useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import {MONITORING_MENU_ITEMS} from "../Dot11MenuItems";
import SectionMenuBar from "../../../shared/SectionMenuBar";
import AuthenticationManagementService from "../../../../services/AuthenticationManagementService";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";
import ApiRoutes from "../../../../util/ApiRoutes";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import GNSSMonitoringRuleForm from "../../../gnss/monitoring/GNSSMonitoringRuleForm";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";

const authenticationManagementService = new AuthenticationManagementService();

export default function Dot11CreateCustomMonitoringRulePage() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);

  const [redirect, setRedirect] = useState(null);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
  }, [organizationId, tenantId])

  const onFormSubmitted = (name, description, conditions, taps, onFailure) => {
    // TODO
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.DOT11.MONITORING.CUSTOM.INDEX} />
  }

  if (!organization || !tenant) {
    return <LoadingSpinner />
  }

  return (
    <>
      <div className="row">
        <div className="col-md-8">
          <SectionMenuBar items={MONITORING_MENU_ITEMS}
                          activeRoute={ApiRoutes.DOT11.MONITORING.CUSTOM.INDEX} />
        </div>

        <div className="col-md-4">
            <span className="float-end">
              <a className="btn btn-primary" href={ApiRoutes.DOT11.MONITORING.CUSTOM.INDEX}>
                Back
              </a>
            </span>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <h1>Create WiFi Monitoring Rule</h1>
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
    </>
  )
}

