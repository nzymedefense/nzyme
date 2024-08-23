import React, {useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import Dot11Service from "../../../../services/Dot11Service";
import ApiRoutes from "../../../../util/ApiRoutes";
import CustomBanditForm from "./CustomBanditForm";
import {notify} from "react-notify-toast";

const dot11Service = new Dot11Service();

function CreateCustomBanditPage(props) {

  const { organizationId } = useParams();
  const { tenantId } = useParams();

  const [redirect, setRedirect] = useState(false);

  const create = (name, description) => {
    dot11Service.createCustomBandit(organizationId, tenantId, name, description, () => {
      notify.show('Custom bandit created.', 'success');
      setRedirect(true);
    });
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.DOT11.MONITORING.BANDITS.INDEX} />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-7">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.INDEX}>Monitoring</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.BANDITS.INDEX}>Bandits</a></li>
                <li className="breadcrumb-item">Custom</li>
                <li className="breadcrumb-item active" aria-current="page">Create</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-5">
            <span className="float-end">
              <a className="btn btn-primary" href={ApiRoutes.DOT11.MONITORING.BANDITS.INDEX}>Back</a>
            </span>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>
              Create new Custom Bandit
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>New Custom Bandit</h3>

                <p className="text-muted">
                  You can add fingerprints to the bandit after creating it.
                </p>

                <CustomBanditForm submitText="Create Custom Bandit" onSubmit={create} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateCustomBanditPage;