import React, {useState} from "react";

export default function AssetDetailsAssetName({asset}) {

  const SAVE_BUTTON_TEXT = "Set Name";

  const [showForm, setShowForm] = useState(false);

  const [newName, setNewName] = useState(asset.name ? asset.name : "")

  const [saveButtonText, setSaveButtonText] = useState(SAVE_BUTTON_TEXT);
  const [isSaving, setIsSaving] = useState(false);

  const toggleForm = (e) => {
    e.preventDefault();
    setShowForm(!showForm);
  }

  const name = () => {
    if (asset.name) {
      return <span className="context-name">{asset.name}</span>
    } else {
      return <span className="text-muted">n/a</span>
    }
  }

  const button = () => {
    if (showForm) {
      return;
    }

    if (asset.name) {
      return <a href="#" onClick={toggleForm}>Change Name</a>
    } else {
      return <a href="#" onClick={toggleForm}>Set Name</a>
    }
  }

  const form = () => {
    if (!showForm) {
      return null;
    }

    return (
      <div className="input-group mt-2">
        <input type="text" className="form-control" id="new-asset-name"
               value={name} onChange={(e) => { setNewName(e.target.value) }} />

        <button className="btn btn-sm btn-primary" type="button" disabled={isSaving}>
          {saveButtonText}
        </button>

        <button className="btn btn-sm btn-secondary" type="button" onClick={toggleForm} disabled={isSaving}>
          Cancel
        </button>
      </div>
    )
  }

  return (
    <>
      {name()}{' '}{button()}

      {form()}
    </>
  )

}