import React, {useEffect, useState} from "react";
import ApiRoutes from "../../../../util/ApiRoutes";
import {useParams} from "react-router-dom";
import Dot11Service from "../../../../services/Dot11Service";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import BanditFingerprints from "./BanditFingerprints";

const dot11Service = new Dot11Service();

function BuiltinBanditDetailsPage() {

  const {id} = useParams();

  const [bandit, setBandit] = useState();

  useEffect(() => {
    dot11Service.findBuiltinBandit(id, setBandit);
  }, [id]);

  if (!bandit) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-7">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.INDEX}>Monitoring</a></li>
                <li className="breadcrumb-item">Bandits</li>
                <li className="breadcrumb-item">Built-In</li>
                <li className="breadcrumb-item active" aria-current="page">{bandit.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-5">
            <span className="float-end">
              <a className="btn btn-primary" href={ApiRoutes.DOT11.MONITORING.INDEX}>Back</a>
            </span>
          </div>

        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>
              Built-In Bandit &quot;{bandit.name}&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Description</h3>

                <p className="mb-0">
                  {bandit.description}
                </p>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Fingerprints</h3>

                <p>
                  You can learn more about fingerprints in
                  the <a href="https://go.nzyme.org/wifi-fingerprinting">documentation</a>
                </p>

                <BanditFingerprints fingerprints={bandit.fingerprints} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default BuiltinBanditDetailsPage;