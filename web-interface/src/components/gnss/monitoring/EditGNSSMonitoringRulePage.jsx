import React, {useEffect, useState} from "react";
import GnssService from "../../../services/GnssService";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import {Navigate, useParams} from "react-router-dom";
import LoadingSpinner from "../../misc/LoadingSpinner";
import ApiRoutes from "../../../util/ApiRoutes";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import GNSSMonitoringRuleForm from "./GNSSMonitoringRuleForm";
import {notify} from "react-notify-toast";

const gnssService = new GnssService();

export default function EditGNSSMonitoringRulePage() {

  const {uuid} = useParams();
  const [organizationId, tenantId] = useSelectedTenant();

  const [rule, setRule] = useState(null);

  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    gnssService.findMonitoringRule(uuid, organizationId, tenantId, setRule)
  }, [uuid]);

  const onFormSubmitted = (name, description, conditions, taps, onFailure) => {
    gnssService.editMonitoringRule(uuid, name, description, conditions, taps, organizationId, tenantId, () => {
      // Success.
      notify.show("Monitoring rule updated.", "success");
      setRedirect(true);
    }, onFailure)
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.GNSS.MONITORING.RULES.DETAILS(uuid)} />
  }

  if (!rule) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-9">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item"><a href={ApiRoutes.GNSS.CONSTELLATIONS}>GNSS</a></li>
              <li className="breadcrumb-item"><a href={ApiRoutes.GNSS.MONITORING.INDEX}>Monitoring</a></li>
              <li className="breadcrumb-item">Rules</li>
              <li className="breadcrumb-item"><a href={ApiRoutes.GNSS.MONITORING.RULES.DETAILS(uuid)}>{rule.name}</a></li>
              <li className="breadcrumb-item active" aria-current="page">Edit</li>
            </ol>
          </nav>
        </div>

        <div className="col-3">
            <span className="float-end">
              <a href={ApiRoutes.GNSS.MONITORING.RULES.DETAILS(uuid)} className="btn btn-secondary">Back</a>
            </span>
        </div>
      </div>

      <div className="row mt-2">
        <div className="col-12">
          <h1>Edit GNSS Monitoring Rule &quot;{rule.name}&quot;</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-xl-12 col-xxl-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Edit Rule" slim={true}/>

              <GNSSMonitoringRuleForm onSubmit={onFormSubmitted}
                                      name={rule.name}
                                      description={rule.description}
                                      selectedTaps={rule.taps}
                                      conditions={rule.conditions}
                                      submitText="Edit Rule" />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )


}