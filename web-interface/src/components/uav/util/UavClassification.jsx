import React, {useEffect, useState} from "react";
import {capitalizeFirstLetter} from "../../../util/Tools";
import UavService from "../../../services/UavService";
import {notify} from "react-notify-toast";

const uavService = new UavService();

export default function UavClassification(props) {

  const SAVE_BUTTON_TEXT = "Save";

  const uav = props.uav;
  const enableEditMode = props.enableEditMode;
  const onChange = props.onChange;

  const [selectedClassification, setSelectedClassification] = useState(null);
  const [inEditMode, setInEditMode] = useState(false);

  const [saveButtonText, setSaveButtonText] = useState(SAVE_BUTTON_TEXT);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setSelectedClassification(uav.classification);
  }, [uav])

  const saveClassification = (e) => {
    e.preventDefault();

    setIsSaving(true);
    setSaveButtonText(<span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Saving ...</span>)

    uavService.classifyUav(uav.identifier, selectedClassification, () => {
      // Success.
      setIsSaving(false);
      setSaveButtonText(SAVE_BUTTON_TEXT)
      notify.show("Classification saved", "success");
      onChange();
    }, () => {
      // Error.
      notify.show("Could not save classification.", "error");
      setIsSaving(false);
      setSaveButtonText(SAVE_BUTTON_TEXT)
    })
  }

  const format = (c) => {
    let className = "";

    switch (c) {
      case "UNKNOWN":
        className = "classification-unknown";
        break;
      case "FRIENDLY":
        className = "classification-friendly";
        break;
      case "NEUTRAL":
        className = "classification-neutral";
        break;
      case "HOSTILE":
        className = "classification-hostile";
        break;
    }

    return <span className={"classification " + className}>{capitalizeFirstLetter(c.toLowerCase())}</span>;
  }

  if (!enableEditMode) {
    return format(uav.classification);
  } else {
    if (inEditMode) {
      return (
          <React.Fragment>
            <div className="input-group mt-1">
              <select className="form-select form-select-sm"
                      value={selectedClassification}
                      onChange={(e) => setSelectedClassification(e.target.value)}>
                <option value="UNKNOWN">Unknown</option>
                <option value="FRIENDLY">Friendly</option>
                <option value="HOSTILE">Hostile</option>
                <option value="NEUTRAL">Neutral</option>
              </select>
              <button className="btn btn-sm btn-success"
                      onClick={saveClassification}
                      disabled={isSaving}
                      type="button">
                {saveButtonText}
              </button>
              <button className="btn btn-sm btn-outline-secondary"
                      onClick={(e) => {e.preventDefault(); setInEditMode(false); }}
                      disabled={isSaving}
                      type="button">
                Cancel
              </button>
            </div>
          </React.Fragment>
      )
    } else {
      return (
          <React.Fragment>
            {format(uav.classification)}{' '}

            <a href="#" onClick={(e) => {
              e.preventDefault();
              setInEditMode(true)
            }}>Edit</a>
          </React.Fragment>
      )
    }
  }

}