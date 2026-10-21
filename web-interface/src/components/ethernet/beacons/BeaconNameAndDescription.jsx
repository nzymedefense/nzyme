import React, {useState} from "react";
import {toast} from "react-toastify";
import BeaconsService from "../../../services/ethernet/BeaconsService";

const beaconsService = new BeaconsService();

export default function BeaconNameAndDescription({beacon, organizationId, tenantId, onChange, canEdit}) {

  const SAVE_BUTTON_TEXT = "Save";

  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState(beacon.name);
  const [description, setDescription] = useState(beacon.description ? beacon.description : "");
  const [saveButtonText, setSaveButtonText] = useState(SAVE_BUTTON_TEXT);
  const [isSaving, setIsSaving] = useState(false);

  const toggleForm = (e) => {
    e.preventDefault();
    setName(beacon.name);
    setDescription(beacon.description ? beacon.description : "");
    setShowForm(!showForm);
  }

  const onSubmit = (e) => {
    e.preventDefault();

    if (name.trim().length === 0) {
      toast.error("The name cannot be empty.");
      return;
    }

    setIsSaving(true);
    setSaveButtonText("Please wait...");

    beaconsService.updateBeacon(beacon.uuid, organizationId, tenantId, name.trim(), description, () => {
      toast.success("Beacon updated.");
      setIsSaving(false);
      setSaveButtonText(SAVE_BUTTON_TEXT);
      setShowForm(false);
      onChange();
    }, (error) => {
      if (error && error.response && error.response.status === 409) {
        toast.error("Another beacon already has this name.");
      } else {
        toast.error("Could not update beacon.");
      }
      setIsSaving(false);
      setSaveButtonText(SAVE_BUTTON_TEXT);
    });
  }

  if (showForm) {
    return (
      <form onSubmit={onSubmit}>
        <div className="mb-3">
          <label htmlFor="beacon-name" className="form-label">Name</label>
          <input type="text" className="form-control" id="beacon-name" maxLength={128}
                 value={name} onChange={(e) => setName(e.target.value)} />
          <div className="form-text">A unique name for this beacon. Random until you change it.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="beacon-description" className="form-label">Description <small>Optional</small></label>
          <textarea className="form-control" id="beacon-description" rows={4}
                    value={description} onChange={(e) => setDescription(e.target.value)} />
          <div className="form-text">
            What this beacon is and why it is classified the way it is. Use the notes below for findings over time.
          </div>
        </div>

        <button className="btn btn-sm btn-primary" type="submit" disabled={isSaving}>{saveButtonText}</button>{' '}
        <button className="btn btn-sm btn-secondary" type="button" onClick={toggleForm} disabled={isSaving}>Cancel</button>
      </form>
    )
  }

  return (
    <dl className="mb-0">
      <dt>Name</dt>
      <dd>
        {beacon.name}{' '}
        {canEdit ? <a href="#" onClick={toggleForm}>Edit</a> : null}
      </dd>
      <dt>Description</dt>
      <dd className="mb-0">
        {beacon.description ?
          <span style={{whiteSpace: "pre-wrap"}}>{beacon.description}</span>
          : <span className="text-muted">No description yet.</span>}
      </dd>
    </dl>
  )

}
