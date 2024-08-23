import React, {useEffect, useState} from "react";
import ApiRoutes from "../../../../util/ApiRoutes";
import {Navigate, useParams} from "react-router-dom";
import Dot11Service from "../../../../services/Dot11Service";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import CustomBanditForm from "./CustomBanditForm";
import {notify} from "react-notify-toast";

const dot11Service = new Dot11Service();

function EditCustomBanditPage() {

  const {id} = useParams();

  const [bandit, setBandit] = useState();
  const [redirect, setRedirect] = useState(false);

  const onEdit = (name, description) => {
    dot11Service.editCustomBandit(bandit.id, name, description, () => {
      notify.show('Custom bandit updated.', 'success');
      setRedirect(true);
    });
  }

  useEffect(() => {
    dot11Service.findCustomBandit(id, setBandit);
  }, [id]);

  if (!bandit) {
    return <LoadingSpinner />
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.DOT11.MONITORING.BANDITS.CUSTOM_DETAILS(bandit.id)} />
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
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.DOT11.MONITORING.BANDITS.CUSTOM_DETAILS(bandit.id)}>
                    {bandit.name}
                  </a>
                </li>
                <li className="breadcrumb-item active" aria-current="page">Edit</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-5">
            <span className="float-end">
              <a className="btn btn-secondary" href={ApiRoutes.DOT11.MONITORING.BANDITS.CUSTOM_DETAILS(bandit.id)}>
                Back
              </a>
            </span>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>
              Edit Custom Bandit &quot;{bandit.name}&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Edit Custom Bandit</h3>

                <CustomBanditForm submitText="Edit Bandit"
                                  onSubmit={onEdit}
                                  name={bandit.name}
                                  description={bandit.description} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default EditCustomBanditPage;