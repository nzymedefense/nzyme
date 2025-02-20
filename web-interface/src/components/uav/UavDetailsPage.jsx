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
import SignalStrength from "../shared/SignalStrength";
import {disableTapSelector, enableTapSelector} from "../misc/TapSelector";
import moment from "moment/moment";

const uavService = new UavService();

export default function UavDetailsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const {identifierParam} = useParams();

  const [uav, setUav] = useState(null);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

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
              <li className="breadcrumb-item">{uav.summary.identifier.substring(0, 7)}</li>
              <li className="breadcrumb-item active" aria-current="page">Details</li>
            </ol>
          </nav>
        </div>

        <div className="col-12">
          <h1>
            UAV &quot;{uav.summary.identifier.substring(0, 7)}&quot;
          </h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-4">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="UAV Information"
                                     fixedAppliedTimeRange={Presets.ALL_TIME}/>

              <dl className="mb-0">
                <dt>Designation</dt>
                <dd>{uav.summary.designation}</dd>
                <dt>Detection Source</dt>
                <dd><UavDetectionSource source={uav.summary.detection_source} /></dd>
                <dt>UAV Type</dt>
                <dd><UavType type={uav.summary.uav_type} /></dd>
                <dt>Signal Strength</dt>
                <dd><SignalStrength strength={uav.summary.rssi_average} selectedTapCount={selectedTaps.length}/></dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="col-4">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="IDs and License"
                                     fixedAppliedTimeRange={Presets.ALL_TIME}/>

              <dl className="mb-0">
                <dt>UAV Serial Number</dt>
                <dd>TODO TODO TODO</dd>
                <dt>UAV Registration</dt>
                <dd>TODO TODO TODO</dd>
                <dt>Other UAV IDs</dt>
                <dd>TODO TODO TODO</dd>
                <dt>Operator License ID</dt>
                <dd>TODO TODO TODO</dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="col-4">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Metadata"
                                     fixedAppliedTimeRange={Presets.ALL_TIME}/>

              <dl className="mb-0">
                <dt>First Seen</dt>
                <dd>
                  {moment(uav.summary.first_seen).format()}{' '}
                  <span className="text-muted">
                    (Note: UAV data retention time is {uav.data_retention_days} days)
                  </span>
                </dd>
                <dt>Last Seen</dt>
                <dd>{moment(uav.summary.last_seen).format()}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}