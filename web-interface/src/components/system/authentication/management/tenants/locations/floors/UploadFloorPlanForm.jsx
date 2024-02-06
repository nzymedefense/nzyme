import React, {useState} from "react";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import FormSubmitErrorMessage from "../../../../../../misc/FormSubmitErrorMessage";

const authenticationManagementService = new AuthenticationManagementService();

function UploadFloorPlanForm(props) {

  const organizationId = props.organizationId;
  const tenantId  = props.tenantId;
  const locationId  = props.locationId;
  const floorId  = props.floorId;

  const submitText = props.submitText;

  const [errorMessage, setErrorMessage] = useState(null);
  const onSuccess = props.onSuccess;

  const [planFiles, setPlanFiles] = useState(null);

  const submit = () => {
    const formData = new FormData();
    formData.append("plan", planFiles[0]);

    authenticationManagementService.uploadFloorPlan(organizationId, tenantId, locationId, floorId, formData,
        () => onSuccess(),
        (error) => {
          if (error.response.status === 400) {
            setErrorMessage(error.response.data.message);
          } else {
            setErrorMessage("Unexpected error. Please try again.");
          }
        })
  }

  return (
      <React.Fragment>
        <div className="mb-3">
          <label htmlFor="plan" className="form-label">
            Floor Plan Image File (JPG or PNG)
          </label>
          <input className="form-control form-control-sm" name="plan" id="plan" type="file"
                 onChange={(e) => setPlanFiles(e.target.files)}/>
        </div>

        <div className="mb-3">
          <button className="btn btn-sm btn-primary " onClick={submit}>
            {submitText}
          </button>

          <FormSubmitErrorMessage message={errorMessage}/>
        </div>
      </React.Fragment>
  )

}

export default UploadFloorPlanForm;