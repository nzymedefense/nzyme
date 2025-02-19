import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../App";
import {useParams} from "react-router-dom";
import UavService from "../../services/UavService";
import LoadingSpinner from "../misc/LoadingSpinner";
import ApiRoutes from "../../util/ApiRoutes";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import {Presets} from "../shared/timerange/TimeRange";
import UavDetectionSource from "./util/UavDetectionSource";
import UavType from "./util/UavType";

const uavService = new UavService();

export default function UavDetailsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const {identifierParam} = useParams();

  const [uav, setUav] = useState(null);

  useEffect(() => {
    uavService.findOne(setUav, identifierParam, selectedTaps);
  }, [selectedTaps]);

  if (!uav) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-12">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item"><a href={ApiRoutes.UAVS.INDEX}>UAVs</a></li>
              <li className="breadcrumb-item">{uav.identifier.substring(0, 7)}</li>
              <li className="breadcrumb-item active" aria-current="page">Details</li>
            </ol>
          </nav>
        </div>

        <div className="col-12">
          <h1>
            UAV &quot;{uav.identifier.substring(0, 7)}&quot;
          </h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-6">
          <div className="row">
            <div className="col-12">
              <div className="card">
                <div className="card-body">
                  <CardTitleWithControls title="UAV Information"
                                         fixedAppliedTimeRange={Presets.ALL_TIME}/>

                  <dl className="mb-0">
                    <dt>Designation</dt>
                    <dd>{uav.designation}</dd>
                    <dt>Detection Source</dt>
                    <dd><UavDetectionSource source={uav.detection_source} /></dd>
                    <dt>UAV Type</dt>
                    <dd><UavType type={uav.uav_type} /></dd>
                    <dt>Operator License ID</dt>
                    <dd>TODO TODO TODO</dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}