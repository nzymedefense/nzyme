import React, {useEffect, useState} from "react";
import {toast} from "react-toastify";
import BeaconsService from "../../../services/ethernet/BeaconsService";

const beaconsService = new BeaconsService();

export const beaconClassificationToName = (classification) => {
  switch (classification) {
    case "UNKNOWN": return "Unknown";
    case "BENIGN": return "Benign";
    case "SUSPICIOUS": return "Suspicious";
    case "MALICIOUS": return "Malicious";
    default: return classification;
  }
}

export const beaconClassificationToClassName = (classification) => {
  switch (classification) {
    case "UNKNOWN": return "classification-unknown";
    case "BENIGN": return "classification-benign";
    case "SUSPICIOUS": return "classification-suspicious";
    case "MALICIOUS": return "classification-hostile";
    default: return "";
  }
}

export default function BeaconClassification({classification, beaconUuid = undefined, organizationId = undefined,
                                               tenantId = undefined, enableEditMode = false, compact = false,
                                               onChange = undefined}) {

  const SAVE_BUTTON_TEXT = "Save";

  const [selectedClassification, setSelectedClassification] = useState(classification);
  const [inEditMode, setInEditMode] = useState(false);
  const [saveButtonText, setSaveButtonText] = useState(SAVE_BUTTON_TEXT);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setSelectedClassification(classification);
  }, [classification]);

  const save = (e, value = selectedClassification) => {
    if (e) {
      e.preventDefault();
    }

    setIsSaving(true);
    setSaveButtonText(<span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Saving ...</span>);

    beaconsService.classifyBeacon(beaconUuid, organizationId, tenantId, value, () => {
      setIsSaving(false);
      setSaveButtonText(SAVE_BUTTON_TEXT);
      setInEditMode(false);
      toast.success("Classification saved.");
      if (onChange) {
        onChange();
      }
    }, () => {
      toast.error("Could not save classification.");
      setSelectedClassification(classification);
      setIsSaving(false);
      setSaveButtonText(SAVE_BUTTON_TEXT);
    });
  }

  const format = (c) => {
    if (!c) {
      return <span className="text-muted">n/a</span>
    }

    return <span className={"classification " + beaconClassificationToClassName(c)}>{beaconClassificationToName(c)}</span>
  }

  if (!enableEditMode) {
    return format(classification);
  }

  if (compact) {
    if (inEditMode) {
      return (
        <select className="form-select form-select-sm classification-select"
                style={{width: "auto", display: "inline-block"}}
                autoFocus
                value={selectedClassification}
                disabled={isSaving}
                title="Change classification"
                onChange={(e) => { setSelectedClassification(e.target.value); save(null, e.target.value); }}
                onBlur={() => { if (!isSaving) { setInEditMode(false); } }}
                onKeyDown={(e) => { if (e.key === "Escape") { setInEditMode(false); } }}>
          <option value="UNKNOWN">Unknown</option>
          <option value="BENIGN">Benign</option>
          <option value="SUSPICIOUS">Suspicious</option>
          <option value="MALICIOUS">Malicious</option>
        </select>
      )
    }

    return (
      <React.Fragment>
        {format(classification)}{' '}
        <a href="#" className="classification-edit" title="Change classification"
           onClick={(e) => { e.preventDefault(); setInEditMode(true); }}>
          <i className="fa-solid fa-pen"></i>
        </a>
      </React.Fragment>
    )
  }

  if (inEditMode) {
    return (
      <div className="input-group mt-1">
        <select className="form-select form-select-sm"
                value={selectedClassification}
                onChange={(e) => setSelectedClassification(e.target.value)}>
          <option value="UNKNOWN">Unknown</option>
          <option value="BENIGN">Benign</option>
          <option value="SUSPICIOUS">Suspicious</option>
          <option value="MALICIOUS">Malicious</option>
        </select>
        <button className="btn btn-sm btn-success" onClick={save} disabled={isSaving} type="button">
          {saveButtonText}
        </button>
        <button className="btn btn-sm btn-outline-secondary"
                onClick={(e) => { e.preventDefault(); setInEditMode(false); }}
                disabled={isSaving}
                type="button">
          Cancel
        </button>
      </div>
    )
  }

  return (
    <React.Fragment>
      {format(classification)}{' '}
      <a href="#" onClick={(e) => { e.preventDefault(); setInEditMode(true); }}>Edit</a>
    </React.Fragment>
  )

}
