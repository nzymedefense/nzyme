import React from 'react';
import {Navigate} from "react-router-dom";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import UavService from "../../../services/UavService";
import Routes from "../../../util/ApiRoutes";
import {notify} from "react-notify-toast";
import UavTypeForm from "./UavTypeForm";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";

const uavService = new UavService();

export default function CreateCustomTypePage() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [redirect, setRedirect] = React.useState(false);

  const onFormSubmitted = (matchType, matchValue, defaultClassification, type, model, name, onFailure) => {
    uavService.createCustomType(organizationId, tenantId, matchType, matchValue, defaultClassification, type, model, name,
        () => {
          notify.show("Custom UAV type created.", "success");
          setRedirect(true);
        }, onFailure)
  }

  if (redirect) {
    return <Navigate to={Routes.UAV.TYPES.INDEX} />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">UAVs</li>
                <li className="breadcrumb-item"><a href={Routes.UAV.TYPES.INDEX}>Types</a></li>
                <li className="breadcrumb-item">Custom Types</li>
                <li className="breadcrumb-item active" aria-current="page">Create</li>
              </ol>
            </nav>
          </div>

          <div className="col-2">
            <a className="btn btn-secondary float-end" href={Routes.UAV.TYPES.INDEX}>
              Back
            </a>
          </div>
        </div>

        <div className="row">
          <div className="col-12">
            <h1>Create Custom Unmanned Aerial Vehicle (UAV) Type</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Create Type" />

                <UavTypeForm onSubmit={onFormSubmitted} matchType="EXACT" type="GENERIC" submitText="Create Type" />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}