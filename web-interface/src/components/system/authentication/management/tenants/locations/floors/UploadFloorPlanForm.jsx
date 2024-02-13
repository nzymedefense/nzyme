import React, {useState} from "react";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import FormSubmitErrorMessage from "../../../../../../misc/FormSubmitErrorMessage";

const authenticationManagementService = new AuthenticationManagementService();

function UploadFloorPlanForm(props) {

  const organizationId = props.organizationId;
  const tenantId  = props.tenantId;
  const locationId  = props.locationId;
  const floorId  = props.floorId;

  const hasExistingPlan = props.hasExistingPlan;
  const submitText = props.submitText;

  const [isUploading, setIsUploading] = useState(false);

  const [errorMessage, setErrorMessage] = useState(null);
  const onSuccess = props.onSuccess;

  const [planFiles, setPlanFiles] = useState([]);

  const submit = () => {
    if (hasExistingPlan && !confirm("Replace floor plan? The existing plan for this floor will be replaced.")) {
      return;
    }

    const formData = new FormData();
    formData.append("plan", planFiles[0]);

    setIsUploading(true);
    authenticationManagementService.uploadFloorPlan(organizationId, tenantId, locationId, floorId, formData,
        () => {
          setIsUploading(false);
          onSuccess();
        },
        (error) => {
          setIsUploading(false);
          if (error.response && error.response.status === 400) {
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
            Floor Plan Image File (JPG or PNG, maximum file size 5MB)
          </label>
          <input className="form-control form-control-sm" name="plan" id="plan" type="file"
                 onChange={(e) => setPlanFiles(e.target.files)}/>
        </div>

        <button className="btn btn-sm btn-primary" onClick={submit} disabled={planFiles.length === 0 || isUploading}>
          {isUploading ? "Please Wait ..." : submitText}
        </button>

        <FormSubmitErrorMessage message={errorMessage} />
      </React.Fragment>
  )

}

export default UploadFloorPlanForm;