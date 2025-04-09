import React from "react";
import UavDetectionSource from "../../../uav/util/UavDetectionSource";
import UavClassification from "../../../uav/util/UavClassification";
import ApiRoutes from "../../../../util/ApiRoutes";

export default function UavDetectedAlertDetails(props) {

  const alert = props.alert;

  // LINK: ApiRoutes.UAV.DETAILS(alert.attributes.identifier) - add direct organizationId, tenantId. they are in alert object

  return (
      <React.Fragment>
        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Details</h3>
                <dl>
                  <dt>UAV Identifier</dt>
                  <dd>{alert.attributes.identifier}</dd>
                  <dt>Classification</dt>
                  <dd><UavClassification classification={alert.attributes.classification} /></dd>
                  <dt>Detection Source</dt>
                  <dd><UavDetectionSource source={alert.attributes.detection_source} /></dd>

                  <p className="mb-0 mt-3">
                    A UAV (Unmanned Aerial Vehicle) has been detected.
                  </p>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Possible false-positives</h3>

                <p>
                  These circumstances can lead to false-positive alerts of this type:
                </p>

                <ul className="mb-0">
                  <li className="text-muted">n/a</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )


}