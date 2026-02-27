import React, {useEffect, useState} from 'react';
import {Navigate, useParams} from "react-router-dom";
import {notify} from "react-notify-toast";
import Routes from "../../../util/ApiRoutes";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import UavTypeForm from "./UavTypeForm";
import UavService from "../../../services/UavService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";

const uavService = new UavService();

export default function EditCustomTypePage() {

  const {uuid} = useParams();

  const [organizationId, tenantId] = useSelectedTenant();

  const [type, setType] = useState(null);

  const [redirect, setRedirect] = React.useState(false);

  useEffect(() => {
    uavService.findCustomType(setType, uuid, organizationId, tenantId);
  }, [uuid, organizationId, tenantId]);

  const onFormSubmitted = (matchType, matchValue, defaultClassification, type, model, name, onFailure) => {
    uavService.editCustomType(uuid, organizationId, tenantId, matchType, matchValue, defaultClassification, type, model, name,
        () => {
          notify.show("Custom UAV type updated.", "success");
          setRedirect(true);
        }, onFailure)
  }

  if (redirect) {
    return <Navigate to={Routes.UAV.TYPES.INDEX} />
  }

  if (!type) {
    return <LoadingSpinner />
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
                <li className="breadcrumb-item">{type.name}</li>
                <li className="breadcrumb-item active">Edit</li>
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
            <h1>Edit Custom Unmanned Aerial Vehicle (UAV) Type &quot;{type.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Create Type" />

                <UavTypeForm onSubmit={onFormSubmitted}
                             matchType={type.match_type}
                             matchValue={type.match_value}
                             type={type.type}
                             model={type.model}
                             name={type.name}
                             defaultClassification={type.default_classification}
                             submitText="Edit Type" />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}