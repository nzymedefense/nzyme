import React, {useEffect, useState} from "react";
import SystemService from "../../../services/SystemService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import FormSubmitErrorMessage from "../../misc/FormSubmitErrorMessage";
import {notify} from "react-notify-toast";

const systemService = new SystemService();

export default function SidebarTitleForm() {

  const [originalSidebarTitle, setOriginalSidebarTitle] = useState(null);

  const [sidebarTitle, setSidebarTitle] = useState(null);
  const [sidebarSubtitle, setSidebarSubtitle] = useState(null);

  const [errorMessage, setErrorMessage] = useState(null);

  useEffect(() => {
    systemService.getSidebarTitle(setOriginalSidebarTitle, setSidebarSubtitle);
  }, []);

  useEffect(() => {
    setSidebarTitle(originalSidebarTitle);
  }, [originalSidebarTitle]);

  const onSubmit = (e) => {
    e.preventDefault();
    systemService.setSidebarTitle(sidebarTitle, sidebarSubtitle,
        () => {
          notify.show('Sidebar title updated.', 'success');
        },
        (error) => {
          if (error.response && error.response.status === 400) {
            setErrorMessage(error.response.data.message);
          } else {
            setErrorMessage("Unexpected error. Please try again.");
          }
        })
  }

  if (!originalSidebarTitle) {
    return <LoadingSpinner />
  }

  return (
      <form>
        <div className="mb-3">
          <label htmlFor="title" className="form-label">Title (Maximum 12 characters)</label>
          <input type="title" className="form-control" id="title" value={sidebarTitle || ""}
                 maxLength={12}
                 onChange={(e) => setSidebarTitle(e.target.value)}/>
        </div>

        <div className="mb-3">
          <label htmlFor="title" className="form-label">Subtitle (Optional, Maximum 28 characters)</label>
          <input type="title" className="form-control" id="title" value={sidebarSubtitle || ""}
                 maxLength={28}
                 onChange={(e) => setSidebarSubtitle(e.target.value)}/>
        </div>

        <button type="submit" className="btn btn-sm btn-secondary" onClick={onSubmit} disabled={!sidebarTitle}>
          Save
        </button>

        <FormSubmitErrorMessage message={errorMessage} />
      </form>
  )

}