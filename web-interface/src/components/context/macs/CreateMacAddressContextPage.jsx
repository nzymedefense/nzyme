import React from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import MacAddressContextForm from "./MacAddressContextForm";

function CreateMacAddressContextPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-8">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">Context</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.CONTEXT.MAC_ADDRESSES.INDEX}>MAC Addresses</a>
                </li>
                <li className="breadcrumb-item active">Create</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row">
          <div className="col-md-8">
            <h1>
              Context: MAC Addresses
            </h1>
          </div>

          <div className="col-md-4">
            <span className="float-end">
              <a className="btn btn-primary" href={ApiRoutes.CONTEXT.MAC_ADDRESSES.INDEX}>Back</a>
            </span>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-lg-12 col-xl-6">
            <div className="card">
              <div className="card-body">
                <h3>Create MAC Address Context</h3>

                <MacAddressContextForm submitText={"Add Context"} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateMacAddressContextPage;