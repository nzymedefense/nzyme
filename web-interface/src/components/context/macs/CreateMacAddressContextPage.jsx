import React, {useState} from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import MacAddressContextForm from "./MacAddressContextForm";
import ContextService from "../../../services/ContextService";
import {notify} from "react-notify-toast";
import {Navigate, useLocation} from "react-router-dom";

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

const contextService = new ContextService();

function CreateMacAddressContextPage() {

  let urlQuery = useQuery()
  const [complete, setComplete] = useState(false);

  const [errorMessage, setErrorMessage] = useState(null);

  const [passedUrlMacAddress, setPassedUrlMacAddress] = useState(
      urlQuery.get("address") ? urlQuery.get("address") : ""
  );

  const onSubmit = (macAddress, name, description, notes, organizationId, tenantId, onComplete) => {
    contextService.createMacAddressContext(
        macAddress,
        name,
        description,
        notes,
        organizationId,
        tenantId,
        () => {
          notify.show('Context created.', 'success');
          onComplete();
          setComplete(true);
        },
        (error) => {
          notify.show('Could not create context.', 'error');
          setErrorMessage(error.response.data.message);
          onComplete();
        }
    )
  }

  if (complete) {
    return <Navigate to={ApiRoutes.CONTEXT.MAC_ADDRESSES.INDEX} />
  }

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
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Create MAC Address Context</h3>

                <MacAddressContextForm submitText={"Add Context"}
                                       onSubmit={onSubmit}
                                       macAddress={passedUrlMacAddress}
                                       errorMessage={errorMessage} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateMacAddressContextPage;