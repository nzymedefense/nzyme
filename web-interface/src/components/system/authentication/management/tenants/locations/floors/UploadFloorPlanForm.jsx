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

  const [widthMeters, setWidthMeters] = useState("");
  const [lengthMeters, setLengthMeters] = useState("");

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
    formData.append("width_meters", widthMeters);
    formData.append("length_meters", lengthMeters)

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

  const formIsReady = () => {
    return planFiles.length > 0 && parseInt(widthMeters, 10) > 0 && parseInt(lengthMeters, 10) > 0 && !isUploading
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

        <div className="row mb-3">
          <div className="col-6">
            <label htmlFor="width_meters" className="form-label">
              Width (Meters)
            </label>
            <input className="form-control form-control-sm" name="width_meters" id="width_meters" type="number"
                   onChange={(e) => setWidthMeters(e.target.value)}/>
          </div>
          <div className="col-6">
            <label htmlFor="length_meters" className="form-label">
              Length (Meters)
            </label>
            <input className="form-control form-control-sm" name="length_meters" id="length_meters" type="number"
                   onChange={(e) => setLengthMeters(e.target.value)}/>
          </div>
        </div>

        <button className="btn btn-sm btn-primary" onClick={submit} disabled={!formIsReady()}>
          {isUploading ? "Please Wait ..." : submitText}
        </button>

        <FormSubmitErrorMessage message={errorMessage}/>
      </React.Fragment>
  )

}

export default UploadFloorPlanForm;