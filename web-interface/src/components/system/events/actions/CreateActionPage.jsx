import React, {useState} from "react";
import {Navigate, Routes} from "react-router-dom";
import ApiRoutes from "../../../../util/ApiRoutes";
import CreateActionSelect from "../shared/forms/CreateActionSelect";
import CreateActionProxy from "../shared/forms/CreateActionProxy";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";

function CreateActionPage() {

  const [complete, setComplete] = useState(false);

  const [type, setType] = useState("");

  if (complete) {
    return <Navigate to={ApiRoutes.SYSTEM.EVENTS.INDEX} />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.SYSTEM.EVENTS.INDEX}>Events &amp; Actions</a></li>
                <li className="breadcrumb-item">Actions</li>
                <li className="breadcrumb-item active" aria-current="page">Create</li>
              </ol>
            </nav>
          </div>

          <div className="col-2">
            <a className="btn btn-primary float-end" href={ApiRoutes.SYSTEM.EVENTS.INDEX}>Back</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Create Action" helpLink="https://go.nzyme.org/alerting-action-types"/>

                <CreateActionSelect type={type} setType={setType} />
                <CreateActionProxy type={type} setComplete={setComplete} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateActionPage;