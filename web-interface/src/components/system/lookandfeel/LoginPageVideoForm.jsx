import React, {useState} from "react";
import FormSubmitErrorMessage from "../../misc/FormSubmitErrorMessage";
import SystemService from "../../../services/SystemService";
import {notify} from "react-notify-toast";

const systemService = new SystemService();

export default function LoginPageVideoForm() {

  const [imageFiles, setImageFiles] = useState([]);

  const [isUploading, setIsUploading] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  const submit = (e) => {
    e.preventDefault();

    const formData = new FormData();
    formData.append("image", imageFiles[0]);

    setIsUploading(true);

    systemService.uploadLoginImage(formData, () => {
      setIsUploading(false);
      notify.show('Login page image uploaded..', 'success');
    },
    (error) => {
      setIsUploading(false);
      if (error.response && error.response.status === 400) {
        setErrorMessage(error.response.data.message);
      } else {
        setErrorMessage("Unexpected error. Please try again.");
      }
    });
  }

  const reset = (e) => {
    e.preventDefault();

    if (!confirm("Really reset login page image to default?")) {
      return
    }

    systemService.resetLoginImage(() => notify.show('Login page image reset to default.', 'success'));
  }

  return (
      <form>
        <div className="mb-3">
          <label htmlFor="file" className="form-label">
            Image File (JPG or PNG, maximum file size 1MB, must be 700x600px)
          </label>
          <input className="form-control form-control-sm" name="file" id="file" type="file"
                 onChange={(e) => setImageFiles(e.target.files)}/>
        </div>

        <button className="btn btn-sm btn-primary" onClick={submit} disabled={imageFiles.length === 0}>
          {isUploading ? "Please Wait ..." : "Upload"}
        </button>&nbsp;

        <button className="btn btn-sm btn-danger" onClick={reset}>Reset Login Page Image to Default</button>

        <FormSubmitErrorMessage message={errorMessage} />
      </form>
  )

}