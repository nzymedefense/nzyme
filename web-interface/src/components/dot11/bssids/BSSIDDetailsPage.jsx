import React from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../util/ApiRoutes";

function BSSIDDetailsPage() {

  const {bssidParam} = useParams();

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.NETWORKS.BSSIDS}>Access Points</a></li>
                <li className="breadcrumb-item">{bssidParam}</li>
                <li className="breadcrumb-item active" aria-current="page">Details</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-12">
            <h1>
              BSSID &quot;{bssidParam}&quot;
            </h1>
          </div>
        </div>

        <div className="alert alert-info">
          This page is under construction.
        </div>
      </React.Fragment>
  )

}

export default BSSIDDetailsPage;